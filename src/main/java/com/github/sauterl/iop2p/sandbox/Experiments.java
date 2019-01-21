package com.github.sauterl.iop2p.sandbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Experiments {

  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

    String privateKeyPath = "C:/Users/Loris/.babun/cygwin/home/Loris/.ssh/awsunibas";
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    FileReader fr = new FileReader(privateKeyPath);
    BufferedReader br = new BufferedReader(fr);
    StringBuilder sb = new StringBuilder();
    List<String> l = br.lines().collect(Collectors.toCollection(ArrayList::new));
    l.remove(0);
    l.remove(l.size()-1);
    l.forEach(sb::append);


    String key = sb.toString();


    Base64.Decoder dec = Base64.getDecoder();
    AsymmetricKeyParameter privateKey =
        PrivateKeyFactory.createKey(dec.decode(key));


    AsymmetricBlockCipher e = new RSAEngine();
    e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
    e.init(false, privateKey);

    System.out.println("asdf");


    /*try {
      //IPFSAdapter.create("C:/Users/loris/uni/11_hs18/internet-overloards/go-ipfs/ipfs.exe");
      IPFSAdapter ipfsAdapter = IPFSAdapter.create("C:/Users/loris/uni/11_hs18/internet-overloards/go-ipfs/ipfs.exe", "./ipfs-repo");
      ipfsAdapter.ipfs().pubsub.pub("asdf", "asdf");
      System.out.println("ffff");
      ipfsAdapter.close();
      System.exit(0);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }*/
  }
}
