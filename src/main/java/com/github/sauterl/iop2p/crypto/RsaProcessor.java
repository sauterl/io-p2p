package com.github.sauterl.iop2p.crypto;

import com.github.sauterl.iop2p.data.EncryptedMessage;
import com.github.sauterl.iop2p.data.Message;
import java.io.IOException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * RSA based en- and decryption of messages.
 *
 * <p>Requires 'our' private key to encrypt the message and 'their' public key.
 *
 * <p>There is the requirement that public keys are stored at the specified location as textfield
 * with the name 'public-key', Private key as 'private-key'
 *
 * @author loris.sauter
 */
public class RsaProcessor {

  private final AsymmetricKeyParameter ourKey;
  private final AsymmetricKeyParameter theirKey;

  public RsaProcessor(String ourKeyLocation, String theirKeysLocation) throws IOException {
    ourKey = RSA.loadKeyFromFile(ourKeyLocation, false);
    theirKey = RSA.loadKeyFromFile(theirKeysLocation, true);
  }

  public EncryptedMessage encrypt(Message m) throws InvalidCipherTextException {
    EncryptedMessage enc = new EncryptedMessage();
    enc.setPayload(RSA.encrypt(theirKey, m.getPayload()));
    return enc;
  }

  public Message decrypt(EncryptedMessage enc) throws InvalidCipherTextException {
    Message m = new Message(enc);
    m.setPayload(RSA.decrypt(ourKey, enc.getPayload()));
    return m;
  }
}
