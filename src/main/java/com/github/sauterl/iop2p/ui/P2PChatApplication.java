package com.github.sauterl.iop2p.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Write JavaDoc
 *
 * @author loris.sauter
 */
public class P2PChatApplication extends Application {

  public static final Logger LOGGER = LoggerFactory.getLogger(P2PChatApplication.class);

  @Override
  public void start(Stage primaryStage) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);

    ChatWindow window = new ChatWindow();
    primaryStage.setScene(new Scene(window, 800,600));
    primaryStage.setTitle("peer-text-peer -- IPFS based Chat");

    window.getManager().loadExisitngChats();

    primaryStage.show();
    primaryStage.setOnCloseRequest(event -> {
      window.getManager().stop();
    });
  }

  private void handleUncaughtException(Thread thread, Throwable throwable) {
    LOGGER.error("Uncaught Exception.",throwable);
  }
}
