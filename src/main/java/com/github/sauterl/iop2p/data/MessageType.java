package com.github.sauterl.iop2p.data;

/**
 * Enumeration of possible message types. This is used to eventually parse the message accordingly
 * and handle different UI requirements.
 *
 * @author loris.sauter
 */
public enum MessageType {

  PLAIN,
  ENCRYPTED,
  FILE,
  BROADCAST
}
