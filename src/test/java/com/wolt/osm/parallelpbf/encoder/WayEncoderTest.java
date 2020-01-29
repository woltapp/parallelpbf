package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WayEncoderTest {
    private StringTableEncoder stringEncoder;

    @BeforeEach
    public void setUp() {
        stringEncoder = new StringTableEncoder();
    }

    @Test
    public void testWaySize() {
        Way way = new Way(1L);
        way.getTags().put("test", "test");
        way.getNodes().add(3L);
        way.getNodes().add(6L);
        way.getNodes().add(2L);

        WayEncoder testedObject = new WayEncoder(stringEncoder);
        testedObject.add(way);

        assertEquals(40, testedObject.estimateSize());
    }

    @Test
    public void testWrite() {
        String str = "test";
        Way way = new Way(1L);
        way.getTags().put(str, str);
        way.getNodes().add(3L);
        way.getNodes().add(6L);
        way.getNodes().add(2L);

        WayEncoder testedObject = new WayEncoder(stringEncoder);
        testedObject.add(way);

        Osmformat.PrimitiveGroup actual = testedObject.write().build();

        Osmformat.Way w = actual.getWays(0);
        assertEquals(1, w.getId());
        assertEquals(1, w.getKeys(0));
        assertEquals(1, w.getVals(0));

        assertEquals(3, w.getRefs(0));
        assertEquals(3, w.getRefs(1));
        assertEquals(-4, w.getRefs(2));
    }
}