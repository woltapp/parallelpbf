/*
 * This file is part of parallelpbf.
 *
 *     parallelpbf is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wolt.osm.parallelpbf.parser;

import com.wolt.osm.parallelpbf.entity.Info;
import crosby.binary.Osmformat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for OSM message parsers. Stores callback and a shared string table.
 *
 * @param <M> Type of the message to process.
 * @param <T> Callback Type
 */
@RequiredArgsConstructor
@Getter
public abstract class BaseParser<M, T extends Consumer<?>> {
  /**
   * Callback to call on successful parse of the entity.
   */
  private final T callback;

  /**
   * Indexed table os strings.
   */
  private final Osmformat.StringTable stringTable;

  /**
   * Helper that knows how to extract tags from the OSM entity.
   * <p>
   * Tags are stored as two parallel arrays of indices of keys and values.
   * Each index points to the some string in the string table.
   *
   * @param keys   List if tag keys indices in the string table.
   * @param values List if tag values indices in the string table.
   * @return Map of tags with their values.
   */
  Map<String, String> parseTags(final List<Integer> keys, final List<Integer> values) {
    HashMap<String, String> result = new HashMap<>();
    for (int indx = 0; indx < keys.size(); ++indx) {
      String key = stringTable.getS(keys.get(indx)).toStringUtf8();
      String value = stringTable.getS(values.get(indx)).toStringUtf8();
      result.put(key, value);
    }
    return result;
  }

  /**
   * Checks if OSM entity have Info filled and extracts it from the PBF.
   *
   * @param message Node message to parse.
   * @return Info message if present of null otherwise.
   */
  Info parseInfo(final Osmformat.Node message) {
    Osmformat.Info infoMessage = null;
    if (message.hasInfo()) {
      infoMessage = message.getInfo();
    }
    return convertInfo(infoMessage);
  }

  /**
   * Checks if OSM entity have Info filled and extracts it from the PBF.
   *
   * @param message Way message to parse.
   * @return Info message if present of null otherwise.
   */
  Info parseInfo(final Osmformat.Way message) {
    Osmformat.Info infoMessage = null;
    if (message.hasInfo()) {
      infoMessage = message.getInfo();
    }
    return convertInfo(infoMessage);
  }

  /**
   * Checks if OSM entity have Info filled and extracts it from the PBF.
   *
   * @param message Relation message to parse.
   * @return Info message if present of null otherwise.
   */
  Info parseInfo(final Osmformat.Relation message) {
    Osmformat.Info infoMessage = null;
    if (message.hasInfo()) {
      infoMessage = message.getInfo();
    }
    return convertInfo(infoMessage);
  }

  /**
   * Parses PBF info message and return Info entity.
   *
   * @param infoMessage Info message to parse. Can be null.
   * @return Info entity or null in case of error or if incoming message was null.
   */
  private Info convertInfo(final Osmformat.Info infoMessage) {
    if (infoMessage != null) {
      String username = stringTable.getS(infoMessage.getUserSid()).toStringUtf8();
      boolean isVisible = !infoMessage.hasVisible() || infoMessage.getVisible();
      return new Info(infoMessage.getUid(),
          username,
          infoMessage.getVersion(),
          infoMessage.getTimestamp(),
          infoMessage.getChangeset(),
          isVisible);
    }
    return null;
  }

  /**
   * Actual parse function, should be implemented for a specific type.
   *
   * @param message PBF message to parse.
   */
  protected abstract void parse(M message);
}
