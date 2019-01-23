package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {


  protected MessageType type = MessageType.PLAIN;
  private long timestamp = System.currentTimeMillis();
  private String payload;
  private String sourceUsername;
  private String targetUsername;

  public Message() {
  }

  public Message(String msg) {
    this.payload = msg;
    type = MessageType.PLAIN;
  }

  public Message(EncryptedMessage enc) {
    setSourceUsername(enc.getSourceUsername());
    setTargetUsername(enc.getTargetUsername());
    setTimestamp(enc.getTimestamp());
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("timestamp=").append(timestamp);
    sb.append(", payload='").append(payload).append('\'');
    sb.append(", sourceUsername='").append(sourceUsername).append('\'');
    sb.append(", targetUsername='").append(targetUsername).append('\'');
    sb.append(", type=").append(type);
    sb.append('}');
    return sb.toString();
  }
}
