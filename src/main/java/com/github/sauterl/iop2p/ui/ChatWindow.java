package com.github.sauterl.iop2p.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    VBox root = new VBox();
    BorderPane border = new BorderPane();
    Scene scene = new Scene(root, 300, 250);
    primaryStage.setScene(scene);

    // upper VBox
    VBox upperVBox = new VBox();
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
          String message = firstTF.getText();
          upperVBox.getChildren().add(new Label(message));
        });

  }
}
