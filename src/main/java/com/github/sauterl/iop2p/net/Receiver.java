package com.github.sauterl.iop2p.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sauterl.iop2p.data.Message;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.ipfs.api.IPFS;
import io.ipfs.api.IPFS.Pubsub;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Receiver implements Runnable {

  private String topic;
  private Pubsub pubsub;
  private ObjectMapper om = new ObjectMapper();
  private BlockingQueue<Message> messages = new ArrayBlockingQueue<>(1000);

  public Receiver(String topic, Pubsub pubsub) {
    this.topic = topic;
    this.pubsub = pubsub;
  }

  @Override
  public void run() {
    try {
      pubsub
          .sub(topic)
          .forEach(
              msg -> {
                try {
                  String rawMsg = parseRaw((String) msg.get("data"));
                  Message actual = parse(rawMsg);
                  System.out.println(actual.getPayload());
                  messages.add(actual);
                } catch (Base64DecodingException | IOException e) {
                  e.printStackTrace();
                }
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String parseRaw(String data) throws Base64DecodingException {
    return new String(Base64.decode(data));
  }

  private Message parse(String msg) throws IOException {
    return om.readValue(msg, Message.class);
  }

  public Message getNextMessage() throws InterruptedException {
    return messages.take();
  }
}
