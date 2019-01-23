package com.github.sauterl.iop2p.cli;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract command to provide an interface to common options.
 *
 * @author loris.sauter
 */
public abstract class AbstractCommand implements Runnable {

  protected Logger logger;
  /**
   * The IPFS executable file. This must be the full path to the exuctable, either relative or
   * absolute. Using go-ipfs, this is usually sumething like {@code path/to/go-ipfs/ipfs} or {@code
   * /usr/var/bin/ipfs} after installation.
   */
  @Option(title = "IPFS Executable", name = {"-e",
      "--ipfs"}, description = "The IPFS executable as a relative or absolute path")
  @Required
  private String ipfsExecutable;
  /**
   * The IPFS repository to use as an option. This is useful if someone wants to have mulitple chat
   * clients on the same machine.
   */
  @Option(title = "IPFS repository", name = {"-d",
      "--repo"}, description = "The IPFS repository to use")
  private String ipfsRepo = ".ipfs-repo";

  protected AbstractCommand() {
    logger = LoggerFactory.getLogger(this.getClass());
  }

  /**
   * Returns the IPFS executable inclusive path as a string.
   *
   * @return The IPFS executable inclusive its path as a string.
   */
  public String getIpfsExecutable() {
    return ipfsExecutable;
  }

  /**
   * Executes this command
   */
  @Override
  public void run() {
    execute();
  }

  public String getIpfsRepo() {
    return ipfsRepo;
  }

  /**
   * The command's main. All logic goes into here.
   */
  public abstract void execute();
}
