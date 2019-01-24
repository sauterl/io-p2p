package com.github.sauterl.iop2p.net;

import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.Message;
import io.ipfs.api.IPFS.Pubsub;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Chatter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Chatter.class);

  private Receiver receiver;
  private Sender sender;
  private Thread receiverThred;
  private Thread msgHandlerThread;
  private String username;

  private Consumer<Message> newMessageConsumer = null;

  public Chatter(String username, Pubsub pubsub, boolean broadcaster) {
    this.username = username;
    if (broadcaster) {
      receiver = new Receiver(Utils.getInboxTopicBroadcast(), pubsub);
    } else {
      receiver = new Receiver(Utils.getUsernameInboxTopic(username), pubsub);
    }
    sender = new Sender(pubsub);
    receiverThred = new Thread(receiver);
    receiverThred.setName("ReceiverThread");
    msgHandlerThread =
        new Thread(
            () -> {
              LOGGER.debug("MessageHandlerThread running...");
              while (true) {
                if (hasNewMessageConsumer()) {
                  try {
                    LOGGER.debug("Waiting for message");
                    Message m = getNextMessage();
                    LOGGER.debug("Msg: {}", m);
                    if (!broadcaster || !m.getSourceUsername().equals(username)) {
                      newMessageConsumer.accept(m);
                    }
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                  }
                } else {
                  if (System.currentTimeMillis() % 1000 == 0) {
                    LOGGER.debug("Consumer: {}", hasNewMessageConsumer());
                  }
                }
              }
            });
    msgHandlerThread.setName("MessageHandlerThread");
  }

  public Message send(String username, String msg) throws Exception {
    Message message = new Message(msg);
    message.setTargetUsername(username);
    message.setSourceUsername(this.username);
    sender.send(message);
    return message;
  }

  public void send(Message m) throws Exception {
    sender.send(m);
  }

  public void stop() {
    receiverThred.interrupt();
    msgHandlerThread.interrupt();
  }

  public void start() {
    receiverThred.start();
    msgHandlerThread.start();
  }

  /** Should be blocking */
  public Message getNextMessage() throws InterruptedException {
    return receiver.getNextMessage();
  }

  public void setOnMessageReceived(Consumer<Message> consumer) {
    LOGGER.debug("Setting consumer: {}", consumer);
    newMessageConsumer = consumer;
  }

  private boolean hasNewMessageConsumer() {
    return newMessageConsumer != null;
  }

  public String getUsername() {
    return username;
  }

  public void sendBroadcast(Message m) throws Exception {
    sender.sendBroadcast(m);
  }
}
