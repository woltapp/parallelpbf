package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DenseNodesEncoderTest {
    private StringTableEncoder stringEncoder;

    @BeforeEach
    public void setUp() {
        stringEncoder = new StringTableEncoder();
    }

    @Test
    public void testNodeSize() {
        Node node = new Node(1, 100.0, 500.0);
        node.getTags().put("test", "test");

        DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
        testedObject.add(node);

        assertEquals(40, testedObject.estimateSize());
    }

    @Test
    public void testWrite() {
        String str = "test";
        Node node = new Node(1, 10.0, 50.0);
        node.getTags().put(str, str);

        DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
        testedObject.add(node);
        Osmformat.PrimitiveGroup actual = testedObject.write().build();

        Osmformat.DenseNodes nodes = actual.getDense();
        assertEquals(1, nodes.getId(0));
        assertEquals(5.0E8, nodes.getLon(0));
        assertEquals(1.0E8, nodes.getLat(0));

        assertEquals(1, nodes.getKeysVals(0));
        assertEquals(1, nodes.getKeysVals(1));
        assertEquals(0, nodes.getKeysVals(2));
    }

    @Test
    public void testDeltaCoding() {
        String str = "test";
        Node node1 = new Node(3, 20.0, 60.0);
        node1.getTags().put(str, str);
        Node node2 = new Node(6, 30.0, 20.0);
        node2.getTags().put(str, str);
        Node node3 = new Node(2, 60.0, 30.0);
        node3.getTags().put(str, str);

        DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
        testedObject.add(node1);
        testedObject.add(node2);
        testedObject.add(node3);

        Osmformat.PrimitiveGroup actual = testedObject.write().build();

        Osmformat.DenseNodes nodes = actual.getDense();

        assertEquals(3, nodes.getId(0));
        assertEquals(3, nodes.getId(1));
        assertEquals(-4, nodes.getId(2));

        assertEquals(200000000, nodes.getLat(0));
        assertEquals(100000000, nodes.getLat(1));
        assertEquals(300000000, nodes.getLat(2));

        assertEquals(600000000, nodes.getLon(0));
        assertEquals(-400000000, nodes.getLon(1));
        assertEquals(100000000, nodes.getLon(2));
    }
}