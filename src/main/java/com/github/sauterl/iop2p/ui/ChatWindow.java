package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.io.Chatter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatWindow extends Application {

  VBox upperVBox;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    Chatter chatter = new Chatter(null, null);
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
            displayMessage(m);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  public void displayMessage(Message message) {

  }
}
