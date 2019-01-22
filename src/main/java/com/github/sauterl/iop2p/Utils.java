package com.github.sauterl.iop2p;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

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

  public static Shape createLeftSpeechBubble(){
    SVGPath svgPath = new SVGPath();
    svgPath.setContent("m 23.979133,62.016026 2.187523,-16.748167 c -2.769994,0 -4.99999,-2.229996 -4.99999,-4.99999 V 17.194931 c 0,-2.769995 2.229996,-4.99999 4.99999,-4.99999 h 75.989604 c 2.77,0 4.99999,2.229995 4.99999,4.99999 v 23.072938 c 0,2.769994 -2.22999,4.99999 -4.99999,4.99999 H 32.836682 Z");
    return svgPath;
  }

  public static Shape createRightSpeechBubble(){
    SVGPath svgPath = new SVGPath();
    svgPath.setContent("m 104.34378,62.016026 -2.18752,-16.748167 c 2.76999,0 4.99999,-2.229996 4.99999,-4.99999 V 17.194931 c 0,-2.769995 -2.23,-4.99999 -4.99999,-4.99999 H 26.166656 c -2.77,0 -4.99999,2.229995 -4.99999,4.99999 v 23.072938 c 0,2.769994 2.22999,4.99999 4.99999,4.99999 h 69.319578 z");
    return svgPath;
  }

}
