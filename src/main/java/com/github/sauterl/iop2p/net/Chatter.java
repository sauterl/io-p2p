package com.github.sauterl.iop2p.net;

import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.Message;
import io.ipfs.api.IPFS.Pubsub;
import java.util.function.Consumer;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Chatter {

  private Receiver receiver;
  private Sender sender;
  private Thread receiverThred;
  private Thread msgHandlerThread;

  private Consumer<Message> newMessageConsumer = null;

  public Chatter(String username, Pubsub pubsub) {
    receiver = new Receiver(Utils.getUsernameInboxTopic(username),pubsub);
    sender = new Sender(pubsub);
    receiverThred = new Thread(receiver);
    receiverThred.setName("ReceiverThread");
    msgHandlerThread = new Thread( () -> {
      while(true){
        if(hasNewMessageConsumer()){
          try {
            newMessageConsumer.accept(getNextMessage());
          } catch (InterruptedException e) {
            e.printStackTrace();
            break;
          }
        }
      }

    });
    msgHandlerThread.setName("MessageHandlerThread");
  }

  public Message send(String username, String msg) throws Exception {
    return sender.send(username, msg);
  }

  public void stop(){
    receiverThred.interrupt();
    msgHandlerThread.interrupt();
  }

  public void start(){
    receiverThred.start();
    msgHandlerThread.start();
  }

  /**
   * Should be blocking
   * @return
   * @throws InterruptedException
   */
  public Message getNextMessage() throws InterruptedException {
    return receiver.getNextMessage();
  }

  public void setOnMessageReceived(Consumer<Message> consumer){
    newMessageConsumer = consumer;
  }

  private boolean hasNewMessageConsumer(){
    return newMessageConsumer != null;
  }
}
