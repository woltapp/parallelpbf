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

package com.hcspak.osm.parallelpbf.parser;

import com.hcspak.osm.parallelpbf.TestObjectsFactory;
import com.hcspak.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeParserTest {

  private final Consumer<Node> checker = (node) -> {
    assertEquals(1, node.getId());
    assertEquals(1, node.getLat(), 0.1);
    assertEquals(2, node.getLon(), 0.1);
    Assertions.assertEquals(TestObjectsFactory.info, node.getInfo());

    var tags = node.getTags();
    assertTrue(tags.containsKey("tag"));
    assertEquals("value", tags.get("tag"));
  };

  @Test
  void testNodeParse() {
    var testedObject = new NodeParser(checker, TestObjectsFactory.stringTable, 1, 0, 0, 1);
    testedObject.parse(TestObjectsFactory.nodeMessage);
  }

  @Test
  void testDenseNodeParse() {
    var testedObject = new NodeParser(checker, TestObjectsFactory.stringTable, 1, 0, 0, 1);
    testedObject.parse(TestObjectsFactory.denseNodesMessage);
  }

  @Test
  void testDenseNodeParseDefaultVisible() {
    var denseInfo = Osmformat.DenseInfo.newBuilder()
        .addUid(1)
        .addUserSid(2)
        .addVersion(3)
        .addTimestamp(4)
        .addChangeset(5)
        .build();
    var denseNodes = Osmformat.DenseNodes.newBuilder()
        .addId(1)
        .addLat(1000000000)
        .addLon(2000000000)
        .addKeysVals(3).addKeysVals(4).addKeysVals(0)
        .setDenseinfo(denseInfo)
        .build();

    var testedObject = new NodeParser(checker, TestObjectsFactory.stringTable, 1, 0, 0, 1);
    testedObject.parse(denseNodes);
  }
}