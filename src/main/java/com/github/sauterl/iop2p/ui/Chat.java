package com.github.sauterl.iop2p.ui;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.crypto.KeyStore;
import com.github.sauterl.iop2p.crypto.KeyStore.Entry;
import com.github.sauterl.iop2p.crypto.RsaProcessor;
import com.github.sauterl.iop2p.data.ChatHistory;
import com.github.sauterl.iop2p.data.EncryptedMessage;
import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.data.MessageType;
import com.github.sauterl.iop2p.net.Chatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.application.Platform;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Corresponding controller class of the ChatView.
 *
 * @author loris.sauter
 */
public class Chat {

  private static final Logger LOGGER = LoggerFactory.getLogger(Chat.class);

  private final String us;
  private final String they;
  private final Chatter chatter;
  private ChatHistory history;

  private KeyStore.Entry keystoreEntry;

  private boolean encrypted = false;
  private ChatView view;
  private RsaProcessor security;

  public Chat(final String they, final Chatter chatter) {
    this.us = chatter.getUsername();
    this.they = they;
    this.chatter = chatter;
    // chatter.setOnMessageReceived(this::receive);
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
    LOGGER.debug("Enabling security {}<->{}", us, they);
    this.keystoreEntry = keystoreEntry;
    encrypted = true;
    try {
      security =
          new RsaProcessor(IOUtils.getOurKeyLocation().toString(), keystoreEntry.getKeyLocation());
    } catch (IOException e) {
      LOGGER.error("Coudln't crate security", e);
    }
  }

  public ChatHistory getHistory() {
    return history;
  }

  public void setHistory(ChatHistory history) {
    this.history = history;
    // TODO add check for 'they'
    view.initMessageHandling();

  }

  public void setBroadcast() {
    history.setBroadcast(true);
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

  public Message send(Message m) {
    LOGGER.debug("Security={}, sending: {}", encrypted, m);
    if(m.getType() == MessageType.FILE){
      return sendMessage(m);
    }
    if (encrypted) {
      return sendEndrypted(m);
    } else {
      return sendMessage(m);
    }
  }

  public Message sendEndrypted(Message m) {
    LOGGER.debug("Sending encrypted msg");
    try {
      EncryptedMessage encryptedMessage = security.encrypt(m);
      sendMessage(encryptedMessage);
      return encryptedMessage;
    } catch (InvalidCipherTextException e) {
      LOGGER.error("Couldn't encrypt message. WILL NOT SEND IT", m);
    }
    return m;
  }

  private Message sendMessage(Message m) {
    try {
      if(history.isBroadcast()){
        chatter.sendBroadcast(m);
      }else{
        chatter.send(m);
      }
      LOGGER.debug("Successfully send message {}", m);
    } catch (Exception e) {
      LOGGER.error("An error occurred during send.", e);
    }
    return m;
  }

  public Optional<Message> decrypt(EncryptedMessage m) {
    try {
      return Optional.of(security.decrypt(m));
    } catch (InvalidCipherTextException e) {
      LOGGER.error("Couldn't decrypt the message", e);
      return Optional.empty();
    }
  }

  public void receive(Message m) {
    LOGGER.debug("Receiving message {}", m);
    Platform.runLater(
        () -> {

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
    if (history.isBroadcast()) {
      LOGGER.debug("Saving broadcast");
      IOUtils.saveBroadcastHistory(history);
    } else {
      LOGGER.debug("Saving history");
      IOUtils.saveHistory(history);
    }
  }

  public boolean isBroadcaster() {
    return history.isBroadcast();
  }
}
