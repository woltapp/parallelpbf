package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wolt.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WayEncoderTest {

    @Test
    public void testWaySize() {
        Way way = new Way(1L);
        way.getTags().put("test", "test");
        way.getNodes().add(3L);
        way.getNodes().add(6L);
        way.getNodes().add(2L);

        WayEncoder testedObject = new WayEncoder();
        testedObject.add(way);

        assertEquals(44, testedObject.estimateSize());
    }

    @Test
    public void testWrite() throws InvalidProtocolBufferException {
        String str = "test";
        Way way = new Way(1L);
        way.getTags().put(str, str);
        way.getNodes().add(3L);
        way.getNodes().add(6L);
        way.getNodes().add(2L);

        WayEncoder testedObject = new WayEncoder();
        testedObject.add(way);

        byte[] blob = testedObject.write();

        Osmformat.PrimitiveBlock actual = Osmformat.PrimitiveBlock.parseFrom(blob);

        Osmformat.StringTable stringTable  = actual.getStringtable();
        assertEquals(ByteString.EMPTY, stringTable.getS(0));
        assertEquals(str, stringTable.getS(1).toStringUtf8());

        Osmformat.Way w = actual.getPrimitivegroup(0).getWays(0);
        assertEquals(1, w.getId());
        assertEquals(1, w.getKeys(0));
        assertEquals(1, w.getVals(0));

        assertEquals(3, w.getRefs(0));
        assertEquals(3, w.getRefs(1));
        assertEquals(-4, w.getRefs(2));
    }

    @Test
    public void testNoUseAfterWrite() {
        Way way = new Way(1L);

        WayEncoder testedObject = new WayEncoder();
        testedObject.add(way);
        testedObject.write();
        assertThrows(IllegalStateException.class, () -> testedObject.add(way));
    }

}