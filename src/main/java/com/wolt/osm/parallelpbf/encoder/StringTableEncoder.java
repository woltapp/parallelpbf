package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

/**
 * Encodes StringTable for the whole blob.
 * <p>
 * Encoder is stateful and shouldn't be used after 'getStrings' call is issued.
 * Encoder is not thread-safe.
 */
public class StringTableEncoder {
  /**
   * Keeps current maximum string index value.
   */
  private Integer stringIndex = 0;

  /**
   * Size of strings kept in the string table.
   */
  private Integer stringTableSize = 0;

  /**
   * Reverse index mapping - for string already stored in the table it will map
   * string values back to their indices.
   */
  private final Map<String, Integer> indexMap = new HashMap<>();

  /**
   * The string table.
   * -- GETTER --
   * String table accessor.
   */
  @Getter
  private final Osmformat.StringTable.Builder strings = Osmformat.StringTable.newBuilder();

  /**
   * Adds string to the string table and adds string size to the stringtable size.
   *
   * @param str String to add.
   * @return String index in table.
   */
  private int addStringToTable(final String str) {
    stringTableSize = stringTableSize + str.length();
    strings.addS(ByteString.copyFromUtf8(str));
    return ++stringIndex;
  }

  /**
   * Default constructor.
   */
  public StringTableEncoder() {
    strings.addS(ByteString.EMPTY); //First entry with index 0 is always empty.
  }

  /**
   * Finds stringtable index for a supplied string. Will return either existing index for a string
   * or add string to the stringtable and emit a new index.
   *
   * @param s String to index.
   * @return Strings index in the stringtable.
   */
  public int getStringIndex(final String s) {
    return indexMap.computeIfAbsent(s, this::addStringToTable);
  }

  /**
   * Returns current estimated size of the string table.
   *
   * @return size in bytes.
   */
  public int getStringSize() {
    return stringTableSize;
  }
}
