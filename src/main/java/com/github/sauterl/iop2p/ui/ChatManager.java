package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.JSONUtils;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.net.Chatter;
import com.github.sauterl.iop2p.ui.components.ModifiableListHandler;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.AddEvent;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.RemoveEvent;
import java.io.IOException;
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
 * General managing instance of the application ui
 *
 * @author loris.sauter
 */
public class ChatManager implements ModifiableListHandler<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatManager.class);

  private final Chatter theChatter;
  private ArrayList<String> others = new ArrayList<>();
  private ChatWindow view;

  private HashMap<String, Chat> chatHashMap = new HashMap<>();

  private Chat activeChat;

  public ChatManager(ChatWindow chatWindow) {
    view = chatWindow;
    theChatter = new Chatter(SimpleGuiCommand.getInstance().getUsername(),
        SimpleGuiCommand.getInstance().getIpfs().pubsub);
    theChatter.start();
    theChatter.setOnMessageReceived(this::handleIncomingMessage);
  }

  private void handleIncomingMessage(Message m){
    LOGGER.debug("Incoming message: {}",m);
    if(!m.getTargetUsername().equals(theChatter.getUsername())){
      // ignoring those chats not for us
      return;
    }
    if(m.getSourceUsername().equals(activeChat.getThey())){
      activeChat.receive(m);
    }else if(chatHashMap.containsKey(m.getSourceUsername())) {
      LOGGER.debug("Will switch active chat");
      Platform.runLater(() -> {
        chatHashMap.get(m.getSourceUsername()).receive(m);
        view.selectChat(m.getSourceUsername());
      });
    }else{
      LOGGER.debug("Will create new chat");

      Platform.runLater(() -> {
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
    alert.setHeaderText("Delete chat with "+they);
    alert.setContentText("Are you sure you want to delete the chat with "+they+"\n "
        + "Deletion cannot be undone and may cause loss of data");
    Optional<ButtonType> out = alert.showAndWait();
    out.ifPresent(buttonType -> {
      if(buttonType.equals(ButtonType.OK)){
        view.getChatsList().getItems().remove(event.getSelectedIndex());
        try {
          IOUtils.deleteHistory(they);
        } catch (IOException e) {
          LOGGER.error("Error while deleting history for "+they, e);
        }
      }else{
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
          if(!chatHashMap.containsKey(name)){

          }else{
            LOGGER.warn("For user {} already exists a chat", name);
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Duplicate Chat");
            alert.setHeaderText("Chat addressee already in list");
            alert.setContentText("Cannot have two chats to the same addressee");
            alert.showAndWait();
          }
          view.getChatsList().getItems().add(name);
        });
  }

  public void stop(){
    theChatter.stop();
    System.exit(0);
  }

  public ArrayList<String> getOthers() {
    return others;
  }

  public void loadExisitngChats() {
    Path path = Paths.get(IOUtils.getDirectory());
    Arrays.stream(Objects.requireNonNull(path.toFile().listFiles((dir,filename)->filename.startsWith(IOUtils.HISTORY_PREFIX) && filename.endsWith(IOUtils.HISTORY_EXTENSION)))).forEach(f -> {
      try {
        ChatHistory chatHistory = JSONUtils.readFromJSONFile(f, ChatHistory.class);
        addChat(chatHistory);
      } catch (IOException e) {
        LOGGER.error("Error while loading existing chats.", e);
      }
    });
    view.getChatsList().getListView().getSelectionModel().select(0);
  }

  private boolean addChat(String they){
    if(!chatHashMap.containsKey(they)){
      chatHashMap.put(they, new ChatView(they, theChatter).getChat());
      if(!view.getChatsList().getItems().contains(they)){
        view.getChatsList().getItems().add(they);
      }
      return true;
    }else{
      return false;
    }
  }

  private void addChat(ChatHistory history){
    if(!chatHashMap.containsKey(history.getUser())){
      chatHashMap.put(history.getUser(), new ChatView(history.getUser(), theChatter).getChat());
    }else{
      // Not sure whether needed
      Chat chat = chatHashMap.get(history.getUser());
      chat.setHistory(history);
    }
    view.getChatsList().getItems().add(history.getUser());
  }

  public void selectChat(String chat) {
    if(chatHashMap.containsKey(chat)){
      LOGGER.debug("Selecting chat {}", chat);
      activeChat = chatHashMap.get(chat);
      view.setActiveChat(activeChat);
    }
  }

}
