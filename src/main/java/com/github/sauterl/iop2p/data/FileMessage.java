package com.github.sauterl.iop2p.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class FileMessage extends Message {

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FileMessage{");
    sb.append("filename='").append(filename).append('\'');
    sb.append(", hash=").append(hash);
    sb.append(", timestamp=").append(getTimestamp());
    sb.append(", sourceUsername=").append(getSourceUsername());
    sb.append(", targetUsername=").append(getTargetUsername());
    sb.append(", type=").append(type);
    sb.append('}');
    return sb.toString();
  }

  private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("PNG", "JPG", "JPEG", "TIFF");

  private String filename;
  private String hash;

  public FileMessage() {
    super();
    type = MessageType.FILE;
  }

  public FileMessage(Message m){
    this();
    if(m.getType() == MessageType.FILE){
      try {
        PublishedFile f = PublishedFile.fromJson(m.getPayload());
        setFilename(f.getFilename());
        setHash(f.getHash());
        setSourceUsername(m.getSourceUsername());
        setTargetUsername(m.getTargetUsername());
      } catch (IOException e) {
        throw new RuntimeException("Coudln't parse payload. Is this really a filemessage?: "+m, e);
      }
    }else{
      throw new IllegalArgumentException("Cannot create FileMessage from non FILE message: "+m);
    }
  }

  @Override
  public String getPayload() {
    try {
      return new PublishedFile(filename,hash).toJson();
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Problem with serialization: ",e);
    }
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  @Override
  public MessageType getType() {
    return MessageType.FILE;
  }

  public boolean isImage(){
    return IMAGE_EXTENSIONS.stream().anyMatch(ext -> filename.endsWith(ext));
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class PublishedFile{
    private String filename;
    private String hash;

    public PublishedFile() {
    }

    public PublishedFile(String filename, String hash) {
      this.filename = filename;
      this.hash = hash;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }

    public String getHash() {
      return hash;
    }

    public void setHash(String hash) {
      this.hash = hash;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String toJson() throws JsonProcessingException {
      return OBJECT_MAPPER.writeValueAsString(this);
    }

    public static PublishedFile fromJson(String json) throws IOException {
      return OBJECT_MAPPER.readValue(json, PublishedFile.class);
    }
  }
}
