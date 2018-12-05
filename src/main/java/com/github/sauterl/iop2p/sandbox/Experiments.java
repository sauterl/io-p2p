package com.github.sauterl.iop2p.sandbox;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.ipfs.api.IPFS;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Experiments {

  public static void main(String[] args) {

    IPFS ipfs = new IPFS(args[0]);
    try {
      ipfs.pubsub
          .sub(args[1])
          .forEach(
              e -> {
                System.out.println(e);
                String a = (String) e.get("com/github/sauterl/iop2p/data");
                try {
                  System.out.println("Data: " + new String(Base64.decode(a)));
                } catch (Base64DecodingException e1) {
                  e1.printStackTrace();
                }
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
