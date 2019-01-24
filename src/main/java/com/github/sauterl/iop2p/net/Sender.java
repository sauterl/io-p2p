package com.github.sauterl.iop2p.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sauterl.iop2p.Utils;
import com.github.sauterl.iop2p.data.Message;
import io.ipfs.api.IPFS.Pubsub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sender {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

  private ObjectMapper om = new ObjectMapper();
  private Pubsub pubsub;

  public Sender(Pubsub pubsub) {
    this.pubsub = pubsub;
  }

  public void send(Message message) throws Exception {
    Object o = pubsub.pub(
        Utils.getUsernameInboxTopic(message.getTargetUsername()), om.writeValueAsString(message));
    LOGGER.debug("Sent: {}",o);
  }

  // send broadcast
  public void sendBroadcast(Message message) throws Exception {
    Object o =  pubsub.pub(Utils.getInboxTopicBroadcast(), om.writeValueAsString(message));
    LOGGER.debug("SentBroadcast: {}",o);
  }
}
