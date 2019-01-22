package com.github.sauterl.iop2p.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.JSONUtils;
import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.net.Chatter;
import com.github.sauterl.iop2p.ui.components.ModifiableListView;
import com.sandec.mdfx.MDFXNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated  Use ChatView instead
 */
@Deprecated
public class ChatPanel extends VBox {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChatPanel.class);

  private VBox upperVBox;
  private ObservableList<Message> messages = null;
  private ChatHistory chatHistory;
  private String partner;
  private final Button sendBtn;
  private final TextField msgInputTF;
  private Chatter chatter;

  private ModifiableListView<String>  chatPartners;

  public void setChatPartners(ModifiableListView<String> chatPartners) {
    this.chatPartners = chatPartners;
  }

  public ChatPanel(Chatter chatter) {
    this.partner = null;
    this.chatter = chatter;

    //chatHistory = loadOrCreateHistory();
    //

    // setMinHeight(50);
    // setMinWidth(100);

    // BorderPane border = new BorderPane();

    // upper VBox
    upperVBox = new VBox();
    // scroll pane for all the sent & received messages
    ScrollPane scrollPane = new ScrollPane();
    // Setting a horizontal scroll bar
    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    // Setting vertical scroll bar
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setContent(upperVBox);

    scrollPane.prefWidthProperty().bind(widthProperty());

    getChildren().add(scrollPane);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    // lower VBox
    HBox lowerHBox = new HBox();
    // TextField to enter message
    msgInputTF = new TextField();
    msgInputTF.setPromptText("Enter your message here");

    // SEND button
    sendBtn = new Button("SEND");

    // add in VBox that contains all the elements
    // lowerHBox.getChildren().add(firstTF);
    HBox.setHgrow(msgInputTF, Priority.ALWAYS);
    lowerHBox.getChildren().add(msgInputTF);
    lowerHBox.getChildren().add(sendBtn);
    getChildren().add(lowerHBox);

    lowerHBox.prefWidthProperty().bind(widthProperty());
    sendBtn.setDefaultButton(true);

    // add in VBox that contains all the elements
    // this.getChildren().addAll(scrollPane, lowerHBox);
    // displayChatHistory();
  }

  private void displayChatHistory() {
    chatHistory
        .getMessages()
        .forEach(
            m -> {
              displayMessage(m, m.getTargetUsername().equals(chatHistory.getUser()));
            });
  }

  private void initMessageHandling() {
    messages = FXCollections.observableList(chatHistory.getMessages());
    sendBtn.setOnAction(
        event -> {
          String message = msgInputTF.getText();
          try {
            Message m = chatter.send(partner, message);
            messages.add(m);
            msgInputTF.clear();
            saveHistory();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    chatter.setOnMessageReceived(this::handleIncomingMessage);
    messages.addListener(
        (ListChangeListener<Message>)
            c -> {
              if (!c.next()) {
                return;
              }
              if (c.wasAdded()) {
                displayMessage(
                    c.getAddedSubList().get(0), false); // should only be a single added message
              } else {
                System.out.println("Received something strange: " + c);
              }
            });
  }

  private void removeOldChat() {
    upperVBox.getChildren().clear();
  }

  public void createNewChat(String newPartner) throws IOException {
    saveHistory();
    removeOldChat();
    partner = newPartner;
    LOGGER.debug("Savefile: {}", getSaveFile());
    chatHistory = loadOrCreateHistory();
    initMessageHandling();
    displayChatHistory();
  }

  private ChatHistory loadOrCreateHistory() {
    if (Files.exists(Paths.get(getSaveFile()))) {
      LOGGER.debug("Save file exists");
      try {
        return loadHistory();
      } catch (IOException e) {
        LOGGER.error("Couldn't load savefile", e);
        return new ChatHistory(partner);
      }
    } else {
      LOGGER.debug("Creating new history");
      return new ChatHistory(partner);
    }
  }

  public String getSaveFile() {
    return IOUtils.getDirectory() + "/history-" + partner + ".json";
  }

  public void saveHistory() throws IOException {
    if (partner != null) {

      JSONUtils.writeToJSONFile(chatHistory, new File((getSaveFile())));
    }
  }

  public void deleteHistory(String username) throws IOException {
    Path path = Paths.get(IOUtils.getDirectory(), "history-" + username + ".json");
    Files.delete(path);
  }

  public ChatHistory loadHistory() throws IOException {
    ChatHistory ch = JSONUtils.readFromJSONFile(new File((getSaveFile())), ChatHistory.class);
    LOGGER.trace("Loaded history: {}", JSONUtils.toJSON(ch));
    return ch;
  }

  private void displayMessage(Message message, boolean self) {
    // extract timestamp
    StringBuilder stringBuilder = new StringBuilder();
    String pattern = "dd MMM yy, HH:mm";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    String time = simpleDateFormat.format(new Date(message.getTimestamp()));

    // extract username
    String usernameFrom = message.getSourceUsername();
    String usernameTo = message.getTargetUsername();
    if (self) {
      String you = " (you)";
      usernameFrom = usernameFrom + you;
    }

    // extract message
    String messageText = message.getPayload();
    // fill StringBuilder
    stringBuilder
        .append("[")
        .append(time)
        .append("]")
        .append(": ")
        .append(usernameFrom)
        .append(" to ")
        .append(usernameTo)
        .append(": ")
        .append(messageText);

    MDFXNode mdfx = new MDFXNode(stringBuilder.toString());
    getParent().getStylesheets().add("/com/sandec/mdfx/mdfx-default.css");
    // mdfx.setWrapText(true);
    upperVBox.getChildren().add(mdfx);
  }

  private Region createSpeechBubble(String msg, boolean self){
    StackPane st = new StackPane();
    Label l = new Label(msg);
    l.setWrapText(true);
    if(self){
      st.getChildren().add(Utils.createRightSpeechBubble());
    }else{
      st.getChildren().add(Utils.createLeftSpeechBubble());
    }
    st.getChildren().add(l);
    return st;
  }

  private void handleIncomingMessage(Message m){
    // TODO Handle msg type
    LOGGER.debug("Incoming: {}",m);
    if(m.getTargetUsername().equals(chatter.getUsername())){
      if(partner.equals(m.getSourceUsername())){
        // all good.
        handleMessage(m);
      }else{
        // Not good: Not this chat
        if(chatPartners.getItems().contains(m.getSourceUsername())){
          chatPartners.getListView().getSelectionModel().select(m.getSourceUsername());
          handleMessage(m); // not sure whether working correctly or not
        }else{
          chatPartners.getListView().getItems().add(m.getSourceUsername());
          chatPartners.getListView().getSelectionModel().select(m.getSourceUsername());
          handleMessage(m);
        }
      }
    }else{
      LOGGER.warn("Received message not for us: {}", m);
    }

  }

  private void handleMessage(Message m) {
    Platform.runLater(() -> {
      messages.add(m);
      try {
        LOGGER.debug("Received message: {}", JSONUtils.toJSON(m));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    });
  }
}
