package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

  public Message() {}

  public Message(String msg) {
    this.payload = msg;
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

  private long timestamp = System.currentTimeMillis();
  private String payload;
  private String sourceUsername;
  private String targetUsername;

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
