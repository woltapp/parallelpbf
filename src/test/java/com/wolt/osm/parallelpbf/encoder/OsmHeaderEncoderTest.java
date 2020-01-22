package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wolt.osm.parallelpbf.entity.BoundBox;
import com.wolt.osm.parallelpbf.entity.Header;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OsmHeaderEncoderTest {
    @Test
    void testHeaderNoBBox() throws InvalidProtocolBufferException {
        byte[] blob = OsmHeaderEncoder.encodeHeader(null);

        Osmformat.HeaderBlock actual = Osmformat.HeaderBlock.parseFrom(blob);

        assertFalse(actual.hasBbox());

        assertTrue(actual.hasWritingprogram());
        assertEquals("parallelpbf", actual.getWritingprogram());

        assertTrue(actual.getRequiredFeaturesList().contains(Header.FEATURE_OSM_SCHEMA));
        assertTrue(actual.getRequiredFeaturesList().contains(Header.FEATURE_DENSE_NODES));
    }

    @Test
    void testHeaderBBox() throws InvalidProtocolBufferException {
        BoundBox bbox = new BoundBox(1, 2, 4 ,8);

        byte[] blob = OsmHeaderEncoder.encodeHeader(bbox);

        Osmformat.HeaderBlock actual = Osmformat.HeaderBlock.parseFrom(blob);

        assertTrue(actual.hasBbox());

        assertEquals(1000000000L, actual.getBbox().getLeft());
        assertEquals(2000000000L, actual.getBbox().getTop());
        assertEquals(4000000000L, actual.getBbox().getRight());
        assertEquals(8000000000L, actual.getBbox().getBottom());
    }
}