package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.TestObjectsFactory;
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
        WayEncoder testedObject = new WayEncoder(stringEncoder);
        testedObject.add(TestObjectsFactory.way());

        assertEquals(40, testedObject.estimateSize());
    }

    @Test
    public void testWrite() {
        WayEncoder testedObject = new WayEncoder(stringEncoder);
        testedObject.add(TestObjectsFactory.way());

        Osmformat.PrimitiveGroup actual = testedObject.write().build();

        Osmformat.Way w = actual.getWays(0);
        assertEquals(1, w.getId());
        assertEquals(1, w.getKeys(0));
        assertEquals(1, w.getVals(0));

        assertEquals(3, w.getRefs(0));
        assertEquals(3, w.getRefs(1));
        assertEquals(-4, w.getRefs(2));

        assertEquals(3, w.getInfo().getVersion());
        assertEquals(5, w.getInfo().getChangeset());
        assertEquals(1, w.getInfo().getUid());
        assertEquals(true, w.getInfo().getVisible());
    }
}