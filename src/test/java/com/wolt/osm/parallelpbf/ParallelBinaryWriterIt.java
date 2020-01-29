package com.wolt.osm.parallelpbf;

import com.wolt.osm.parallelpbf.entity.Node;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.RelationMember;
import com.wolt.osm.parallelpbf.entity.Way;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ParallelBinaryWriterIt {
    private Node simpleNode;
    private Node taggedNode;

    private Way taggedWay;

    private Relation taggedRelation;

    Consumer<Node> nodeChecker = (node) -> {
        if (node.getId() == 653970877) {
            simpleNode = node;
        }
        if (node.getId() == 502550970) {
            taggedNode = node;
        }
    };

    Consumer<Way> wayChecker = (way) -> {
        if (way.getId() == 158788812) {
            taggedWay = way;
        }
    };

    Consumer<Relation> relationsChecker = relation -> {
        if (relation.getId() == 31640) {
            taggedRelation = relation;
        }
    };

    private ParallelBinaryWriter writer;

    private void processNodes(Node node) {
        writer.write(node);
    }

    private void processWays(Way way) {
        writer.write(way);
    }

    private void processRelations(Relation relation) {
        writer.write(relation);
    }

    @SneakyThrows
    private void closeOnComplete() {
        writer.close();
    }

    @Test
    void testParser() throws IOException {
        String outputFilename = System.getProperty("java.io.tmpdir")+"/parallel.pbf";
        File outputFile = new File(outputFilename);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();
        OutputStream output = new FileOutputStream(outputFile);

        writer = new ParallelBinaryWriter(output,1, null);
        writer.start();

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf");
        new ParallelBinaryParser(input, 1)
                .onComplete(this::closeOnComplete)
                .onNode(this::processNodes)
                .onWay(this::processWays)
                .onRelation(this::processRelations)
                .parse();
        output.close();

        InputStream written = new FileInputStream(outputFile);

        new ParallelBinaryParser(written, 1)
                .onNode(nodeChecker)
                .onWay(wayChecker)
                .onRelation(relationsChecker)
                .parse();

        assertEquals(51.7636027, simpleNode.getLat(), 0.0000001);
        assertEquals(-0.22875700000000002, simpleNode.getLon(), 0.0000001);
        assertTrue(simpleNode.getTags().isEmpty());

        assertEquals(51.76511770000001, taggedNode.getLat(), 0.0000001);
        assertEquals(-0.23366680000000006, taggedNode.getLon(), 0.0000001);
        assertFalse(taggedNode.getTags().isEmpty());
        assertTrue(taggedNode.getTags().containsKey("name"));
        assertEquals("Oaktree Close", taggedNode.getTags().get("name"));
        assertTrue(taggedNode.getTags().containsKey("highway"));
        assertEquals("bus_stop", taggedNode.getTags().get("highway"));


        assertEquals(Arrays.asList(1709246789L, 1709246746L, 1709246741L, 1709246791L), taggedWay.getNodes());
        assertFalse(taggedWay.getTags().isEmpty());
        assertTrue(taggedWay.getTags().containsKey("highway"));
        assertEquals("footway", taggedWay.getTags().get("highway"));

        Optional<RelationMember> member = taggedRelation.getMembers().stream().filter(m -> m.getId() == 25896432).findAny();
        assertTrue(member.isPresent());
        assertEquals("forward", member.get().getRole());
        Assertions.assertEquals(RelationMember.Type.WAY, member.get().getType());
        assertFalse(taggedRelation.getTags().isEmpty());
        assertTrue(taggedRelation.getTags().containsKey("route"));
        assertEquals("bicycle", taggedRelation.getTags().get("route"));
    }
}
