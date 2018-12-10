package com.github.sauterl.iop2p;

import com.github.sauterl.iop2p.net.Receiver;
import com.github.sauterl.iop2p.net.Sender;
import io.ipfs.api.IPFS;
import java.util.Scanner;

public class Main {
  private String username;
  private IPFS ipfs;
  private Sender s;
  private Receiver r;

  public Main(String username, String node) {
    this.username = username;
    ipfs = new IPFS(node);
    s = new Sender(ipfs.pubsub);
    r = new Receiver(username, ipfs.pubsub);
  }

  public void run() throws Exception {
    Thread recieving = new Thread(r);
    recieving.start();

    Scanner s = new Scanner(System.in);
    boolean running = true;

    while(running){
      String line = s.nextLine();
      if (line.equalsIgnoreCase("q")){
        System.exit(0);
      }
      else{
        String[] parts = line.split(":");
      //  this.s.send(parts[0], parts[1]);
      }
    }
  }


  public String getTopic(){
    return "/iop2pchat/inbox/" + username;
  }

  public static void main(String[] args) throws Exception {
    Main m  = new Main(args[1], args[0]);
    m.run();
  }
}
