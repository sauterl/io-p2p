package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.JSONUtils;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.net.Chatter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatPanel extends VBox {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChatPanel.class);

  private VBox upperVBox;
  private final ObservableList<Message> messages;
  private ChatHistory chatHistory;
  private String partner;

  public ChatPanel(Chatter chatter, String partner) {
    this.partner = partner;
    LOGGER.debug("Savefile: {}", getSaveFile());
    chatHistory = loadOrCreateHistory();
    messages = FXCollections.observableList(chatHistory.getMessages());

    setMinHeight(50);
    setMinWidth(100);

    //BorderPane border = new BorderPane();

    // upper VBox
    upperVBox = new VBox();
    upperVBox.setPrefSize(300, 220);
    // scroll pane for all the sent & received messages
    ScrollPane scrollPane = new ScrollPane();
    // Setting a horizontal scroll bar
    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    // Setting vertical scroll bar
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setContent(upperVBox);

    getChildren().add(scrollPane);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    // lower VBox
    HBox lowerHBox = new HBox();
    // TextField to enter message
    TextField secondTF = new TextField();
    secondTF.setPromptText("Enter your message here");

    // SEND button
    Button b = new Button("SEND");

    // add in VBox that contains all the elements
    // lowerHBox.getChildren().add(firstTF);
    //HBox.setHgrow(secondTF, Priority.SOMETIMES);
    lowerHBox.getChildren().add(secondTF);
    lowerHBox.getChildren().add(b);
    getChildren().add(lowerHBox);

    b.setDefaultButton(true);
    b.setOnAction(
        event -> {
          String message = secondTF.getText();
          try {
            Message m = chatter.send(partner, message);
            messages.add(m);
            secondTF.clear();

          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    chatter.setOnMessageReceived(m -> Platform.runLater(() -> messages.add(m)));
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
    // add in VBox that contains all the elements
    //this.getChildren().addAll(scrollPane, lowerHBox);
    chatHistory.getMessages().forEach(m -> {displayMessage(m, m.getTargetUsername().equals(chatHistory.getUser()));} );
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
    JSONUtils.writeToJSONFile(chatHistory, new File((getSaveFile())));
  }

  public ChatHistory loadHistory() throws IOException {
    ChatHistory ch = JSONUtils.readFromJSONFile(new File((getSaveFile())), ChatHistory.class);
    LOGGER.debug("Loaded history: {}", JSONUtils.toJSON(ch));
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

    Label label = new Label(stringBuilder.toString());
    label.setWrapText(true);
    upperVBox.getChildren().add(label);
  }
}
