package com.wolt.osm.parallelpbf.io;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.encoder.OsmEncoder;
import com.wolt.osm.parallelpbf.entity.*;
import crosby.binary.Osmformat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

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

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        writer = new BlobWriterMock(output);
        queue = new LinkedBlockingQueue<>();
    }

    @Test
    void testWriteStrings() throws InterruptedException, InvalidProtocolBufferException {
        String str = "test";
        Way way = new Way(1L);
        way.getTags().put(str, str);
        way.getNodes().add(3L);

        Thread testedObject = new Thread(new OSMWriter(writer, queue));
        testedObject.start();

        queue.put(way);
        while(!queue.isEmpty()) {
            Thread.sleep(1);
        }
        testedObject.interrupt();
        testedObject.join();

        byte[] blob = output.toByteArray();

        Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

        Osmformat.StringTable stringTable  = block.getStringtable();
        assertEquals(ByteString.EMPTY, stringTable.getS(0));
        assertEquals(str, stringTable.getS(1).toStringUtf8());
    }

    @Test
    void testNodeSetsGranularity() throws InterruptedException, InvalidProtocolBufferException {
        Node node = new Node(1, 100.0, 500.0);

        Thread testedObject = new Thread(new OSMWriter(writer, queue));
        testedObject.start();

        queue.put(node);
        while(!queue.isEmpty()) {
            Thread.sleep(1);
        }
        testedObject.interrupt();
        testedObject.join();

        byte[] blob = output.toByteArray();

        Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

        assertEquals(OsmEncoder.GRANULARITY, block.getGranularity());
        assertEquals(0, block.getLonOffset());
        assertEquals(0, block.getLatOffset());
    }

    @Test
    void testNoNodeNoGranularity() throws InterruptedException, InvalidProtocolBufferException {
        Way way = new Way(1L);
        way.getNodes().add(3L);


        Thread testedObject = new Thread(new OSMWriter(writer, queue));
        testedObject.start();

        queue.put(way);
        while(!queue.isEmpty()) {
            Thread.sleep(1);
        }
        testedObject.interrupt();
        testedObject.join();

        byte[] blob = output.toByteArray();

        Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

        assertFalse(block.hasGranularity());
        assertFalse(block.hasLatOffset());
        assertFalse(block.hasLonOffset());
    }

    @Test
    void testWriteRelation() throws InterruptedException, InvalidProtocolBufferException {
        RelationMember member = new RelationMember(2L, "test", RelationMember.Type.WAY);
        Relation relation = new Relation(1L);
        relation.getTags().put("test", "test");
        relation.getMembers().add(member);

        Thread testedObject = new Thread(new OSMWriter(writer, queue));
        testedObject.start();

        queue.put(relation);
        while(!queue.isEmpty()) {
            Thread.sleep(1);
        }
        testedObject.interrupt();
        testedObject.join();

        byte[] blob = output.toByteArray();

        Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(blob);

        assertEquals(1, block.getPrimitivegroupList().size());
        assertFalse(block.getPrimitivegroup(0).getRelationsList().isEmpty());
    }

    private static class FakeEntity extends OsmEntity {
        public FakeEntity(long id) {
            super(id);
        }
    }
    @Test
    void testNoInvalidEntities() throws InterruptedException {
        FakeEntity entity = new FakeEntity(1L);
        Thread testedObject = new Thread(new OSMWriter(writer, queue));
        testedObject.start();

        queue.put(entity);
        while(!queue.isEmpty()) {
            Thread.sleep(1);
        }
        testedObject.interrupt();
        testedObject.join();

        assertEquals(0, output.toByteArray().length);
    }
}