package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import com.wolt.osm.parallelpbf.TestObjectsFactory;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringTableEncoderTest {
    @Test
    public void testStableIndex() {
        StringTableEncoder testedObject = new StringTableEncoder();
        int index = testedObject.getStringIndex(TestObjectsFactory.testTag);
        int actual = testedObject.getStringIndex(TestObjectsFactory.testTag);

        assertEquals(index, actual);
    }

    @Test
    public void testStringTableSize() {
        StringTableEncoder testedObject = new StringTableEncoder();
        testedObject.getStringIndex(TestObjectsFactory.testTag);
        assertEquals(4, testedObject.getStringSize());
        testedObject.getStringIndex(TestObjectsFactory.testTag);
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
        testedObject.getStringIndex(TestObjectsFactory.testTag);

        assertEquals(ByteString.EMPTY,testedObject.getStrings().build().getS(0));
    }
}