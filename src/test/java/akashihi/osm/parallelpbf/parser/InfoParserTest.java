package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Info;
import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Tag("BaseParser")
class InfoParserTest {

    class InfoParser extends BaseParser<Object, Consumer<Object>> {

        public InfoParser(Consumer<Object> callback, Osmformat.StringTable stringTable) {
            super(callback, stringTable);
        }

        @Override
        protected void parse(Object message) { }
    }

    private Info info = new Info(1, "test", 3, 4, 5, true);
    private Osmformat.Info infoMessage = Osmformat.Info.newBuilder().setUid(1).setUserSid(2).setVersion(3).setTimestamp(4).setChangeset(5).setVisible(true).build();
    private Osmformat.StringTable stringTable = Osmformat.StringTable.newBuilder().addS(ByteString.copyFromUtf8("")).addS(ByteString.copyFromUtf8("fail")).addS(ByteString.copyFromUtf8("test")).build();

    @Test
    void testNodeInfoMissing() {
        var node = Osmformat.Node.newBuilder()
                .setId(1)
                .setLat(2)
                .setLon(3)
                .build();

        var testedObject = new InfoParser(null, stringTable);

        assertNull(testedObject.parseInfo(node));
    }

    @Test
    void testNodeInfo() {
        var node = Osmformat.Node.newBuilder()
                .setId(1)
                .setLat(2)
                .setLon(3)
                .setInfo(infoMessage)
                .build();

        var testedObject = new InfoParser(null, stringTable);

        var actual = testedObject.parseInfo(node);
        assertEquals(info, actual);
    }

    @Test
    void testWayInfoMissing() {
        var way = Osmformat.Way.newBuilder()
                .setId(1)
                .build();

        var testedObject = new InfoParser(null, stringTable);

        assertNull(testedObject.parseInfo(way));
    }

    @Test
    void testWayInfo() {
        var way = Osmformat.Way.newBuilder()
                .setId(1)
                .setInfo(infoMessage)
                .build();

        var testedObject = new InfoParser(null, stringTable);

        var actual = testedObject.parseInfo(way);
        assertEquals(info, actual);
    }

    @Test
    void testRelationInfoMissing() {
        var relation = Osmformat.Relation.newBuilder()
                .setId(1)
                .build();

        var testedObject = new InfoParser(null, stringTable);

        assertNull(testedObject.parseInfo(relation));
    }

    @Test
    void testRelationInfo() {
        var relation = Osmformat.Relation.newBuilder()
                .setId(1)
                .setInfo(infoMessage)
                .build();

        var testedObject = new InfoParser(null, stringTable);

        var actual = testedObject.parseInfo(relation);
        assertEquals(info, actual);
    }
}