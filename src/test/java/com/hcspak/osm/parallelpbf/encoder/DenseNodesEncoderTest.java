package com.hcspak.osm.parallelpbf.encoder;

import com.hcspak.osm.parallelpbf.TestObjectsFactory;
import com.hcspak.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DenseNodesEncoderTest {
  private StringTableEncoder stringEncoder;

  @BeforeEach
  public void setUp() {
    stringEncoder = new StringTableEncoder();
  }

  @Test
  public void testNodeSize() {
    DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
    testedObject.add(TestObjectsFactory.node());

    assertEquals(36, testedObject.estimateSize());
  }

  @Test
  public void testWrite() {
    DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
    testedObject.add(TestObjectsFactory.node());
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
  public void testTaglessWrite() {
    Node node = TestObjectsFactory.node();
    node.getTags().clear();
    DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
    testedObject.add(node);
    testedObject.add(node);
    Osmformat.PrimitiveGroup actual = testedObject.write().build();

    Osmformat.DenseNodes nodes = actual.getDense();
    assertEquals(0, nodes.getKeysValsCount());
  }

  @Test
  public void testTagMixWrite() {
    Node node = TestObjectsFactory.node();
    node.getTags().clear();
    DenseNodesEncoder testedObject = new DenseNodesEncoder(stringEncoder);
    testedObject.add(node);
    testedObject.add(TestObjectsFactory.node());
    Osmformat.PrimitiveGroup actual = testedObject.write().build();

    Osmformat.DenseNodes nodes = actual.getDense();
    assertEquals(4, nodes.getKeysValsCount());
    assertEquals(0, nodes.getKeysVals(0));
    assertEquals(1, nodes.getKeysVals(1));
    assertEquals(1, nodes.getKeysVals(2));
    assertEquals(0, nodes.getKeysVals(3));
  }

  @Test
  public void testDeltaCoding() {
    Node node1 = new Node(3, null, new HashMap<>(), 20.0, 60.0);
    node1.getTags().put(TestObjectsFactory.testTag, TestObjectsFactory.testTag);
    Node node2 = new Node(6, null, new HashMap<>(), 30.0, 20.0);
    node2.getTags().put(TestObjectsFactory.testTag, TestObjectsFactory.testTag);
    Node node3 = new Node(2, null, new HashMap<>(), 60.0, 30.0);
    node3.getTags().put(TestObjectsFactory.testTag, TestObjectsFactory.testTag);

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