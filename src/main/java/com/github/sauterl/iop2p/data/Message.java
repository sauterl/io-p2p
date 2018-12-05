package com.github.sauterl.iop2p.data;

public class Message {

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
}
