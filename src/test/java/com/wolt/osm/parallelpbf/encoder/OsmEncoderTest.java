package com.wolt.osm.parallelpbf.encoder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OsmEncoderTest {

    @Test
    void testNanoScale() {
        double input = 100.5009;

        long actual = OsmEncoder.doubleToNanoScaled(input);
        assertEquals(100500900000L, actual);
    }
}