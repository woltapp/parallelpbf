package com.hcspak.osm.parallelpbf.io;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hcspak.osm.parallelpbf.TestObjectsFactory;
import com.hcspak.osm.parallelpbf.blob.BlobWriter;
import com.hcspak.osm.parallelpbf.encoder.OsmEncoder;
import com.hcspak.osm.parallelpbf.entity.OsmEntity;
import crosby.binary.Osmformat;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OSMWriterTest {

  private ByteArrayOutputStream output;
  private BlobWriter writer;
  private LinkedBlockingQueue<OsmEntity> queue;

  private class BlobWriterMock extends BlobWriter {

    public BlobWriterMock(OutputStream output) {
      super(output);
    }

    @SneakyThrows
    @Override
    public boolean writeData(byte[] blob) {
      output.write(blob);
      return true;
    }
  }

  private static class FakeEntity extends OsmEntity {
    public FakeEntity(long id) {
      super(id, null, null);
    }
  }

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
    writer = new BlobWriterMock(output);
    queue = new LinkedBlockingQueue<>();
  }

  private void writeEntity(OsmEntity entity) throws InterruptedException {
    Thread testedObject = new Thread(new OSMWriter(writer, queue));
    testedObject.start();

    queue.put(entity);
    while (!queue.isEmpty()) {
      Thread.yield();
    }
    testedObject.interrupt();
    testedObject.join();
  }

  @Test
  void testWriteStrings() throws InterruptedException, InvalidProtocolBufferException {
    writeEntity(TestObjectsFactory.way());

    byte[] blob = output.toByteArray();

    Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

    Osmformat.StringTable stringTable = block.getStringtable();
    assertEquals(ByteString.EMPTY, stringTable.getS(0));
    assertEquals(TestObjectsFactory.testTag, stringTable.getS(1).toStringUtf8());
  }

  @Test
  void testNodeSetsGranularity() throws InterruptedException, InvalidProtocolBufferException {
    writeEntity(TestObjectsFactory.node());

    byte[] blob = output.toByteArray();

    Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

    Assertions.assertEquals(OsmEncoder.GRANULARITY, block.getGranularity());
    assertEquals(0, block.getLonOffset());
    assertEquals(0, block.getLatOffset());
  }

  @Test
  void testNoNodeNoGranularity() throws InterruptedException, InvalidProtocolBufferException {
    writeEntity(TestObjectsFactory.way());

    byte[] blob = output.toByteArray();

    Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

    assertFalse(block.hasGranularity());
    assertFalse(block.hasLatOffset());
    assertFalse(block.hasLonOffset());
  }

  @Test
  void testWriteRelation() throws InterruptedException, InvalidProtocolBufferException {
    writeEntity(TestObjectsFactory.relation());

    byte[] blob = output.toByteArray();

    Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

    assertEquals(1, block.getPrimitivegroupList().size());
    assertFalse(block.getPrimitivegroup(0).getRelationsList().isEmpty());
  }

  @Test
  void testNoInvalidEntities() throws InterruptedException {
    FakeEntity entity = new FakeEntity(1L);

    writeEntity(entity);

    assertEquals(0, output.toByteArray().length);
  }

  @Test
  @Tag("slow")
  void testFlushOnOverflow() throws InterruptedException {
    Thread testedObject = new Thread(new OSMWriter(writer, queue));
    testedObject.start();

    // We flush on 15*1024*1024, each node is 36 bytes + string table is 4 bytes
    //That means we have to write more than (15*1024*1024-4)/36 = 436907 nodes.
    for (int i = 0; i < 436908; ++i) {
      queue.put(TestObjectsFactory.node());
    }
    while (!queue.isEmpty()) {
      Thread.yield();
    }
    assertTrue(output.toByteArray().length > 4); //4 as is should contain more then just a test tag

    //Flush only there
    testedObject.interrupt();
    testedObject.join();
  }
}