package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Info;
import akashihi.osm.parallelpbf.entity.Node;
import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class NodeParserTest {

    private Info info = new Info(1, "test", 3, 4, 5, true);
    private Osmformat.Info infoMessage = Osmformat.Info.newBuilder().setUid(1).setUserSid(2).setVersion(3).setTimestamp(4).setChangeset(5).setVisible(true).build();
    private Osmformat.StringTable stringTable = Osmformat.StringTable.newBuilder()
            .addS(ByteString.copyFromUtf8(""))
            .addS(ByteString.copyFromUtf8("fail"))
            .addS(ByteString.copyFromUtf8("test"))
            .addS(ByteString.copyFromUtf8("tag"))
            .addS(ByteString.copyFromUtf8("value"))
            .build();

    @Test
    void testNodeParse() {
        var nodeMessage = Osmformat.Node.newBuilder()
                .setId(1)
                .setLat(1000000000)
                .setLon(2000000000)
                .addKeys(3)
                .addVals(4)
                .setInfo(infoMessage)
                .build();

        Consumer<Node> checker = (node) -> {
            assertEquals(1, node.getId());
            assertEquals(1, node.getLat(), 0.1);
            assertEquals(2, node.getLon(), 0.1);
            assertEquals(info, node.getInfo());

            var tags = node.getTags();
            assertTrue(tags.containsKey("tag"));
            assertEquals("value", tags.get("tag"));
        };

        var testedObject = new NodeParser(checker, stringTable, 1, 0, 0, 1);
        testedObject.parse(nodeMessage);
    }

    @Test
    void testDenseNodeParse() {
        var denseInfo = Osmformat.DenseInfo.newBuilder()
                .addUid(1)
                .addUserSid(2)
                .addVersion(3)
                .addTimestamp(4)
                .addChangeset(5)
                .addVisible(true)
                .build();
        var denseNodes = Osmformat.DenseNodes.newBuilder()
                .addId(1)
                .addLat(1000000000)
                .addLon(2000000000)
                .addKeysVals(3).addKeysVals(4).addKeysVals(0)
                .setDenseinfo(denseInfo)
                .build();

        Consumer<Node> checker = (node) -> {
            assertEquals(1, node.getId());
            assertEquals(1, node.getLat(), 0.1);
            assertEquals(2, node.getLon(), 0.1);
            assertEquals(info, node.getInfo());

            var tags = node.getTags();
            assertTrue(tags.containsKey("tag"));
            assertEquals("value", tags.get("tag"));
        };

        var testedObject = new NodeParser(checker, stringTable, 1, 0, 0, 1);
        testedObject.parse(denseNodes);
    }
}