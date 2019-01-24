package com.github.sauterl.iop2p.data;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class EncryptedMessage extends Message {

  public EncryptedMessage() {
    super();
    type = MessageType.ENCRYPTED;
  }

  public EncryptedMessage(Message m) {
    setSourceUsername(m.getSourceUsername());
    setTargetUsername(m.getTargetUsername());
    setTimestamp(m.getTimestamp());
  }

  public static EncryptedMessage of(Message message) {
    EncryptedMessage m = new EncryptedMessage(message);
    m.setPayload(message.getPayload());
    return m;
  }

  @Override
  public MessageType getType() {
    return MessageType.ENCRYPTED;
  }
}
