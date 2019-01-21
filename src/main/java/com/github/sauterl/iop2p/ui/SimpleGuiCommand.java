package com.github.sauterl.iop2p.ui;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.sauterl.iop2p.cli.AbstractCommand;
import com.github.sauterl.iop2p.ipfs.IPFSAdapter;
import io.ipfs.api.IPFS;
import java.util.concurrent.ExecutionException;

/**
 * TODO: Write JavaDoc
 *
 * @author loris.sauter
 */
@Command(name = "simplegui")
public class SimpleGuiCommand extends AbstractCommand {

  @Option(title="User name", name={"-u", "--user"})
  @Required
  private String username;

  public String getUsername() {
    return username;
  }

  private static SimpleGuiCommand instance = null;

  public SimpleGuiCommand(){
    super();
    instance = this;
  }

  public IPFS getIpfs() {
    return ipfs;
  }

  private IPFS ipfs;
  @Override
  public void execute() {
    LaunchHelper<ChatWindow> launchHelper = new LaunchHelper<>(ChatWindow.class);
    try {
      IPFSAdapter adapter = IPFSAdapter.create(getIpfsExecutable(), getIpfsRepo());
      ipfs = adapter.ipfs();
      logger.debug("Aquired ipfs");
      launchHelper.launch();
      logger.debug("Launched UI");
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static SimpleGuiCommand getInstance() {
    return instance;
  }
}
