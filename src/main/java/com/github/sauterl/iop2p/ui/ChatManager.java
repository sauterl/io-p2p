package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.JSONUtils;
import com.github.sauterl.iop2p.crypto.KeyStore;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.data.FileMessage;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.data.MessageType;
import com.github.sauterl.iop2p.ipfs.FileExchange;
import com.github.sauterl.iop2p.ipfs.IPFSAdapter;
import com.github.sauterl.iop2p.net.Chatter;
import com.github.sauterl.iop2p.ui.components.ModifiableListHandler;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.AddEvent;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.RemoveEvent;
import io.ipfs.api.IPFS;
import io.ipfs.multiaddr.MultiAddress;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle general incoming messages and multiple chats at once
 *
 * <p>General managing instance of the application ui
 *
 * @author loris.sauter
 */
public class ChatManager implements ModifiableListHandler<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatManager.class);

  private final Chatter theChatter;
  private final Chatter broadcastChatter;
  private ArrayList<String> others = new ArrayList<>();
  private ChatWindow view;

  private HashMap<String, Chat> chatHashMap = new HashMap<>();
  private KeyStore keyStore = new KeyStore();

  private Chat activeChat;

  private FileExchange fileExchange;

  public FileExchange getFileExchange() {
    return fileExchange;
  }


  public ChatManager(ChatWindow chatWindow) {
    view = chatWindow;
    theChatter =
        new Chatter(
            ChatGuiCommand.getInstance().getUsername(),
            ChatGuiCommand.getInstance().getIpfs().pubsub,
            false);
    theChatter.start();
    theChatter.setOnMessageReceived(this::handleIncomingMessage);
    broadcastChatter =
        new Chatter(
            ChatGuiCommand.getInstance().getUsername(),
            ChatGuiCommand.getInstance().getIpfs().pubsub,
            true);
    broadcastChatter.start();
    broadcastChatter.setOnMessageReceived(this::handleIncomingMessage);
    LOGGER.info("Our ID: {}", getOwnNodeId());
    LOGGER.debug("Addresses: {}", Arrays.toString(getOwnAddresses()));
    if(IPFSAdapter.getInstance().getCachedIPFS().isPresent()){
      fileExchange = new FileExchange(IPFSAdapter.getInstance().getCachedIPFS().get());
    }
  }

  public void loadAndInitSecurityModule() {
    if (IOUtils.hasKeyStore()) {
      try {
        keyStore = IOUtils.loadKeystore();
      } catch (IOException e) {
        LOGGER.error("Could'nt load keystore", e);
      }
    }
    keyStore
        .entries()
        .forEach(
            e -> {
              if (chatHashMap.containsKey(e.getUser())) {
                chatHashMap.get(e.getUser()).setKeystoreEntry(e);
              }
            });
    LOGGER.debug("Loaded keys where possible");
  }

  private void handleIncomingMessage(Message m) {
    LOGGER.debug("Incoming message: {}", m);
    if(m == null){
      return;
    }
    if(m.getType() == MessageType.FILE){
      FileMessage msg = new FileMessage(m);
      try {
        File f = fileExchange.loadPublishedFile(msg);
        LOGGER.info("Downloaded file successfully {}", f);
      } catch (IOException e) {
        LOGGER.error("Couldn download file",e);
      }
    }
    if (!m.getTargetUsername().equals(theChatter.getUsername())) {
      // ignoring those chats not for us
      return;
    }
    if (m.getSourceUsername().equals(activeChat.getThey())) {
      activeChat.receive(m);
    } else if (chatHashMap.containsKey(m.getSourceUsername())) {
      LOGGER.debug("Will switch active chat");
      Platform.runLater(
          () -> {
            chatHashMap.get(m.getSourceUsername()).receive(m);
            view.selectChat(m.getSourceUsername());
          });
    } else {
      LOGGER.debug("Will create new chat");

      Platform.runLater(
          () -> {
            LOGGER.debug("Creating new chat for {}", m.getSourceUsername());
            addChat(m.getSourceUsername());
            Chat c = chatHashMap.get(m.getSourceUsername());
            c.receive(m);
            view.selectChat(c.getThey());
          });
    }

  }


  @Override
  public void onRemove(RemoveEvent<String> event) {
    String they = event.getSelected();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Chat Deletion");
    alert.setHeaderText("Delete chat with " + they);
    alert.setContentText(
        "Are you sure you want to delete the chat with "
            + they
            + "\n "
            + "Deletion cannot be undone and may cause loss of data");
    Optional<ButtonType> out = alert.showAndWait();
    out.ifPresent(
        buttonType -> {
          if (buttonType.equals(ButtonType.OK)) {
            view.getChatsList().getItems().remove(event.getSelectedIndex());
            try {
              IOUtils.deleteHistory(they);
            } catch (IOException e) {
              LOGGER.error("Error while deleting history for " + they, e);
            }
          } else {
            // Noting
          }
        });
  }

  @Override
  public void onAdd(AddEvent<String> event) {
    TextInputDialog dialog = new TextInputDialog("Bob");
    dialog.setTitle("Chat");
    dialog.setHeaderText("Enter the name of your partner:");
    dialog.setContentText("Name:");
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(
        name -> {
          if (!chatHashMap.containsKey(name)) {
            addChat(name);

          } else {
            LOGGER.warn("For user {} already exists a chat", name);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Duplicate Chat");
            alert.setHeaderText("Chat addressee already in list");
            alert.setContentText("Cannot have two chats to the same addressee");
            alert.showAndWait();
          }
        });
  }

  public void stop() {
    theChatter.stop();
    broadcastChatter.stop();
    if (keyStore != null && !keyStore.isEmpty()) {
      try {
        IOUtils.saveKeystore(keyStore);
      } catch (IOException e) {
        LOGGER.error("Couldn't save keystore");
      }
    }
    System.exit(0);
  }

  public ArrayList<String> getOthers() {
    return others;
  }

  public void loadExisitngChats() {
    Path path = Paths.get(IOUtils.getDirectory());
    Arrays.stream(
            Objects.requireNonNull(
                path.toFile()
                    .listFiles(
                        (dir, filename) ->
                            filename.startsWith(IOUtils.HISTORY_PREFIX)
                                && filename.endsWith(IOUtils.HISTORY_EXTENSION))))
        .forEach(
            f -> {
              try {
                ChatHistory chatHistory = JSONUtils.readFromJSONFile(f, ChatHistory.class);
                addChat(chatHistory);
              } catch (IOException e) {
                LOGGER.error("Error while loading existing chats.", e);
              }
            });
    view.getChatsList().getListView().getSelectionModel().select(0);
  }

  public void initBroadcast() throws IOException {
    Path path = IOUtils.getBroadgast();
    if (Files.exists(path)) {
      ChatHistory chatHistory = JSONUtils.readFromJSONFile(path.toFile(), ChatHistory.class);
      addChat(chatHistory);
      LOGGER.debug("Loaded broadcast");
    } else {
      ChatHistory chatHistory = new ChatHistory(true);
      chatHistory.setUser("BROADCAST");
      addChat(chatHistory);
      LOGGER.debug("Created braodcast");
    }
  }

  private boolean addChat(String they) {
    if (!chatHashMap.containsKey(they)) {
      chatHashMap.put(they, new ChatView(they, theChatter).getChat());
      if (!view.getChatsList().getItems().contains(they)) {
        view.getChatsList().getItems().add(they);
        view.getChatsList().getListView().getSelectionModel().select(they);
      }
      return true;
    } else {
      return false;
    }
  }

  private void addChat(ChatHistory history) {
    if (!chatHashMap.containsKey(history.getUser())) {
      ChatView view = new ChatView(history.getUser(), theChatter);
      Chat chat = view.getChat();
      if(history.isBroadcast()){
        chat.setBroadcast();
        chat.setHistory(history);
      }
      chatHashMap.put(history.getUser(), chat);
    } else {
      // Not sure whether needed
      Chat chat = chatHashMap.get(history.getUser());
      if(history.isBroadcast()){
        chat.setBroadcast();
      }
      chat.setHistory(history);
    }
    view.getChatsList().getItems().add(history.getUser());
  }

  public void selectChat(String chat) {
    if (chatHashMap.containsKey(chat)) {
      LOGGER.debug("Selecting chat {}", chat);
      activeChat = chatHashMap.get(chat);
      view.setActiveChat(activeChat);
      chatHashMap.values().forEach(c -> c.getView().setActive(false));
      activeChat.getView().setActive(true);
    }
  }

  public void connectToNode(String other) {
    IPFSAdapter.getInstance()
        .getCachedIPFS()
        .ifPresent(
            ipfs -> {
              try {
                ipfs.swarm.connect(new MultiAddress(other));
              } catch (IOException e) {
                LOGGER.error("Couldn't connect to other.", e);
              }
            });
  }

  @SuppressWarnings("unchecked")
  public String[] getOwnAddresses() {
    Optional<IPFS> ipfs = IPFSAdapter.getInstance().getCachedIPFS();
    if (ipfs.isPresent()) {
      try {
        if (ipfs.get().id().containsKey("Addresses")) {
          return ((ArrayList<String>) ipfs.get().id().get("Addresses")).toArray(new String[0]);
        }
      } catch (IOException e) {
        LOGGER.error("Couldn't retrieve own id", e);
      }
      return null; // ipfs.get().id();
    } else {
      return null;
    }
  }

  public String getOwnNodeId() {
    Optional<IPFS> ipfs = IPFSAdapter.getInstance().getCachedIPFS();
    if (ipfs.isPresent()) {
      try {
        if (ipfs.get().id().containsKey("ID")) {
          return (String) ipfs.get().id().get("ID");
        }
      } catch (IOException e) {
        LOGGER.error("Couldn't retrieve own id", e);
      }
      return null; // ipfs.get().id();
    } else {
      return null;
    }
  }

  public void addKeyLocationFor(String they, String keyLocation) {
    // TODO Check if entry exists
    keyStore.add(they, keyLocation);
    keyStore.getEntry(they).ifPresent(activeChat::setKeystoreEntry);
    LOGGER.debug("Added {}/{} to the keystore", they, keyLocation);
  }

  public void sendFile(File f) {
    try {
      FileMessage m = fileExchange.publishFile(f);
      m.setSourceUsername(activeChat.getUs());
      m.setTargetUsername(activeChat.getThey());
      activeChat.send(m);
      activeChat.getView().getMessages().add(m);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
