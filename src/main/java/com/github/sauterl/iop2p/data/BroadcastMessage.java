package com.github.sauterl.iop2p.data;

public class BroadcastMessage extends Message {

  public BroadcastMessage() {
    type = MessageType.BROADCAST;
  }

  public BroadcastMessage(Message message) {
    this();
    setPayload(message.getPayload());
    setTimestamp(message.getTimestamp());
    setSourceUsername(message.getSourceUsername());
  }

  @Override
  public MessageType getType() {
    return MessageType.BROADCAST;
  }

  @Override
  public String getTargetUsername() {
    return "";
  }

  @Override
  public void setTargetUsername(String targetUsername) {
    super.setTargetUsername("");
  }
}
