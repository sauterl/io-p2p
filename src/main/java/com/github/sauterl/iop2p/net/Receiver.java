package com.github.sauterl.iop2p.net;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sauterl.iop2p.data.Message;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.ipfs.api.IPFS.Pubsub;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Receiver implements Runnable {

  // TODO Loggerize

  private String topic;
  private Pubsub pubsub;
  private ObjectMapper om = new ObjectMapper();
  private BlockingQueue<Message> messages = new ArrayBlockingQueue<>(1000);




  public Receiver(String topic, Pubsub pubsub) {
    System.out.println("Receiving at "+topic);
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
                  Optional<Message> prsdMsg = parse(rawMsg);
                  if(prsdMsg.isPresent()){
                    Message actual = prsdMsg.get();
                    System.out.println(actual.getPayload());
                    messages.add(actual);
                  }else{
                    System.out.println("Coudln't handle: "+rawMsg);
                  }
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

  private Optional<Message> parse(String msg) throws IOException {
    try{
      Message m = om.readValue(msg, Message.class);
      return Optional.of(m);
    }catch(JsonMappingException | JsonParseException e){
      return Optional.empty();
    }
  }

  public Message getNextMessage() throws InterruptedException {
    return messages.take();
  }
}
