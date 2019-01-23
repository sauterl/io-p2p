package com.github.sauterl.iop2p.crypto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Custom map-alike class for holding username/rsa-key-pair-location entries.
 *
 * This class is for serialization purposes and designed to work with fasterxml/jackson.
 *
 * @author loris.sauter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyStore {

  private final ArrayList<Entry> keys = new ArrayList<>();

  public List<Entry> getKeys() {
    return keys;
  }


  public boolean add(String user, String keyLocation) {
    return keys.add(new Entry(user, keyLocation));
  }


  public Optional<String> get(String user) {
    Optional<Entry> canditates = keys.stream().filter(entry -> entry.user.equals(user)).findFirst();
    return canditates.map(entry -> entry.keyLocation);
  }

  @JsonIgnore
  public int size() {
    return keys.size();
  }

  public Optional<Entry> getEntry(String they) {
    return keys.stream().filter(entry -> entry.user.equals(they)).findFirst();
  }

  /**
   * The actual entry which maps usernames and keystore location
   */
  public static class Entry {

    private String user;
    private String keyLocation;

    public Entry() {
    }

    public Entry(String user, String keyLocation) {
      this.user = user;
      this.keyLocation = keyLocation;
    }

    public String getUser() {
      return user;
    }

    public void setUser(String user) {
      this.user = user;
    }

    public String getKeyLocation() {
      return keyLocation;
    }

    public void setKeyLocation(String keyLocation) {
      this.keyLocation = keyLocation;
    }
  }

}
