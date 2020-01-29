package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.TestObjectsFactory;
import com.wolt.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OsmEntityEncoderTest {
    private static class OsmEntityEncoderImpl extends OsmEntityEncoder<Node> {

        @Override
        public void addImpl(Node entity) {

        }

        @Override
        public int estimateSize() {
            return 0;
        }

        @Override
        public Osmformat.PrimitiveGroup.Builder writeImpl() {
            return Osmformat.PrimitiveGroup.newBuilder();
        }
    }

    @Test
    public void testNoUseAfterWrite() {
        OsmEntityEncoderImpl testedObject = new OsmEntityEncoderImpl();
        testedObject.add(TestObjectsFactory.node());
        testedObject.write();
        assertThrows(IllegalStateException.class, () -> testedObject.add(TestObjectsFactory.node()));
    }
}