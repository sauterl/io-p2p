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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

  private String topic;
  private Pubsub pubsub;
  private ObjectMapper om = new ObjectMapper();
  private BlockingQueue<Message> messages = new ArrayBlockingQueue<>(1000);

  private volatile boolean running = false;


  public Receiver(String topic, Pubsub pubsub) {
    System.out.println("Receiving at " + topic);
    this.topic = topic;
    this.pubsub = pubsub;
  }

  @Override
  public void run() {
    running = true;
    try {
      pubsub
          .sub(topic)
          .forEach(
              msg -> {
                try {
                  String rawMsg = parseRaw((String) msg.get("data"));
                  LOGGER.trace("Received raw: {}", rawMsg);
                  Optional<Message> prsdMsg = parse(rawMsg);
                  if (prsdMsg.isPresent()) {
                    Message actual = prsdMsg.get();
                    LOGGER.debug("Received Message: {}", actual);
                    messages.add(actual);
                  } else {
                    LOGGER.warn("Received non-valid message: {}", rawMsg);
                  }
                } catch (Base64DecodingException | IOException e) {
                  LOGGER.warn("Ignoring exception during receiving.", e);
                }
              });
    } catch(InterruptedException ie) {
      if(!running){
        // Everything is fine
      }else{
        LOGGER.error("Was unexpectetly interrupted. Will die now", ie);
      }
    }catch (Exception e) {
      LOGGER.error("An error occurred",e);
    }
  }

  public void stop(){
    running = false;
  }

  private String parseRaw(String data) throws Base64DecodingException {
    return new String(Base64.decode(data));
  }

  private Optional<Message> parse(String msg) throws IOException {
    try {
      Message m = om.readValue(msg, Message.class);
      return Optional.of(m);
    } catch (JsonMappingException | JsonParseException e) {
      return Optional.empty();
    }
  }

  public Message getNextMessage() throws InterruptedException {
    return messages.take();
  }
}
