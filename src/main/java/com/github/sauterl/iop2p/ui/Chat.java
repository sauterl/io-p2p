package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.crypto.KeyStore;
import com.github.sauterl.iop2p.crypto.KeyStore.Entry;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.net.Chatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Corresponding controller class of the ChatView.
 *
 * @author loris.sauter
 */
public class Chat {

  private final static Logger LOGGER = LoggerFactory.getLogger(Chat.class);

  private final String us;
  private final String they;
  private final Chatter chatter;
  private ChatHistory history;

  private KeyStore.Entry keystoreEntry;

  private boolean encrypted = false;
  private ChatView view;

  public Chat(final String they, final Chatter chatter) {
    this.us = chatter.getUsername();
    this.they = they;
    this.chatter = chatter;
    //chatter.setOnMessageReceived(this::receive);
    try {
      load();
    } catch (IOException e) {
      LOGGER.error("Error during history load", e);
      history = new ChatHistory(they);
    }
  }

  public Entry getKeystoreEntry() {
    return keystoreEntry;
  }

  public void setKeystoreEntry(Entry keystoreEntry) {
    this.keystoreEntry = keystoreEntry;
    encrypted = true;
  }

  public ChatHistory getHistory() {
    return history;
  }

  public void setHistory(ChatHistory history) {
    this.history = history;
    // TODO add check for 'they'

  }

  public ChatView getView() {
    return view;
  }

  public void setView(ChatView view) {
    this.view = view;
  }

  public String getUs() {
    return us;
  }

  public String getThey() {
    return they;
  }

  public void send(Message m) {
    LOGGER.debug("Security={}, sending: {}", encrypted, m);
    sendMessage(m);
  }

  private void sendMessage(Message m) {
    try {
      chatter.send(m);
      LOGGER.debug("Successfully send message {}", m);
    } catch (Exception e) {
      LOGGER.error("An error occurred during send.", e);
    }
  }

  public void receive(Message m) {
    LOGGER.debug("Receiving message {}", m);
    Platform.runLater(() -> {
      view.getMessages().add(m);
    });
  }

  public void load() throws IOException {
    Path p = Paths.get(IOUtils.getHistoryFile(they));
    if (Files.exists(p)) {
      history = IOUtils.loadHistory(they);
    } else {
      history = new ChatHistory(they);
    }
  }

  public void save() throws IOException {
    history.setLastSaved(System.currentTimeMillis());
    IOUtils.saveHistory(history);
  }
}
