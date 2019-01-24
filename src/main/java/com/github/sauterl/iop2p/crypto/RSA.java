package com.github.sauterl.iop2p.crypto;

import com.github.sauterl.iop2p.Utils;
import io.ipfs.multibase.Base58;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Base64;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class RSA {

  private RSA() {}

  public static AsymmetricKeyParameter loadKeyFromFile(String path, boolean pub)
      throws IOException {
    String file = Paths.get(path, "/", pub ? KeyGen.PUBLIC_KEY : KeyGen.PRIVATE_KEY).toString();

    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    String keyFile = Utils.readFileContents(file);

    Base64.Decoder dec = Base64.getDecoder();

    AsymmetricKeyParameter key = null;
    if (pub) {
      key = PublicKeyFactory.createKey(dec.decode(keyFile));
    } else {
      key = PrivateKeyFactory.createKey(dec.decode(keyFile));
    }

    return key;
  }

  public static String encrypt(AsymmetricKeyParameter publicKey, String decrypted)
      throws InvalidCipherTextException {
    if (publicKey.isPrivate()) {
      throw new IllegalArgumentException("Public key is private!");
    }

    AsymmetricBlockCipher e = new RSAEngine();
    e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
    e.init(true, publicKey);


    Base64.Encoder enc = Base64.getEncoder();
    byte[] encodedBytes = e.processBlock(decrypted.getBytes(), 0, decrypted.getBytes().length);
    return enc.encodeToString(encodedBytes);
  }

  public static String decrypt(AsymmetricKeyParameter privateKey, String encryptedMsg)
      throws InvalidCipherTextException {
    if (!privateKey.isPrivate()) {
      throw new IllegalArgumentException("Private key is public!");
    }

    AsymmetricBlockCipher e = new RSAEngine();
    e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
    e.init(false, privateKey);

    Base64.Decoder dec = Base64.getDecoder();

    byte[] encodedBytes =
        e.processBlock(dec.decode(encryptedMsg), 0, dec.decode(encryptedMsg).length);
    return new String(encodedBytes);
  }
}
