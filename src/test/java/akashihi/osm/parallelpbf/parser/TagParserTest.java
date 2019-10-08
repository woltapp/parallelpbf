package akashihi.osm.parallelpbf.parser;

import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("BaseParser")
class TagParserTest {

    static class TagParser extends BaseParser<Object, Consumer<Object>> {

        public TagParser(Consumer<Object> callback, Osmformat.StringTable stringTable) {
            super(callback, stringTable);
        }

        @Override
        public void parse(Object message) { }
    }

    @Test
    void testParseTags() {
        var stringTable = Osmformat.StringTable.newBuilder()
                .addS(ByteString.copyFromUtf8("")) // Index 0 is always empty
                .addS(ByteString.copyFromUtf8("tag")) // Index 1 will be tag
                .addS(ByteString.copyFromUtf8("value"))
                .build(); // Index 2 will be value

        var testedObject = new TagParser(null, stringTable);
        var actual = testedObject.parseTags(Collections.singletonList(1), Collections.singletonList(2));

        testedObject.parse(null);
        assertTrue(actual.containsKey("tag"));
        assertEquals(actual.get("tag"), "value");
    }
}