package com.github.sauterl.iop2p.crypto;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.sauterl.iop2p.Utils;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
@Command(name = "key-gen", description = "Generate key-pair for usage with iop2p")
public class KeyGen implements Runnable {


  public static final String PUBLIC_KEY = "public-key";
  public static final String PRIVATE_KEY = "private-key";

  @Required
  @Option(name = {"-o", "--output"}, description = "Path to write the key files to")
  private String outputPath;

  @Override
  public void run() {
    try {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

      // Create the public and private keys
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

      Base64.Encoder b64Enc = Base64.getEncoder();

      String publicKeyFilename = Paths.get(outputPath, "/", PUBLIC_KEY).toFile()
          .getAbsolutePath(); // TODO Extract constants
      String privateKeyFilename = Paths.get(outputPath, "/", PRIVATE_KEY).toFile()
          .getAbsolutePath();

      System.out.println("Pub: " + publicKeyFilename);
      System.out.println("Priv: " + privateKeyFilename);

      SecureRandom random = SecureRandom.getInstanceStrong();
      generator.initialize(1024, random);

      KeyPair pair = generator.generateKeyPair();
      Key pubKey = pair.getPublic();
      Key privKey = pair.getPrivate();

      String puk = b64Enc.encodeToString(pubKey.getEncoded());
      String pik = b64Enc.encodeToString(privKey.getEncoded());
      System.out.println("publicKey : " + puk);
      System.out.println(PRIVATE_KEY + "Key : " + pik);

      Utils.writeFileContents(publicKeyFilename, puk);

      Utils.writeFileContents(privateKeyFilename, pik);
    } catch (Throwable t) {
      // Ignore // TODO Proper exception handling!
    }
  }

}
