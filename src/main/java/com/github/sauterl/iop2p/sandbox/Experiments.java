package com.github.sauterl.iop2p.sandbox;

import com.github.sauterl.iop2p.ipfs.IPFSAdapter;
import io.ipfs.api.IPFS;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Experiments {

  public static void main(String[] args) throws IOException {
    try {
      //IPFSAdapter.create("C:/Users/loris/uni/11_hs18/internet-overloards/go-ipfs/ipfs.exe");
      IPFSAdapter ipfsAdapter = IPFSAdapter.create("C:/Users/loris/uni/11_hs18/internet-overloards/go-ipfs/ipfs.exe", "./ipfs-repo");
      ipfsAdapter.get().pubsub.pub("asdf", "asdf");
      System.out.println("ffff");
      ipfsAdapter.close();
      System.exit(0);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
