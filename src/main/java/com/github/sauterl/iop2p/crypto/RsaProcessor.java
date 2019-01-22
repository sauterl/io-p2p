package com.github.sauterl.iop2p.crypto;

import com.github.sauterl.iop2p.data.EncryptedMessage;
import com.github.sauterl.iop2p.data.Message;
import java.io.IOException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * RSA based en- and decryption of messages.
 *
 * @author loris.sauter
 */
public class RsaProcessor {

  private final AsymmetricKeyParameter[] ourKeys;
  private final AsymmetricKeyParameter[] theirKeys;

  public RsaProcessor(String ourKeysLocation, String theirsKeysLocation) throws IOException {
    ourKeys = loadKeys(ourKeysLocation);
    theirKeys = loadKeys(theirsKeysLocation);
  }

  private AsymmetricKeyParameter[] loadKeys(String keysLocation) throws IOException {
    AsymmetricKeyParameter[] keys = new AsymmetricKeyParameter[2];
    keys[0] = RSA.loadKeyFromFile(keysLocation, false);
    keys[1] = RSA.loadKeyFromFile(keysLocation, true);
    return keys;
  }

  public EncryptedMessage encrypt(Message m) throws InvalidCipherTextException {
    EncryptedMessage enc = new EncryptedMessage();
    enc.setPayload( RSA.encrypt(theirKeys[1], m.getPayload()));
    return enc;
  }

  public Message decrypt(EncryptedMessage enc) throws InvalidCipherTextException {
    Message m = new Message(enc);
    m.setPayload(RSA.decrypt(ourKeys[0], enc.getPayload()));
    return m;
  }

}
