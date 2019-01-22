package com.github.sauterl.iop2p;

import java.io.File;

public class IOUtils {

  public static final String HOME = "iop2p";

  public static String getDirectory() {

    String path = System.getProperty("user.dir") + "/" + HOME; // get cwd from System
    File f = new File(path);
    f.mkdirs();

    return path;
  }
}
