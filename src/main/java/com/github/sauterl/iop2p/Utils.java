package com.github.sauterl.iop2p;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Utils {

  private Utils() {
    // no instance needed
  }

  public static String getUsernameInboxTopic(String username) {
    return "/iop2pchat/inbox/" + username;
  }

  public static String readFileContents(String path) throws FileNotFoundException {
    StringBuilder sb = new StringBuilder();

    BufferedReader br = new BufferedReader(new FileReader(path));
    br.lines().forEach(sb::append);

    return sb.toString();
  }

  public static void writeFileContents(String path, String content) throws IOException {
    File f = Paths.get(path).getParent().toFile();
    f.mkdirs();
    BufferedWriter bw = new BufferedWriter(new FileWriter(path));
    bw.write(content);
    bw.flush();
    bw.close();
  }

  public static Shape createLeftSpeechBubble() {
    SVGPath svgPath = new SVGPath();
    svgPath.setFill(Color.TRANSPARENT);
    svgPath.setStroke(Color.BLACK);
    svgPath.setContent(
        "m 23.979133,62.016026 2.187523,-16.748167 c -2.769994,0 -4.99999,-2.229996 -4.99999,-4.99999 V 17.194931 c 0,-2.769995 2.229996,-4.99999 4.99999,-4.99999 h 75.989604 c 2.77,0 4.99999,2.229995 4.99999,4.99999 v 23.072938 c 0,2.769994 -2.22999,4.99999 -4.99999,4.99999 H 32.836682 Z");
    return svgPath;
  }

  public static Shape createRightSpeechBubble() {
    SVGPath svgPath = new SVGPath();
    svgPath.setFill(Color.TRANSPARENT);
    svgPath.setStroke(Color.BLACK);
    svgPath.setContent(
        "m 104.34378,62.016026 -2.18752,-16.748167 c 2.76999,0 4.99999,-2.229996 4.99999,-4.99999 V 17.194931 c 0,-2.769995 -2.23,-4.99999 -4.99999,-4.99999 H 26.166656 c -2.77,0 -4.99999,2.229995 -4.99999,4.99999 v 23.072938 c 0,2.769994 2.22999,4.99999 4.99999,4.99999 h 69.319578 z");
    return svgPath;
  }

  public static Dialog<UserCredentials> connectAlert(String[] addresses) {
    // Create login window
    Dialog<UserCredentials> dialog = new Dialog<>();
    dialog.setTitle("peer-text-peer");
    dialog.setHeaderText(" Welcome to our peer-text-peer chat!");

    // create Connect and Cancel button
    ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // Display own ID
    TextArea username = new TextArea();
    username.setPromptText("Your ID:");
    username.setEditable(false);

    StringBuilder stringBuilder = new StringBuilder();
    Arrays.stream(addresses).forEach(a -> {
      stringBuilder.append(a).append("\n");
    });
    username.setText(stringBuilder.toString());

    // Enter partner IP
    TextField ipPartner = new TextField();
    ipPartner.setPromptText("Enter your partner's IP:");

    // Enter port
    TextField numFieldPort = new TextField();
    numFieldPort.setPromptText("Enter your port:");
    // only numerical values in this field allowed!
    numFieldPort
        .textProperty()
        .addListener(
            new ChangeListener<String>() {
              @Override
              public void changed(
                  ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                  numFieldPort.setText(newValue.replaceAll("[^\\d]", ""));
                }
              }
            });

    // Enter partner ID
    TextField partnerID = new TextField();
    partnerID.setPromptText("Enter your partner's ID:");

    grid.add(new Label("These are your addresses:"), 0, 0);
    grid.add(username, 1, 0);
    grid.add(new Label("Enter your partner's IP:"), 0, 1);
    grid.add(ipPartner, 1, 1);
    grid.add(new Label("Enter your port:"), 0, 2);
    grid.add(numFieldPort, 1, 2);
    grid.add(new Label("Enter your partner's ID:"), 0, 3);
    grid.add(partnerID, 1, 3);

    dialog.getDialogPane().setContent(grid);

    Node connectButton = dialog.getDialogPane().lookupButton(connectButtonType);

    dialog.setResultConverter(
        connectBtn -> {
          if (connectBtn == connectButtonType) {
            if ((ipPartner.getText().isEmpty()
                || numFieldPort.getText().isEmpty())
                || partnerID.getText().isEmpty()) {
              return null;
            } else {
              return new UserCredentials(
                  ipPartner.getText(),
                  Integer.parseInt(numFieldPort.getText()),
                  partnerID.getText());
            }
          }
          return null;
        });

    dialog.showAndWait();

    return dialog;
  }

  public static Dialog<String> createAndShowKeyLocationDialog(String user) {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Add Key for " + user);
    dialog.setHeaderText("Add the folder containing the public key for user: " + user);

    // create Connect and Cancel button
    ButtonType connectButtonType = new ButtonType("Add", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // Display own ID
    TextArea keyLocation = new TextArea();
    keyLocation.setPromptText("Key Location");

    grid.add(new Label("Key Location:"), 0, 0);
    grid.add(keyLocation, 1, 0);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(
        connectBtn -> {
          if (connectBtn == connectButtonType) {
            if (!keyLocation.getText().isEmpty()) {
              return keyLocation.getText();
            }
          }
          return null;
        });

    dialog.showAndWait();

    return dialog;
  }

  public static class UserCredentials {

    private String ip;
    private int port;
    private String id;

    public UserCredentials(String ip, int port, String id) {
      this.ip = ip;
      this.port = port;
      this.id = id;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }
}
