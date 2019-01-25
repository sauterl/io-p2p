package com.github.sauterl.iop2p.ui;

import static com.github.sauterl.iop2p.Utils.connectAlert;
import static com.github.sauterl.iop2p.Utils.createAndShowKeyLocationDialog;

import com.github.sauterl.iop2p.Utils.UserCredentials;
import com.github.sauterl.iop2p.ui.components.ModifiableListView;
import java.io.File;
import javafx.geometry.Orientation;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Write JavaDoc
 *
 * @author loris.sauter
 */
public class ChatWindow extends VBox {

  public static final double INITIAL_DIVIDER_POSITION = 0.4;
  private static final Logger LOGGER = LoggerFactory.getLogger(ChatWindow.class);
  private final ChatManager manager;
  private VBox chatContainer;
  private ModifiableListView<String> list;
  private SplitPane splitPane;
  private MenuBar menuBar;

  public ChatWindow() {
    manager = new ChatManager(this);
    initComponents();
    layoutComponents();
  }

  public void setActiveChat(Chat chat) {
    chatContainer.getChildren().clear();
    chatContainer.getChildren().add(chat.getView());
    chat.getView().prefHeightProperty().bind(chatContainer.heightProperty());
    chat.getView().scrollDown();
  }

  public ModifiableListView<String> getChatsList() {
    return list;
  }

  /**
   * Initializes the UI components and performs configuration of them
   */
  private void initComponents() {
    splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(INITIAL_DIVIDER_POSITION);

    chatContainer = new VBox();

    list = new ModifiableListView<>("Chats");
    list.addHandler(manager);
    list.getListView()
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              LOGGER.debug("Selected chat {}", newValue);
              manager.selectChat(newValue);
            });
    initMenu();
  }
  // /ip4/ip/tcp/port/ipfs/partnerID

  private void initMenu() {
    menuBar = new MenuBar();
    Menu menu = new Menu("Menu");
    menuBar.getMenus().add(menu);

    MenuItem connect = new MenuItem("Connect");
    connect.setOnAction(
        e -> {
          Dialog<UserCredentials> dialog = connectAlert(manager.getOwnAddresses());
          if (dialog.getResult() != null) {

            String multiAddr =
                "/ip4/"
                    + dialog.getResult().getIp()
                    + "/tcp/"
                    + dialog.getResult().getPort()
                    + "/ipfs/"
                    + dialog.getResult().getId();
            LOGGER.debug(multiAddr);
            manager.connectToNode(multiAddr);
          }
        });

    MenuItem addKeysMenuItem = new MenuItem("Add Key for Active User");

    addKeysMenuItem.setOnAction(
        e -> {
          Chat active =
              ((ChatView)
                  chatContainer.getChildren().stream()
                      .filter(n -> n instanceof ChatView)
                      .findFirst()
                      .get())
                  .getChat();
          Dialog<String> dialog = createAndShowKeyLocationDialog(active.getThey());
          if (dialog.getResult() != null && !dialog.getResult().isEmpty()) {
            manager.addKeyLocationFor(active.getThey(), dialog.getResult());
          }
        });



    MenuItem fileTransfer = new MenuItem("File Transfer");

    fileTransfer.setOnAction(
        e -> {
          FileChooser fileChooser = new FileChooser();
          fileChooser.setTitle("Open Resource File");
          File f  = fileChooser.showOpenDialog(null);
          if(f != null){
            manager.sendFile(f);
          }
        });

    menu.getItems().addAll(connect, addKeysMenuItem, fileTransfer);

  }

  void selectChat(String chat) {
    list.getListView().getSelectionModel().select(chat);
  }

  /**
   * Will setup the layout, e.g. the look and feel of this component
   */
  private void layoutComponents() {
    getChildren().addAll(menuBar, splitPane);

    splitPane.prefHeightProperty().bind(heightProperty());
    splitPane.prefWidthProperty().bind(widthProperty());

    splitPane.getItems().addAll(list, chatContainer);

    chatContainer.prefHeightProperty().bind(heightProperty());
  }

  public ChatManager getManager() {
    return manager;
  }
}
