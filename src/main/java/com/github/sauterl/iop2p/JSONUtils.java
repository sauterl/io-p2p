package com.github.sauterl.iop2p;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class JSONUtils {

  private JSONUtils(){
    // no objects
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  {
    OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Converts the given object into its JSON representation.
   *
   * @param obj The object to convert
   * @return The object represented as a string in JSON notation
   * @throws JsonProcessingException
   * @see ObjectMapper#writeValueAsString(Object)
   */
  public static String toJSON(Object obj) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(obj);
  }

  /**
   * Writes the given object into the specified file as a JSON file.
   *
   * @param obj  The object which sould be written as JSON into the given file
   * @param file The target file to write the JSON string into
   * @throws IOException
   * @see ObjectMapper#writeValue(File, Object)
   */
  public static void writeToJSONFile(Object obj, File file) throws IOException {
    OBJECT_MAPPER.writeValue(file, obj);
  }

  /**
   * Reads from the given file an object of spiefied class.
   *
   * @param file  The file to read from
   * @param clazz The class of the object which is JSON encoded in the file
   * @param <T>   The type of the object to return
   * @return An object of type T, with its value read from the JSON representation in file
   * @throws IOException
   * @see ObjectMapper#readValue(File, Class)
   */
  public static <T> T readFromJSONFile(File file, Class<T> clazz) throws IOException {
    return OBJECT_MAPPER.readValue(file, clazz);
  }

}
