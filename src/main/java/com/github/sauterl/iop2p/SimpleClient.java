package com.github.sauterl.iop2p;

import com.github.sauterl.iop2p.data.Message;
import com.github.sauterl.iop2p.net.Chatter;
import io.ipfs.api.IPFS;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class SimpleClient implements Runnable {

  private String username;
  private IPFS ipfs;
  private volatile boolean running = true;
  private Chatter chatter;

  public SimpleClient(String multiaddr, String username) {
    this.username = username;
    ipfs = new IPFS(multiaddr);
    chatter = new Chatter(username, ipfs.pubsub);
  }

  public static void main(String[] args) {
    new Thread(new SimpleClient(args[0], args[1])).start();
    ;
  }

  public void run() {
    Scanner scanner = new Scanner(System.in);
    Thread incoming = new Thread(() -> {
      Message msg = null;
      try {
        msg = chatter.getNextMessage();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
      Date d = new Date(msg.getTimestamp());
      System.out.println(String
          .format("%s wrote on %s:\n\t%s", msg.getSourceUsername(), sdf.format(d),
              msg.getPayload()));
    });
    incoming.start();
    while (running) {
      String line = scanner.nextLine();
      if (line.equalsIgnoreCase("q")) {
        running = false;
      } else {
        String[] components = line.split(":");
        if (components.length >= 2) {
          try {
            chatter.send(components[0], components[1]);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          System.err.println("Chat via <username>:<message> ENTER.");
        }
      }
    }
    incoming.interrupt();

  }

}
