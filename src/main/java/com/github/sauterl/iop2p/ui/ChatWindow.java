package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.net.Chatter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ChatWindow extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    // TODO: Cleanup of threading

    Chatter chatter =
        new Chatter(
            SimpleGuiCommand.getInstance().getUsername(),
            SimpleGuiCommand.getInstance().getIpfs().pubsub);
    chatter.start();
    ChatPanel cp = new ChatPanel(chatter, "bob"); // TODO

    Scene scene = new Scene(cp, 500, 400);
    primaryStage.setScene(scene);

    primaryStage
        .getIcons()
        .addAll(new Image(ChatWindow.class.getClassLoader().getResourceAsStream("icon.png")));

    primaryStage.show();
    primaryStage.setOnCloseRequest(
        e -> {
          chatter.stop(); // not sure if this works
        });
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0); // Cause IPFS daemon sub-process to halt
  }
}
