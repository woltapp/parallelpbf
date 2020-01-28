package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import com.wolt.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OsmEntityEncoderTest {
    private class OsmEntityEncoderImpl extends OsmEntityEncoder<Node> {

        @Override
        public void add(Node entity) {

        }

        @Override
        public int estimateSize() {
            return 0;
        }

        @Override
        public byte[] write() {
            return new byte[0];
        }
    }

    @Test
    public void testStableIndex() {
        String str = "test";

        OsmEntityEncoder<Node> testedObject = new OsmEntityEncoderImpl();
        int index = testedObject.getStringIndex(str);
        int actual = testedObject.getStringIndex(str);

        assertEquals(index, actual);
    }

    @Test
    public void testStringTableSize() {
        String str = "test";

        OsmEntityEncoder<Node> testedObject = new OsmEntityEncoderImpl();
        testedObject.getStringIndex(str);
        assertEquals(4, testedObject.getStringSize());
        testedObject.getStringIndex(str);
        assertEquals(4, testedObject.getStringSize());
    }

    @Test
    public void testStringPresence() {
        String first = "first";
        String second = "second";

        OsmEntityEncoder<Node> testedObject = new OsmEntityEncoderImpl();
        int firstIndex = testedObject.getStringIndex(first);
        int secondIndex = testedObject.getStringIndex(second);
        int thirdIndex = testedObject.getStringIndex(second);

        Osmformat.StringTable strings = testedObject.getStrings().build();

        assertEquals(first, strings.getS(firstIndex).toStringUtf8());
        assertEquals(second, strings.getS(secondIndex).toStringUtf8());
        assertEquals(second, strings.getS(thirdIndex).toStringUtf8());
    }

    @Test
    public void testFirstStringReserved() {
        OsmEntityEncoder<Node> testedObject = new OsmEntityEncoderImpl();
        testedObject.getStringIndex("test");

        assertEquals(ByteString.EMPTY,testedObject.getStrings().build().getS(0));
    }
}