package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WayParserTest {
    private final Consumer<Way> checker = (way) -> {
        assertEquals(1, way.getId());
        assertEquals(ParseTestObjects.info, way.getInfo());

        var tags = way.getTags();
        assertTrue(tags.containsKey("tag"));
        assertEquals("value", tags.get("tag"));

        assertEquals(9000, way.getNodes().get(0).longValue());
    };

    @Test
    void testWayParse() {
        var wayMessage = Osmformat.Way.newBuilder()
                .setId(1)
                .addKeys(3)
                .addVals(4)
                .setInfo(ParseTestObjects.infoMessage)
                .addRefs(9000)
                .build();

        var testedObject = new WayParser(checker, ParseTestObjects.stringTable);
        testedObject.parse(wayMessage);
    }
}