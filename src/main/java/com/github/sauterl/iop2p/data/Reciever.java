package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.ipfs.api.IPFS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reciever implements Runnable {

  private String topic;
  private IPFS ipfs;
  private ObjectMapper om = new ObjectMapper();
  private List<Message> messages = new ArrayList<>();

  public Reciever(String topic, IPFS ipfs) {
    this.topic = topic;
    this.ipfs = ipfs;
  }

  @Override
  public void run() {
    try {
      ipfs.pubsub
          .sub(topic)
          .forEach(
              msg -> {
                try {
                  String rawMsg = parseRaw((String) msg.get("data"));
                  Message actual = parse(rawMsg);
                  System.out.println(actual.getPayload());
                  messages.add(actual);
                } catch (Base64DecodingException e) {
                  e.printStackTrace();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String parseRaw(String data) throws Base64DecodingException {
    return new String(Base64.decode(data));
  }

  public Message parse(String msg) throws IOException {
    return om.readValue(msg, Message.class);
  }
}
