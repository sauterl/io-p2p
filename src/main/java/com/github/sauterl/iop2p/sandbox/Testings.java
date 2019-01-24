package com.github.sauterl.iop2p.sandbox;

import com.github.rvesse.airline.annotations.Command;
import com.github.sauterl.iop2p.crypto.RSA;
import java.io.IOException;
import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
@Command(name = "sandbox", description = "Testing things out - quick and dirty")
public class Testings implements Runnable {

  @Override
  public void run() {
    try {
      String enc = RSA.encrypt(RSA.loadKeyFromFile(".", true), "Hello World");
      System.out.println("ENC: " + enc);
      System.out.println(RSA.decrypt(RSA.loadKeyFromFile(".", false), enc));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidCipherTextException e) {
      e.printStackTrace();
    }
  }
}
