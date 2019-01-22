package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.JSONUtils;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.net.Chatter;
import com.github.sauterl.iop2p.ui.components.ModifiableListHandler;
import com.github.sauterl.iop2p.ui.components.ModifiableListView;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.AddEvent;
import com.github.sauterl.iop2p.ui.components.ModifiableListView.RemoveEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated USe P2pChatApplication instead
 */
@Deprecated
public class ChatAppl extends Application implements ModifiableListHandler<String> {

  public static final double DIVIDERRATIO = 0.4;
  private ChatPanel cp;
  private ModifiableListView<String> choice;

  private static final Logger LOGGER = LoggerFactory.getLogger("Application");



  @Override
  public void start(Stage primaryStage) {
    // TODO: Cleanup of threading

    Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOGGER.error("ERROR!", e));


    Chatter chatter =
        new Chatter(
            SimpleGuiCommand.getInstance().getUsername(),
            SimpleGuiCommand.getInstance().getIpfs().pubsub);
    chatter.start();
    // TODO
    cp = new ChatPanel(chatter);

    HBox root = new HBox();

    SplitPane splitter = new SplitPane();
    splitter.setOrientation(Orientation.HORIZONTAL);
    splitter.setDividerPositions(DIVIDERRATIO);
    root.getChildren().add(splitter);
    splitter.prefWidthProperty().bind(root.widthProperty());
    splitter.prefHeightProperty().bind(root.heightProperty());
    cp.prefWidthProperty()
        .bind(splitter.widthProperty().subtract(splitter.widthProperty().multiply(DIVIDERRATIO)));
    cp.prefHeightProperty().bind(splitter.heightProperty());

    choice = new ModifiableListView<>("Chats");
    cp.setChatPartners(choice);
    choice.addHandler(this);
    splitter.getItems().addAll(choice, cp);



    choice
        .getListView()
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              LOGGER.debug("Selection made: "+newValue);
              try {
                cp.createNewChat(newValue);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });

    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);

    primaryStage
        .getIcons()
        .addAll(new Image(ChatAppl.class.getClassLoader().getResourceAsStream("icon.png")));

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

    loadChatsOnStart();

    splitter.getItems().add(1, new ChatView("bob", chatter));
  }

  public void loadChatsOnStart() {
    Path path = Paths.get(IOUtils.getDirectory());
    for (File f : Objects.requireNonNull(path.toFile().listFiles(
        (dir, filename) -> filename.startsWith("history-") && filename.endsWith("json")))) {
      try {
        ChatHistory chat = JSONUtils.readFromJSONFile(f, ChatHistory.class);
      choice.getItems().add(chat.getUser());

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    choice.getListView().getSelectionModel().select(0);
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0); // Cause IPFS daemon sub-process to halt
  }

  @Override
  public void onRemove(RemoveEvent<String> event) {
    choice.getItems().remove(event.getSelectedIndex());
    String username = event.getSelected();
    try {
      cp.deleteHistory(username);
    } catch (IOException e) {
      e.printStackTrace();
    }
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
          try {
            cp.createNewChat(name);
            choice.getItems().add(name);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }
}
