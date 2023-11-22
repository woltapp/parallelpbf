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
import com.hcspak.osm.parallelpbf.entity.Way;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WayParserTest {
  private final Consumer<Way> checker = (way) -> {
    assertEquals(1, way.getId());
    Assertions.assertEquals(TestObjectsFactory.info, way.getInfo());

    var tags = way.getTags();
    assertTrue(tags.containsKey("tag"));
    assertEquals("value", tags.get("tag"));

    assertEquals(9000, way.getNodes().get(0).longValue());
  };

  @Test
  void testWayParse() {
    var testedObject = new WayParser(checker, TestObjectsFactory.stringTable);
    testedObject.parse(TestObjectsFactory.wayMessage);
  }
}