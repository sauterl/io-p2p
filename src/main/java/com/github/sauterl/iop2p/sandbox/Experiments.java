package com.github.sauterl.iop2p.sandbox;

import com.github.sauterl.iop2p.data.FileMessage;
import com.github.sauterl.iop2p.ipfs.FileExchange;
import com.github.sauterl.iop2p.ipfs.IPFSAdapter;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Experiments {

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    IPFSAdapter adapter = IPFSAdapter.create("C:/Users/Loris/uni/11_hs18/internet-overloards/go-ipfs/ipfs.exe", "ipfs-repo");
    FileExchange exhange = new FileExchange(adapter.ipfs());
    try {
      FileMessage m = exhange.publishFile(new File("helloworld.txt"));
      File f = exhange.loadPublishedFile(m);
      System.out.println(f);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
