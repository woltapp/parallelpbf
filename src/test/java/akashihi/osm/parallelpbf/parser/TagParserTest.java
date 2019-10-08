package akashihi.osm.parallelpbf.parser;

import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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
        var testedObject = new TagParser(null, ParseTestObjects.stringTable);
        var actual = testedObject.parseTags(Collections.singletonList(3), Collections.singletonList(4));

        testedObject.parse(null);
        assertTrue(actual.containsKey("tag"));
        assertEquals(actual.get("tag"), "value");
    }
}