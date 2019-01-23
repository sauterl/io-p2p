package com.github.sauterl.iop2p.ipfs;

import io.ipfs.api.IPFS;
import java.io.IOException;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  // TODO Cleanup

  public static final String IPFS_DAEMON_COMMAND = "daemon";
  /*
  Notes:
  This class shall

   1. Start the ipfs daemon as suprocess
   2. Wait for the line API server has started ..., split on space and use last one
   3. to create IPFS instance

   Later: IPFS init and stuff as well
   */
  public static final String IPFS_INIT_COMMAND = "init";
  public static final String ENABLE_PUBSUB_FLAG = "--enable-pubsub-experiment";
  public static final String IPFS_ENVIRONMENT_VARIABLE = "IPFS_PATH";
  private final static Logger LOGGER = LoggerFactory.getLogger(IPFSAdapter.class);
  private static IPFSAdapter instance = null;
  private final IPFSDaemon daemon;
  private final Thread daemonThread;
  private FutureTask<IPFS> futureTask;
  private String ipfsExecPath;
  private String ipfsRepo = "./ipfs-repo/"; // Default
  private IPFS ipfs = null;

  private IPFSAdapter(String ipfsExecPath, String ipfsRepo) {
    this.ipfsExecPath = ipfsExecPath;
    this.ipfsRepo = ipfsRepo;
    daemon = new IPFSDaemon(ipfsExecPath, ipfsRepo);
    daemonThread = new Thread(daemon);
    daemonThread.start();
    Callable<IPFS> callable = () -> {
      while (daemon.getApiAddress() == null) {
        Thread.sleep(1000);
      }
      return new IPFS(daemon.getApiAddress());
    };
    futureTask = new FutureTask<>(callable);
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    executorService.execute(futureTask);
    LOGGER.trace("Created IPFS Adapter");
  }

  public static IPFSAdapter getInstance() {
    if (instance != null) {
      return instance;
    } else {
      throw new IllegalStateException("Wasn't initialized yet. Call create(string, string) before");
    }
  }

  public static IPFSAdapter create(String ipfsExecPath, String ipfsRepo) {
    instance = new IPFSAdapter(ipfsExecPath, ipfsRepo);
    return instance;
  }

  public void close() {
    daemon.stop();
    daemonThread.interrupt();
    futureTask.cancel(true);
  }

  public IPFS ipfs() throws ExecutionException, InterruptedException {
    ipfs = futureTask.get();
    return ipfs;
  }

  public Optional<IPFS> getCachedIPFS() {
    return ipfs != null ? Optional.of(ipfs) : Optional.empty();
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
      processes.forEach(Process::destroy);
    }
  }

  private static class IPFSDaemon implements Runnable {

    private final OnCallProcessDestroyer destroyer = new OnCallProcessDestroyer();
    private String path;
    private String repo;
    private String api = null;
    private volatile boolean stopped = false;

    public IPFSDaemon(String path, String repo) {
      this.path = path;
      this.repo = repo;
    }

    @Override
    public void run() {
      Thread.currentThread().setName("IPFSDaemonThread");
      try {
        boolean success = executeIpfsDaemon();
        if (!success) {
          createAndExecuteIpfsDaemon();
        }
      } catch (InterruptedException e) {
        if (!stopped) {

          e.printStackTrace();
        } else {
          // Gracefull stop, all good
        }
      } catch (TimeoutException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

    public String getApiAddress() {
      if (api == null) {
        return null;
      } else {

        return api.substring(api.indexOf("/"));
      }
    }

    public void stop() {
      destroyer.destroyAll();
      stopped = true;
    }


    private boolean executeIpfsDaemon()
        throws InterruptedException, TimeoutException, IOException {
      final boolean[] success = {false};

      new ProcessExecutor().command(path, IPFS_DAEMON_COMMAND, ENABLE_PUBSUB_FLAG).redirectOutput(
          new org.zeroturnaround.exec.stream.LogOutputStream() {
            @Override
            protected void processLine(String line) {
              LOGGER.debug("Daemon: {}", line);
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
          .execute();
      return success[0];
    }

    private boolean createAndExecuteIpfsDaemon()
        throws InterruptedException, TimeoutException, IOException {
      ProcessResult result = new ProcessExecutor().command(path, IPFS_INIT_COMMAND)
          .addDestroyer(ShutdownHookProcessDestroyer.INSTANCE).addDestroyer(destroyer)
          .redirectOutput(
              new org.zeroturnaround.exec.stream.LogOutputStream() {
                @Override
                protected void processLine(String line) {
                  LOGGER.debug("Init: {}", line);
                }
              }).environment(IPFS_ENVIRONMENT_VARIABLE, repo).execute();
      if (result.getExitValue() == 0) {
        return executeIpfsDaemon();
      } else {
        throw new RuntimeException("Couldn't init");
      }
    }
  }


}

