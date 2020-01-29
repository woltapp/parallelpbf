package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringTableEncoderTest {
    @Test
    public void testStableIndex() {
        String str = "test";

        StringTableEncoder testedObject = new StringTableEncoder();
        int index = testedObject.getStringIndex(str);
        int actual = testedObject.getStringIndex(str);

        assertEquals(index, actual);
    }

    @Test
    public void testStringTableSize() {
        String str = "test";

        StringTableEncoder testedObject = new StringTableEncoder();
        testedObject.getStringIndex(str);
        assertEquals(4, testedObject.getStringSize());
        testedObject.getStringIndex(str);
        assertEquals(4, testedObject.getStringSize());
    }

    @Test
    public void testStringPresence() {
        String first = "first";
        String second = "second";

        StringTableEncoder testedObject = new StringTableEncoder();
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
        StringTableEncoder testedObject = new StringTableEncoder();
        testedObject.getStringIndex("test");

        assertEquals(ByteString.EMPTY,testedObject.getStrings().build().getS(0));
    }
}