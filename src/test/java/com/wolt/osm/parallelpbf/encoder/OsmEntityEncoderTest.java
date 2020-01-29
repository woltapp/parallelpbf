package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OsmEntityEncoderTest {
    private class OsmEntityEncoderImpl extends OsmEntityEncoder<Node> {

        @Override
        public void addImpl(Node entity) {

        }

        @Override
        public int estimateSize() {
            return 0;
        }

        @Override
        public byte[] writeImpl() {
            return new byte[0];
        }
    }

    @Test
    public void testNoUseAfterWrite() {
        String str = "test";
        Node node = new Node(1, 10.0, 50.0);
        node.getTags().put(str, str);

        OsmEntityEncoderImpl testedObject = new OsmEntityEncoderImpl();
        testedObject.add(node);
        testedObject.write();
        assertThrows(IllegalStateException.class, () -> testedObject.add(node));
    }
}