package com.github.sauterl.iop2p.ui;

import javafx.application.Application;

/**
 * TODO: Write JavaDoc
 *
 * @author loris.sauter
 */
public class LaunchHelper<T extends Application> {

  private Class<T> launchClass;

  private volatile boolean running = false;
  private Thread fxLauncher;

  public LaunchHelper(Class<T> launchClass) {
    this.launchClass = launchClass;
  }

  public synchronized void launch() {
    fxLauncher = new Thread(() -> {
      Thread.currentThread().setName("FXLauncher");
      running = true;
      Application.launch(launchClass);
      running = false;
    });
    fxLauncher.start();
  }

  public boolean isRunning() {
    return running;
  }

  public void stop() {
    fxLauncher.interrupt();
  }


}
