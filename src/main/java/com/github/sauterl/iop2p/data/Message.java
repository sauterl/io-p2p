package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {


  private long timestamp = System.currentTimeMillis();
  private String payload;
  private String sourceUsername;
  private String targetUsername;
  protected MessageType type;

  public Message() {
    type = MessageType.PLAIN;
  }

  public Message(String msg) {
    this.payload = msg;
    type = MessageType.PLAIN;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public String getSourceUsername() {
    return sourceUsername;
  }

  public void setSourceUsername(String sourceUsername) {
    this.sourceUsername = sourceUsername;
  }

  public String getTargetUsername() {
    return targetUsername;
  }

  public void setTargetUsername(String targetUsername) {
    this.targetUsername = targetUsername;
  }
}
