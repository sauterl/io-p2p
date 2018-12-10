package com.github.sauterl.iop2p;

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

}
