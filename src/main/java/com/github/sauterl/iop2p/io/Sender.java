package com.github.sauterl.iop2p.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sauterl.iop2p.data.Message;
import io.ipfs.api.IPFS;
import io.ipfs.api.IPFS.Pubsub;

public class Sender {

  private ObjectMapper om = new ObjectMapper();
  private Pubsub pubsub;


  public Sender(Pubsub pubsub) {
    this.pubsub = pubsub;
  }


  public void send(String username, String msg) throws Exception {
    Message message = new Message(msg);
    pubsub.pub("/iop2pchat/inbox/" + username, om.writeValueAsString(message));
  }
}
