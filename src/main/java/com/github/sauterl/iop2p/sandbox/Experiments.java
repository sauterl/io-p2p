package com.github.sauterl.iop2p.sandbox;

import com.github.sauterl.iop2p.ipfs.IPFSAdapter;
import java.io.IOException;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Experiments {

  public static void main(String[] args) throws IOException {
    IPFSAdapter adapter = new IPFSAdapter();
    adapter.start();
  }
}
