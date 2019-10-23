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

package org.akashihi.osm.parallelpbf.parser;

import org.akashihi.osm.parallelpbf.TestObjectsFactory;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("BaseParser")
class TagParserTest {

    static class TagParser extends BaseParser<Object, Consumer<Object>> {

        public TagParser(Consumer<Object> callback, Osmformat.StringTable stringTable) {
            super(callback, stringTable);
        }

        @Override
        public void parse(Object message) { }
    }

    @Test
    void testParseTags() {
        var testedObject = new TagParser(null, TestObjectsFactory.stringTable);
        var actual = testedObject.parseTags(Collections.singletonList(3), Collections.singletonList(4));

        testedObject.parse(null);
        assertTrue(actual.containsKey("tag"));
        assertEquals(actual.get("tag"), "value");
    }
}