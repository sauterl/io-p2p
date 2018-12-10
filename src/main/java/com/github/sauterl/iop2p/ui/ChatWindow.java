package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.net.Chatter;
import io.ipfs.api.IPFS;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatWindow extends Application {
  // TODO Cleanup: Separate UI and logice more

  private VBox upperVBox;
  private ObservableList<Message> messages = FXCollections.observableArrayList();

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    // TODO Refactor: use airline for cli-arg parsing
    Chatter chatter = new Chatter(getParameters().getRaw().get(1), new IPFS(getParameters().getRaw().get(0)).pubsub);
    chatter.setOnMessageReceived(m -> messages.add(m));
    messages.addListener((ListChangeListener<Message>) c -> {
      if(c.wasAdded()){
        displayMessage(c.getAddedSubList().get(0), false); // should only be a single added message
      }else{
        System.out.println("Received something strange: "+c);
      }
    });
    chatter.start();
    primaryStage.setOnCloseRequest(e -> {
      chatter.stop(); // not sure if this works
    });
    VBox root = new VBox();
    BorderPane border = new BorderPane();
    Scene scene = new Scene(root, 300, 250);
    primaryStage.setScene(scene);

    // upper VBox
    upperVBox = new VBox();
    border.setCenter(upperVBox);
    upperVBox.setPrefSize(300, 220);
    // scroll pane for all the sent & received messages
    ScrollPane scrollPane = new ScrollPane();
    // Setting a horizontal scroll bar
    scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    // Setting vertical scroll bar
    scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    scrollPane.setContent(upperVBox);
    // add in VBox that contains all the elements
    root.getChildren().add(scrollPane);

    // lower VBox
    HBox lowerHBox = new HBox();
    border.setBottom(lowerHBox);
    // TextField to enter name of reciever
    TextField firstTF = new TextField();
    // TextField to enter message
    TextField secondTF = new TextField();

    // SEND button
    Button b = new Button("SEND");

    // add in VBox that contains all the elements
    lowerHBox.getChildren().add(firstTF);
    lowerHBox.getChildren().add(secondTF);
    lowerHBox.getChildren().add(b);
    root.getChildren().add(lowerHBox);

    primaryStage.show();
    b.setDefaultButton(true);
    b.setOnAction(
        event -> {
          String username = firstTF.getText();
          String message = secondTF.getText();
          try {
            Message m = chatter.send(username, message);
            displayMessage(m, true);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  private void displayMessage(Message message, boolean self) {
    // extract timestamp
    StringBuilder stringBuilder = new StringBuilder();
    String pattern = "dd-MM-yyyy HH:mm:ssZ";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    String time = simpleDateFormat.format(new Date(message.getTimestamp()));

    // extract username
    String username = message.getSourceUsername();
    if (self) {
      String you = " (you)";
      username = username + you;
    }

    // extract message
    String messageText = message.getPayload();
    stringBuilder.append("[").append(time).append("]");

    // fill StringBuilder
    stringBuilder
        .append("[")
        .append(time)
        .append("]")
        .append(": ")
        .append(username)
        .append(": ")
        .append(messageText);

    upperVBox.getChildren().add(new Label(stringBuilder.toString()));
  }
}
