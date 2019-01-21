package com.github.sauterl.iop2p;

import com.github.rvesse.airline.annotations.Cli;
import com.github.sauterl.iop2p.crypto.KeyGen;
import com.github.sauterl.iop2p.net.Receiver;
import com.github.sauterl.iop2p.net.Sender;
import com.github.sauterl.iop2p.sandbox.Testings;
import io.ipfs.api.IPFS;
import java.util.Scanner;

@Cli(name="iop2p", commands = {KeyGen.class, Testings.class})
public class Main {


  public static void main(String[] args) throws Exception {
    new com.github.rvesse.airline.Cli<Runnable>(Main.class).parse(args).run();
  }
}
