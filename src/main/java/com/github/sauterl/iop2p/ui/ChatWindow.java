package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.net.Chatter;
import com.github.sauterl.iop2p.ui.components.ModifiableListHandler;
import com.github.sauterl.iop2p.ui.components.ModifiableListView;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.AddEvent;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.RemoveEvent;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class ChatWindow extends Application implements ModifiableListHandler<String> {

  private ChatPanel cp;

  @Override
  public void start(Stage primaryStage) {
    // TODO: Cleanup of threading

    Chatter chatter =
        new Chatter(
            SimpleGuiCommand.getInstance().getUsername(),
            SimpleGuiCommand.getInstance().getIpfs().pubsub);
    chatter.start();
    // TODO
    cp = new ChatPanel(chatter, "bob");

    HBox root = new HBox();


    SplitPane splitter = new SplitPane();
    splitter.setOrientation(Orientation.HORIZONTAL);
    splitter.setDividerPositions(0.15);
    root.getChildren().add(splitter);
    splitter.prefWidthProperty().bind(root.widthProperty());
    splitter.prefHeightProperty().bind(root.heightProperty());
    cp.prefWidthProperty().bind(splitter.widthProperty().subtract(splitter.widthProperty().multiply(.15)));
    cp.prefHeightProperty().bind(splitter.heightProperty());

    ModifiableListView<String> choice = new ModifiableListView<>("Chats");
    choice.addHandler(this);

    splitter.getItems().addAll(choice, cp);
    Scene scene = new Scene(root, 500, 400);
    primaryStage.setScene(scene);

    primaryStage
        .getIcons()
        .addAll(new Image(ChatWindow.class.getClassLoader().getResourceAsStream("icon.png")));

    primaryStage.show();
    primaryStage.setOnCloseRequest(
        e -> {
          chatter.stop(); // not sure if this works
          try {
            cp.saveHistory();
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        });

  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0); // Cause IPFS daemon sub-process to halt
  }

  @Override
  public void onRemove(RemoveEvent<String> event) {

  }

  @Override
  public void onAdd(AddEvent<String> event) {
    TextInputDialog dialog = new TextInputDialog("Bob");

    dialog.setTitle("Chat");
    dialog.setHeaderText("Enter the name of your partner:");
    dialog.setContentText("Name:");

    Optional<String> result = dialog.showAndWait();

    result.ifPresent(name -> {
      try {
        cp.createNewChat(name);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}
