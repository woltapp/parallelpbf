package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.TestObjectsFactory;
import akashihi.osm.parallelpbf.entity.Way;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WayParserTest {
    private final Consumer<Way> checker = (way) -> {
        assertEquals(1, way.getId());
        assertEquals(TestObjectsFactory.info, way.getInfo());

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