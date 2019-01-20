package com.github.sauterl.iop2p.ipfs;

import io.ipfs.api.IPFS;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessDestroyer;
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer;

/**
 * Wraps the java ipfs instance with it's deamon. Enables to use multiple ipfs instances, e.g.
 * nodes.
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

  public static final String IPFS_DAEMON_COMMAND = "daemon";
  public static final String IPFS_INIT_COMMAND = "init";
  public static final String ENABLE_PUBSUB_FLAG = "--enable-pubsub-experiment";
  public static final String IPFS_ENVIRONMENT_VARIABLE = "IPFS_PATH";


  private FutureTask<IPFS> futureTask;

  private String ipfsExecPath;
  private String ipfsRepo = "./ipfs-repo/"; // Default


  public static IPFS create(String ipfsExecPath, String ipfsRepo)
      throws ExecutionException, InterruptedException {
    IPFSAdapter adapter = new IPFSAdapter(ipfsExecPath, ipfsRepo);
    return adapter.get();
  }


  private IPFSAdapter(String ipfsExecPath, String ipfsRepo) {
    this.ipfsExecPath = ipfsExecPath;
    this.ipfsRepo = ipfsRepo;
    IPFSDaemon daemon = new IPFSDaemon(ipfsExecPath, ipfsRepo);
    Thread daemonThread = new Thread(daemon);
    daemonThread.start();
    Callable<IPFS> callable = () -> {
      while(daemon.getApiAddress() == null){
        Thread.sleep(10);
        System.out.println("ADdr: "+daemon.getApiAddress());
      }
      return new IPFS(daemon.getApiAddress());
    };
    futureTask = new FutureTask<>(callable);
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    executorService.execute(futureTask);
  }

  public IPFS get() throws ExecutionException, InterruptedException {
    return futureTask.get();
  }

  @Deprecated
  private static void create(String path)
      throws InterruptedException, TimeoutException, IOException {
    final boolean[] success = {false};

    new ProcessExecutor().command(path, "daemon").redirectOutput(
        new org.zeroturnaround.exec.stream.LogOutputStream() {
          @Override
          protected void processLine(String line) {
            System.out.println("[DEAMON] " + line);
            if (line.contains("Error: no IPFS repo found")) {
              success[0] = false;
            }
            if (line.contains("API")) {
              success[0] = true;
            }
          }
        }).environment("IPFS_PATH", "./ipfs-repo/")
        .addDestroyer(ShutdownHookProcessDestroyer.INSTANCE).execute(); // blocking -> thread

    System.out.println("IPFS ready: " + success[0]);
    if (!success[0]) {
      new ProcessExecutor().command(path, "init").redirectOutput(
          new org.zeroturnaround.exec.stream.LogOutputStream() {
            @Override
            protected void processLine(String line) {
              System.out.println("[DAEMON] " + line);
            }
          }).destroyOnExit().execute();
    }
  }

  public static IPFS startDaemonAndCreate() {

    CommandLine commandLine = new CommandLine(IPFS_DAEMON_COMMAND);
    commandLine.addArgument(ENABLE_PUBSUB_FLAG);
    DefaultExecutor executor = new DefaultExecutor();
    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

    PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(new LogOutputStream() {


      @Override
      protected void processLine(String line, int logLevel) {
        System.out.println("[IPFS DAEMON] " + line);
      }
    });

    executor.setStreamHandler(pumpStreamHandler);
    try {
      executor.execute(commandLine, resultHandler);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      Thread.sleep(60 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    String multiaddr;

    return null;
  }

  private void handleDaemonOutput(String line) {
    System.out.println("[IPFS DAEMON] " + line);
    if (line.startsWith("API")) {
      String[] parts = line.split(" ");
    }
  }

  private static class OnCallProcessDestroyer implements ProcessDestroyer {

    private final Vector<Process> processes = new Vector<>();


    @Override
    public boolean add(Process process) {
      return processes.add(process);
    }

    @Override
    public boolean remove(Process process) {
      return processes.remove(process);
    }

    @Override
    public int size() {
      return processes.size();
    }

    public void destroyAll() {
      processes.forEach(p -> {
        if (p.isAlive()) {
          p.destroy();
        }
      });
    }
  }

  private static class IPFSDaemon implements Runnable {

    private final OnCallProcessDestroyer destroyer = new OnCallProcessDestroyer();
    private String path;
    private String repo;
    private String api = null;

    public IPFSDaemon(String path, String repo) {
      this.path = path;
      this.repo = repo;
    }

    @Override
    public void run() {
      try {
        boolean success = executeIpfsDaemon();
        if(!success){
          createAndExecuteIpfsDaemon();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

    public String getApiAddress() {
      if(api == null){
        return null;
      }else{

        return api.substring(api.indexOf("/"));
      }
    }

    public void stop() {
      destroyer.destroyAll();
    }


    private boolean executeIpfsDaemon()
        throws InterruptedException, TimeoutException, IOException {
      final boolean[] success = {false};

      new ProcessExecutor().command(path, IPFS_DAEMON_COMMAND, ENABLE_PUBSUB_FLAG).redirectOutput(
          new org.zeroturnaround.exec.stream.LogOutputStream() {
            @Override
            protected void processLine(String line) {
              System.out.println("[DEAMON] " + line);
              if (line.contains("Error: no IPFS repo found")) {
                success[0] = false;
              }
              if (line.contains("API")) {
                success[0] = true;
                api = line;
              }
            }
          }).environment(IPFS_ENVIRONMENT_VARIABLE, repo)
          .addDestroyer(ShutdownHookProcessDestroyer.INSTANCE).addDestroyer(destroyer)
          .execute(); // blocking -> thread
      return success[0];
    }

    private boolean createAndExecuteIpfsDaemon()
        throws InterruptedException, TimeoutException, IOException {
      ProcessResult result = new ProcessExecutor().command(path, IPFS_INIT_COMMAND).redirectOutput(
          new org.zeroturnaround.exec.stream.LogOutputStream() {
            @Override
            protected void processLine(String line) {
              System.out.println("[INIT] "+line);
            }
          }).environment(IPFS_ENVIRONMENT_VARIABLE, repo).destroyOnExit().execute();
      if(result.getExitValue() == 0){
        return executeIpfsDaemon();
      }else{
        throw new RuntimeException("Couldn't init");
      }
    }
  }



}

