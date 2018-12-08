package com.github.sauterl.iop2p.ipfs;

import io.ipfs.api.IPFS;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Wraps the java ipfs instance with it's deamon.
 * Enables to use multiple multiple ipfs instances, e.g. nodes.
 * Requires that {@code ipfs} is a command known to the operating system.
 *
 * @author loris.sauter
 */
public class IPFSAdapter {

  /*
  Notes:
  This class shall

   1. Start the ipfs daemon as suprocess
   2. Wait for the line API server has started ..., split on space and use last one
   3. to create IPFS instance

   Later: IPFS init and stuff as well
   */

  public static final String IPFS_DAEMON_COMMAND = "ipfs daemon";
  public static final String ENABLE_PUBSUB_FLAG = "--enable-pubsub-experiment";

  public static final String IPFS_INIT_COMMAND = "ipfs init";

  public static final String IPFS_PATH_PREFIX = "IPFS_PATH=";

  private IPFS ipfs;

  private Process daemonProcess;

  private ProcessBuilder processBuilder;

  /**
   * Creates the default IPFSAdapter.
   * Expects that ipfs was previously initialized with the command-line call of {@code ipfs init}.
   */
  public IPFSAdapter(){
    processBuilder = new ProcessBuilder(IPFS_DAEMON_COMMAND, ENABLE_PUBSUB_FLAG);
    processBuilder.redirectErrorStream(true);
  }

  public void start() throws IOException {
    daemonProcess = processBuilder.start();
    ProcessHandler ph = new ProcessHandler(daemonProcess, this::handleDaemonOutput);
  }

  private void handleDaemonOutput(String line){
    System.out.println("[IPFS DAEMON] "+line);
    if(line.startsWith("API")){
      String[] parts = line.split(" ");
        ipfs = new IPFS(parts[parts.length-1]);
    }
  }

  private static class ProcessHandler implements Runnable{

    private BufferedReader br;
    private Thread host;
    private Consumer<String> handler;

    public ProcessHandler(Process p, Consumer<String> handler){
      br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      host = new Thread(this);
      this.handler = handler;
      host.start();
    }

    public void stop(){
      host.interrupt();
    }

    @Override
    public void run() {
      try {
        String line = "";
        while ((line = br.readLine()) != null) {
          handler.accept(line);
        }
      }catch(IOException ex){
        ex.printStackTrace();
      } finally {
        try {
          br.close();
        } catch (IOException e) {
          System.err.println("Could not close br");
          e.printStackTrace();
        }
      }
    }
  }



}
