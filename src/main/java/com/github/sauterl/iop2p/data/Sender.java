package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ipfs.api.IPFS;

public class Sender {
  private IPFS ipfs;
  ObjectMapper om = new ObjectMapper();

  public Sender(IPFS ipfs) {
    this.ipfs = ipfs;
  }


  public void send(String username, String msg) throws Exception {
    Message message = new Message(msg);
    ipfs.pubsub.pub("/iop2pchat/inbox/" + username, om.writeValueAsString(message));
  }
}
