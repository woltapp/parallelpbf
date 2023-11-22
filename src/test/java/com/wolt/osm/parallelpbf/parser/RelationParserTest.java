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

import com.wolt.osm.parallelpbf.TestObjectsFactory;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.RelationMember;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class RelationParserTest {
    private final Consumer<Relation> checker = (relation) -> {
        assertEquals(1, relation.getId());
        Assertions.assertEquals(TestObjectsFactory.info, relation.getInfo());

        var tags = relation.getTags();
        assertTrue(tags.containsKey("tag"));
        assertEquals("value", tags.get("tag"));

        var actualMember = relation.getMembers().get(0);
        assertEquals(9000, actualMember.id().longValue());
        Assertions.assertEquals(RelationMember.Type.NODE, actualMember.type());
        assertEquals("fail", actualMember.role());
        assertNotNull(relation.getInfo());
        assertEquals("test", relation.getInfo().username());
    };

    @Test
    void testRelationParse() {
        var testedObject = new RelationParser(checker, TestObjectsFactory.stringTable);
        testedObject.parse(TestObjectsFactory.relationMessage);
    }
}