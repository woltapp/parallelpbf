package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.TestObjectsFactory;
import akashihi.osm.parallelpbf.entity.Relation;
import akashihi.osm.parallelpbf.entity.RelationMember;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class RelationParserTest {
    private final Consumer<Relation> checker = (relation) -> {
        assertEquals(1, relation.getId());
        assertEquals(TestObjectsFactory.info, relation.getInfo());

        var tags = relation.getTags();
        assertTrue(tags.containsKey("tag"));
        assertEquals("value", tags.get("tag"));

        var actualMember = relation.getMembers().get(0);
        assertEquals(9000, actualMember.getId().longValue());
        assertEquals(RelationMember.Type.NODE, actualMember.getType());
        assertEquals("fail", actualMember.getRole());
    };

    @Test
    void testRelationParse() {
        var testedObject = new RelationParser(checker, TestObjectsFactory.stringTable);
        testedObject.parse(TestObjectsFactory.relationMessage);
    }
}