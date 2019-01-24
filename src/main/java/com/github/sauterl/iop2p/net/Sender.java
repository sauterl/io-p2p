package com.github.sauterl.iop2p.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.Message;
import io.ipfs.api.IPFS.Pubsub;

public class Sender {

  private ObjectMapper om = new ObjectMapper();
  private Pubsub pubsub;

  public Sender(Pubsub pubsub) {
    this.pubsub = pubsub;
  }

  public void send(Message message) throws Exception {
    pubsub.pub(
        Utils.getUsernameInboxTopic(message.getTargetUsername()), om.writeValueAsString(message));
  }

  // send broadcast
  public void sendBroadcast(Message message) throws Exception {
    pubsub.pub(Utils.getInboxTopicBroadcast(), om.writeValueAsString(message));
  }
}
