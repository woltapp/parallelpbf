package com.wolt.osm.parallelpbf.io;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wolt.osm.parallelpbf.TestObjectsFactory;
import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.encoder.OsmEncoder;
import com.wolt.osm.parallelpbf.entity.*;
import crosby.binary.Osmformat;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
            super(id);
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
        while(!queue.isEmpty()) {
            Thread.sleep(1);
        }
        testedObject.interrupt();
        testedObject.join();
    }

    @Test
    void testWriteStrings() throws InterruptedException, InvalidProtocolBufferException {
        writeEntity(TestObjectsFactory.way());

        byte[] blob = output.toByteArray();

        Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

        Osmformat.StringTable stringTable  = block.getStringtable();
        assertEquals(ByteString.EMPTY, stringTable.getS(0));
        assertEquals(TestObjectsFactory.testTag, stringTable.getS(1).toStringUtf8());
    }

    @Test
    void testNodeSetsGranularity() throws InterruptedException, InvalidProtocolBufferException {
        writeEntity(TestObjectsFactory.node());

        byte[] blob = output.toByteArray();

        Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

        assertEquals(OsmEncoder.GRANULARITY, block.getGranularity());
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
}