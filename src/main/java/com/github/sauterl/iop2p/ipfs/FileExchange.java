package com.github.sauterl.iop2p.ipfs;

import com.github.sauterl.iop2p.IOUtils;
import com.github.sauterl.iop2p.data.FileMessage;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class FileExchange {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileExchange.class);

  private final IPFS ipfs;

  public FileExchange(IPFS ipfs) {
    this.ipfs = ipfs;
  }

  public FileMessage publishFile(File file) throws IOException {
    Path copy = IOUtils.getPublishDir().resolve(file.getName());
    Files.copy(file.toPath(), copy, StandardCopyOption.REPLACE_EXISTING);
    LOGGER.debug("Adding {}, which will be copied to {}", file, copy);
    MerkleNode node = ipfs.add(new NamedStreamable.FileWrapper(file)).get(0);
    LOGGER.debug("Added with hash {}",node.hash);
    FileMessage fm = new FileMessage();
    fm.setFilename(file.getName());
    fm.setHash(node.hash.toBase58());
    return fm;
  }


  public File loadPublishedFile(FileMessage msg) throws IOException {
    LOGGER.debug("Should load: {}",msg);
    Multihash filePointer = Multihash.fromBase58(msg.getHash());
    Path target = IOUtils.getDownloadDir().resolve(msg.getFilename());
    byte[] fileContents = ipfs.cat(filePointer);
    Files.write(target, fileContents);
    return target.toFile();
  }


}
