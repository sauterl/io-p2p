package com.github.sauterl.iop2p.data;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class EncryptedMessage extends Message {

  public EncryptedMessage(){
    super();
    type = MessageType.ENCRYPTED;
  }

}
