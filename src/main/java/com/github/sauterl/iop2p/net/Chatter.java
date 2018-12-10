package com.github.sauterl.iop2p.net;

import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.Message;
import io.ipfs.api.IPFS.Pubsub;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Chatter {

  private Receiver receiver;
  private Sender sender;
  private Thread receiverThred;

  public Chatter(String username, Pubsub pubsub) {
    receiver = new Receiver(Utils.getUsernameInboxTopic(username),pubsub);
    sender = new Sender(pubsub);
    receiverThred = new Thread(receiver);
  }

  public Message send(String username, String msg) throws Exception {
    return sender.send(username, msg);
  }

  public void stop(){
    receiverThred.interrupt();
  }

  public void start(){
    receiverThred.start();
  }

  /**
   * Should be blocking
   * @return
   * @throws InterruptedException
   */
  public Message getNextMessage() throws InterruptedException {
    return receiver.getNextMessage();
  }
}
