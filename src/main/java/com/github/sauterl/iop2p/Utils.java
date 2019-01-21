package com.github.sauterl.iop2p;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Utils {

  private Utils() {
    // no instance needed
  }

  public static String getUsernameInboxTopic(String username){
    return "/iop2pchat/inbox/"+username;
  }

  public static String readFileContents(String path) throws FileNotFoundException {
    StringBuilder sb = new StringBuilder();

    BufferedReader br = new BufferedReader(new FileReader(path));
    br.lines().forEach(sb::append);

    return sb.toString();
  }

  public static void writeFileContents(String path, String content) throws IOException {
    File f = Paths.get(path).getParent().toFile();
    f.mkdirs();
    BufferedWriter bw = new BufferedWriter(new FileWriter(path));
    bw.write(content);
    bw.flush();
    bw.close();
  }

}
