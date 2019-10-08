package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.TestObjectsFactory;
import akashihi.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeParserTest {

    private final Consumer<Node> checker = (node) -> {
        assertEquals(1, node.getId());
        assertEquals(1, node.getLat(), 0.1);
        assertEquals(2, node.getLon(), 0.1);
        assertEquals(TestObjectsFactory.info, node.getInfo());

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