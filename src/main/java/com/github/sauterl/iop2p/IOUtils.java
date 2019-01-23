package com.github.sauterl.iop2p;

import com.github.sauterl.iop2p.crypto.KeyStore;
import com.github.sauterl.iop2p.data.ChatHistory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {

  public static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

  public static final String HOME = "iop2p";

  public static final String HISTORY_PREFIX = "history-";
  public static final String HISTORY_EXTENSION = ".json";

  public static String getDirectory() {

    String path = System.getProperty("user.dir") + "/" + HOME; // get cwd from System
    File f = new File(path);
    f.mkdirs();

    return path;
  }

  public static String getHistoryFile(String user) {
    return Paths.get(getDirectory(), HISTORY_PREFIX + user + HISTORY_EXTENSION).toString();
  }

  public static void deleteHistory(String user) throws IOException {
    Path path = Paths.get(getHistoryFile(user));
    Files.delete(path);
    LOGGER.info("Removed history at {}", path);
  }

  public static void saveHistory(ChatHistory history) throws IOException {
    Path path = Paths.get(getHistoryFile(history.getUser()));
    JSONUtils.writeToJSONFile(history, path.toFile());
    LOGGER.info("Wrote history at {}", path);
  }

  public static ChatHistory loadHistory(String user) throws IOException {
    Path path = Paths.get(getHistoryFile(user));
    ChatHistory history = JSONUtils.readFromJSONFile(path.toFile(), ChatHistory.class);
    LOGGER.info("Loaded history from {}", path);
    return history;
  }

  public static void saveKeystore(KeyStore store) throws IOException {
    Path path = getKeystorePath();
    JSONUtils.writeToJSONFile(store, path.toFile());
    LOGGER.info("Wrote keystore at {}", path);
  }

  public static KeyStore loadKeystore() throws IOException {
    Path path = getKeystorePath();
    KeyStore store = JSONUtils.readFromJSONFile(path.toFile(), KeyStore.class);
    LOGGER.info("Loaded keystore from {}", path);
    return store;
  }

  public static boolean hasKeyStore(){
    return Files.exists(getKeystorePath());
  }

  private static Path getKeystorePath() {
    return Paths.get(getDirectory(), "keystore.json");
  }

  public static Path getOurKeyLocation(){
    return Paths.get(getDirectory(), "my-keys/");
  }


}
