package com.github.sauterl.iop2p.ui;

import static com.github.sauterl.iop2p.Utils.connectAlert;

import com.github.sauterl.iop2p.ui.components.ModifiableListView;
import javafx.geometry.Orientation;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Write JavaDoc
 *
 * @author loris.sauter
 */
public class ChatWindow extends VBox {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChatWindow.class);
  public static final double INITIAL_DIVIDER_POSITION = 0.4;

  private ChatView activeChat;
  private VBox chatContainer;
  private ModifiableListView<String> list;

  private final ChatManager manager;
  private SplitPane splitPane;
  private MenuBar menuBar;

  public ChatWindow(){
    manager = new ChatManager(this);
    initComponents();
    layoutComponents();
  }

  public ChatView getActiveChat() {
    return activeChat;
  }

  public ModifiableListView<String> getChatsList() {
    return list;
  }

  /**
   * Initializes the UI components and performs configuration of them
   */
  private void initComponents(){
    splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.setDividerPositions(INITIAL_DIVIDER_POSITION);

    chatContainer = new VBox();

    list = new ModifiableListView<>("Chats");
    list.addHandler(manager);
    list.getListView().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      LOGGER.debug("Selected chat {}", newValue);
      manager.selectChat(newValue);
    });
    initMenu();
  }

  private void initMenu(){
    menuBar = new MenuBar();
    Menu menu = new Menu("Menu");
    menuBar.getMenus().add(menu);

    MenuItem connect = new MenuItem("Connect");
    menu.getItems().add(connect);

    connect.setOnAction(e -> {
      connectAlert();
    });
  }

  void selectChat(String chat){
    list.getListView().getSelectionModel().select(chat);
  }

  /**
   * Will setup the layout, e.g. the look and feel of this component
   */
  private void layoutComponents(){
    getChildren().addAll(menuBar, splitPane);

    splitPane.prefHeightProperty().bind(heightProperty());
    splitPane.prefWidthProperty().bind(widthProperty());

    splitPane.getItems().addAll(list, chatContainer);

    chatContainer.prefHeightProperty().bind(heightProperty());
  }

  public void setActiveChat(Chat chat){
    chatContainer.getChildren().clear();
    chatContainer.getChildren().add(chat.getView());
    chat.getView().prefHeightProperty().bind(chatContainer.heightProperty());
    chat.getView().scrollDown();
  }

  public ChatManager getManager() {
    return manager;
  }
}
