package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.data.MessageType;
import com.github.sauterl.iop2p.net.Chatter;
import com.sandec.mdfx.MDFXNode;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ChatView is the view of the chat. It provides all necessary UI features for the chat.
 */
public class ChatView extends VBox {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatView.class);
  private final Chat chat;
  private VBox messagesBox;
  private ObservableList<Message> messages = FXCollections.observableArrayList();
  private Button sendBtn;
  private TextField inputTextfield;


  public ChatView(String they, Chatter chatter) {
    this( new Chat(they, chatter));

  }

  public ChatView(Chat chat){
    this.chat = chat;
    chat.setView(this);
    initComponents();
    layoutComponents();

    initMessageHandling();
    initInteraction();

    displayAllMessages();
  }

  private void displayAllMessages() {
    messages.forEach(this::displayChatMessage);
  }

  private void initInteraction() {
    sendBtn.setOnAction(event -> {
      String message = inputTextfield.getText();
      inputTextfield.clear();
      Message m = new Message();
      m.setSourceUsername(chat.getUs());
      m.setTargetUsername(chat.getThey());
      m.setTimestamp(System.currentTimeMillis());
      m.setPayload(message);
      messages.add(m);
    });
  }

  private void initMessageHandling() {
    messages = FXCollections.observableList(chat.getHistory().getMessages());
    messages.addListener((ListChangeListener<? super Message>) c -> {
      if (!c.next()) {
        return;
      }
      if (c.wasAdded()) {
        c.getAddedSubList().forEach(this::displayChatMessage);
      } else {
        LOGGER.error("Unexpected state. Change on messages: {}", c);
      }
      try {
        chat.save();
      } catch (IOException e) {
        LOGGER.error("Error during save of history", e);
      }
    });
  }

  /**
   * Will setup the layout, e.g. the look and feel of this component
   */
  private void layoutComponents() {
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setContent(messagesBox);

    HBox inputContainer = new HBox();
    inputContainer.getChildren().addAll(inputTextfield, sendBtn);
    HBox.setHgrow(inputTextfield, Priority.ALWAYS);

    inputContainer.prefWidthProperty().bind(widthProperty());
    scrollPane.prefWidthProperty().bind(widthProperty());
    scrollPane.prefHeightProperty()
        .bind(heightProperty().subtract(inputContainer.prefHeightProperty()));

    getChildren().addAll(scrollPane,inputContainer);
  }

  /**
   * Initializes the UI components and performs configuration of them
   */
  private void initComponents() {
    messagesBox = new VBox();
    inputTextfield = new TextField();
    inputTextfield.setPromptText("Enter your message here");
    sendBtn = new Button("Send"); // TODO Iconize
    sendBtn.setDefaultButton(true);


  }

  private void displayChatMessage(Message message) {
    if (message.getType() == MessageType.PLAIN) {
      messagesBox.getChildren().add(createSpeechBubbleDisplay(message));
    } else {
      // TODO switch on type
    }
  }

  private Node createMessageDisplay(Message message) {
    // extract timestamp
    boolean self = chat.getUs().equals(message.getSourceUsername());

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
// For markdown rendering
    getStylesheets().add("/com/sandec/mdfx/mdfx-default.css");
    return new MDFXNode(stringBuilder.toString());
  }

  private Node createSpeechBubbleDisplay(Message message) {
    // extract timestamp
    boolean self = chat.getUs().equals(message.getSourceUsername());

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

    AnchorPane pane = new AnchorPane();
    Label dateLbl = new Label(time);
    Label msgLbl = new Label(message.getPayload());
    msgLbl.setWrapText(true);
    Label whoLbl = new Label(usernameFrom);
    Node bubble = self ? Utils.createRightSpeechBubble() : Utils.createLeftSpeechBubble();

    double topFirst = 10;
    double topSecond = 15;
    double edgeFirst = 10;
    double edgeSecond = 15;

    AnchorPane.setTopAnchor(dateLbl, topFirst);
    AnchorPane.setTopAnchor(msgLbl, topSecond);
    AnchorPane.setTopAnchor(bubble, topFirst);
    AnchorPane.setBottomAnchor(bubble, topFirst);
    AnchorPane.setBottomAnchor(whoLbl, 0d);

    if(self){
      AnchorPane.setRightAnchor(dateLbl, edgeSecond);
      AnchorPane.setRightAnchor(msgLbl, edgeSecond);
      AnchorPane.setRightAnchor(bubble, edgeFirst);
      AnchorPane.setRightAnchor(whoLbl, edgeFirst);
      pane.getChildren().addAll(dateLbl,msgLbl,bubble);
    }else{
      AnchorPane.setLeftAnchor(dateLbl, edgeSecond);
      AnchorPane.setLeftAnchor(msgLbl, edgeSecond);
      AnchorPane.setLeftAnchor(bubble, edgeFirst);
      AnchorPane.setLeftAnchor(whoLbl, edgeFirst);
      pane.getChildren().addAll(dateLbl,msgLbl,whoLbl,bubble);
    }

    HBox wrapper = new HBox();
    wrapper.getChildren().add(pane);
    HBox.setHgrow(pane, Priority.ALWAYS);
    pane.prefWidthProperty().bind(messagesBox.widthProperty());
    return wrapper;
  }


  private Node createSpeechBubbleDisplayV2(Message message) {
    // extract timestamp
    boolean self = chat.getUs().equals(message.getSourceUsername());

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

    AnchorPane pane = new AnchorPane();
    Label dateLbl = new Label(time);
    Label msgLbl = new Label(message.getPayload());
    msgLbl.setWrapText(true);
    Label whoLbl = new Label(usernameFrom);
    Shape bubble = self ? Utils.createRightSpeechBubble() : Utils.createLeftSpeechBubble();

    msgLbl.shapeProperty().set(bubble);

    AnchorPane.setTopAnchor(dateLbl, 20d);
    AnchorPane.setTopAnchor(msgLbl, 30d);
    AnchorPane.setBottomAnchor(whoLbl, 0d);

    if(self){
      AnchorPane.setRightAnchor(dateLbl, 40d);
      AnchorPane.setRightAnchor(msgLbl, 40d);
      AnchorPane.setRightAnchor(whoLbl, 20d);
      pane.getChildren().addAll(dateLbl,msgLbl);
    }else{
      AnchorPane.setLeftAnchor(dateLbl, 40d);
      AnchorPane.setLeftAnchor(msgLbl, 40d);
      AnchorPane.setLeftAnchor(whoLbl, 20d);
      pane.getChildren().addAll(dateLbl,msgLbl,whoLbl);
    }

    pane.prefWidthProperty().bind(messagesBox.widthProperty());
    return pane;
  }

  public void clear() {
    messages.clear();
    messagesBox.getChildren().clear();
  }

  ObservableList<Message> getMessages() {
    return messages;
  }

  public Chat getChat(){
    return chat;
  }

}
