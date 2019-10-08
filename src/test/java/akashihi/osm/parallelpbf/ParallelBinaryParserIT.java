package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.Node;
import akashihi.osm.parallelpbf.entity.Relation;
import akashihi.osm.parallelpbf.entity.RelationMember;
import akashihi.osm.parallelpbf.entity.Way;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static akashihi.osm.parallelpbf.entity.RelationMember.Type.WAY;
import static org.junit.jupiter.api.Assertions.*;

class ParallelBinaryParserIT {
    private Node simpleNode;
    private Node taggedNode;

    private Way taggedWay;

    private Relation taggedRelation;

    Consumer<Node> nodeChecker = (node) -> {
        if (node.getId() == 653970877) {
            simpleNode = node;
        }
        if (node.getId() == 502550970) {
            taggedNode = node;
        }
    };

    Consumer<Way> wayChecker = (way) -> {
        if (way.getId() == 158788812) {
            taggedWay = way;
        }
    };

    Consumer<Relation> relationsChecker = relation -> {
        if (relation.getId() == 31640) {
            taggedRelation = relation;
        }
    };

    @Test
    void testParser() {
        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("sample.pbf");


        new ParallelBinaryParser(input, 1)
                .onNode(nodeChecker)
                .onWay(wayChecker)
                .onRelation(relationsChecker)
                .parse();

        assertEquals(51.7636027, simpleNode.getLat(), 0.0000001);
        assertEquals(-0.22875700000000002, simpleNode.getLon(), 0.0000001);
        assertTrue(simpleNode.getTags().isEmpty());
        assertEquals(234999, simpleNode.getInfo().getUid());
        assertEquals("Nicholas Shanks", simpleNode.getInfo().getUsername());
        assertEquals(1, simpleNode.getInfo().getVersion());
        assertEquals(1267144226000L, simpleNode.getInfo().getTimestamp());
        assertEquals(3977001, simpleNode.getInfo().getChangeset());
        assertTrue(simpleNode.getInfo().isVisible());

        assertEquals(51.76511770000001, taggedNode.getLat(), 0.0000001);
        assertEquals(-0.23366680000000006, taggedNode.getLon(), 0.0000001);
        assertFalse(taggedNode.getTags().isEmpty());
        assertTrue(taggedNode.getTags().containsKey("name"));
        assertEquals("Oaktree Close", taggedNode.getTags().get("name"));
        assertTrue(taggedNode.getTags().containsKey("highway"));
        assertEquals("bus_stop", taggedNode.getTags().get("highway"));
        assertEquals(104459, taggedNode.getInfo().getUid());
        assertEquals("NaPTAN", taggedNode.getInfo().getUsername());
        assertEquals(1, taggedNode.getInfo().getVersion());
        assertEquals(1253397762000L, taggedNode.getInfo().getTimestamp());
        assertEquals(2539009, taggedNode.getInfo().getChangeset());
        assertTrue(taggedNode.getInfo().isVisible());


        assertEquals(Arrays.asList(1709246789L, 1709246746L, 1709246741L, 1709246791L), taggedWay.getNodes());
        assertFalse(taggedWay.getTags().isEmpty());
        assertTrue(taggedWay.getTags().containsKey("highway"));
        assertEquals("footway", taggedWay.getTags().get("highway"));
        assertEquals(470302, taggedWay.getInfo().getUid());
        assertEquals("Kjc", taggedWay.getInfo().getUsername());
        assertEquals(1, taggedWay.getInfo().getVersion());
        assertEquals(1334007464L, taggedWay.getInfo().getTimestamp());
        assertEquals(11245909, taggedWay.getInfo().getChangeset());
        assertFalse(taggedWay.getInfo().isVisible());

        Optional<RelationMember> member = taggedRelation.getMembers().stream().filter(m -> m.getId() == 25896432).findAny();
        assertTrue(member.isPresent());
        assertEquals("forward", member.get().getRole());
        assertEquals(WAY, member.get().getType());
        assertFalse(taggedRelation.getTags().isEmpty());
        assertTrue(taggedRelation.getTags().containsKey("route"));
        assertEquals("bicycle", taggedRelation.getTags().get("route"));
        assertEquals(24119, taggedRelation.getInfo().getUid());
        assertEquals("Mauls", taggedRelation.getInfo().getUsername());
        assertEquals(81, taggedRelation.getInfo().getVersion());
        assertEquals(1337419064L, taggedRelation.getInfo().getTimestamp());
        assertEquals(11640673, taggedRelation.getInfo().getChangeset());
        assertFalse(taggedRelation.getInfo().isVisible());
    }
}