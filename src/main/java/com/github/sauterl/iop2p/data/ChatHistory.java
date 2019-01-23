package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * A chat history with a specific user. Usually a chat history consists of the messages exchanged
 * with that user.
 *
 * @author loris.sauter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatHistory {

  private String user;
  private long lastSaved;
  private List<Message> messages = new ArrayList<>();

  public ChatHistory() {
  }

  public ChatHistory(String user, long lastSaved,
      List<Message> messages) {
    this.user = user;
    this.lastSaved = lastSaved;
    this.messages = messages;
  }

  public ChatHistory(String partner) {
    user = partner;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public long getLastSaved() {
    return lastSaved;
  }

  public void setLastSaved(long lastSaved) {
    this.lastSaved = lastSaved;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

}
