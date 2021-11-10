/*
 * This file is part of parallelpbf.
 *
 *     parallelpbf is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ParallelBinaryParserIT {
    /* Reader specific part */
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

    /* Writer specific part */
    private ParallelBinaryWriter writer;

    private void writeNodes(Node node) {
        writer.write(node);
    }

    private void writeWays(Way way) {
        writer.write(way);
    }

    private void writeRelations(Relation relation) {
        writer.write(relation);
    }

    @SneakyThrows
    private void closeOnComplete() {
        writer.close();
    }

    /* Shared code */
    private void parse(InputStream input) {
        new ParallelBinaryParser(input, 1)
                .onNode(nodeChecker)
                .onWay(wayChecker)
                .onRelation(relationsChecker)
                .parse();
    }

    private void testSimpleNode() {
        assertEquals(51.7636027, simpleNode.getLat(), 0.0000001);
        assertEquals(-0.22875700000000002, simpleNode.getLon(), 0.0000001);
        assertTrue(simpleNode.getTags().isEmpty());
    }

    private void testTaggedNode() {
        assertEquals(51.76511770000001, taggedNode.getLat(), 0.0000001);
        assertEquals(-0.23366680000000006, taggedNode.getLon(), 0.0000001);
        assertFalse(taggedNode.getTags().isEmpty());
        assertTrue(taggedNode.getTags().containsKey("name"));
        assertEquals("Oaktree Close", taggedNode.getTags().get("name"));
        assertTrue(taggedNode.getTags().containsKey("highway"));
        assertEquals("bus_stop", taggedNode.getTags().get("highway"));

        assertEquals("NaPTAN", taggedNode.getInfo().getUsername());
        assertEquals(1, taggedNode.getInfo().getVersion());
        assertEquals(2539009, taggedNode.getInfo().getChangeset());
        assertEquals(104459, taggedNode.getInfo().getUid());
        assertEquals(1253397762000L, taggedNode.getInfo().getTimestamp());
    }

    private void testWay() {
        assertEquals(Arrays.asList(1709246789L, 1709246746L, 1709246741L, 1709246791L), taggedWay.getNodes());
        assertFalse(taggedWay.getTags().isEmpty());
        assertTrue(taggedWay.getTags().containsKey("highway"));
        assertEquals("footway", taggedWay.getTags().get("highway"));
    }

    public void testRelation() {
        Optional<RelationMember> member = taggedRelation.getMembers().stream().filter(m -> m.getId() == 25896432).findAny();
        assertTrue(member.isPresent());
        assertEquals("forward", member.get().getRole());
        Assertions.assertEquals(RelationMember.Type.WAY, member.get().getType());
        assertFalse(taggedRelation.getTags().isEmpty());
        assertTrue(taggedRelation.getTags().containsKey("route"));
        assertEquals("bicycle", taggedRelation.getTags().get("route"));
        assertNotNull(taggedRelation.getInfo());
        assertEquals("Mauls", taggedRelation.getInfo().getUsername());
    }

    @Test
    void testParser() {
        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("sample.pbf");
        parse(input);

        testSimpleNode();
        assertEquals(234999, simpleNode.getInfo().getUid());
        assertEquals("Nicholas Shanks", simpleNode.getInfo().getUsername());
        assertEquals(1, simpleNode.getInfo().getVersion());
        assertEquals(1267144226000L, simpleNode.getInfo().getTimestamp());
        assertEquals(3977001, simpleNode.getInfo().getChangeset());
        assertTrue(simpleNode.getInfo().isVisible());

        testTaggedNode();
        assertEquals(104459, taggedNode.getInfo().getUid());
        assertEquals("NaPTAN", taggedNode.getInfo().getUsername());
        assertEquals(1, taggedNode.getInfo().getVersion());
        assertEquals(1253397762000L, taggedNode.getInfo().getTimestamp());
        assertEquals(2539009, taggedNode.getInfo().getChangeset());
        assertTrue(taggedNode.getInfo().isVisible());

        testWay();
        assertEquals(470302, taggedWay.getInfo().getUid());
        assertEquals("Kjc", taggedWay.getInfo().getUsername());
        assertEquals(1, taggedWay.getInfo().getVersion());
        assertEquals(1334007464L, taggedWay.getInfo().getTimestamp());
        assertEquals(11245909, taggedWay.getInfo().getChangeset());
        assertFalse(taggedWay.getInfo().isVisible());

        testRelation();
        assertEquals(24119, taggedRelation.getInfo().getUid());
        assertEquals("Mauls", taggedRelation.getInfo().getUsername());
        assertEquals(81, taggedRelation.getInfo().getVersion());
        assertEquals(1337419064L, taggedRelation.getInfo().getTimestamp());
        assertEquals(11640673, taggedRelation.getInfo().getChangeset());
        assertFalse(taggedRelation.getInfo().isVisible());
    }

    @Test
    void testWriter() throws IOException {
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
                .onNode(this::writeNodes)
                .onWay(this::writeWays)
                .onRelation(this::writeRelations)
                .parse();
        output.close();

        InputStream written = new FileInputStream(outputFile);
        parse(written);

        testSimpleNode();
        testTaggedNode();
        testWay();
        testParser();
    }

    @Test
    void testExceptionProcessing() {
        final AtomicInteger completedCount = new AtomicInteger();

        final AtomicInteger nodeCount = new AtomicInteger();
        assertThrows(RuntimeException.class, () -> {
            try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf")) {
                new ParallelBinaryParser(input, 5).onComplete(completedCount::incrementAndGet).onNode((Node node) -> {
                    nodeCount.incrementAndGet();
                    if (nodeCount.get() > 5) {
                        throw new RuntimeException("Problem processing node!");
                    }
                }).onWay(way -> {
                }).onRelation(relation -> {
                }).parse();
            }
        });

        final AtomicInteger wayCount = new AtomicInteger();
        assertThrows(RuntimeException.class, () -> {
            try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf")) {
                new ParallelBinaryParser(input, 5).onComplete(completedCount::incrementAndGet).onNode(node -> {
                }).onWay(way -> {
                    wayCount.incrementAndGet();
                    if (wayCount.get() > 5) {
                        throw new RuntimeException("Problem processing way!");
                    }
                }).onRelation(relation -> {
                }).parse();
            }
        });

        final AtomicInteger relationCount = new AtomicInteger();
        assertThrows(RuntimeException.class, () -> {
            try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf")) {
                new ParallelBinaryParser(input, 5).onComplete(completedCount::incrementAndGet).onNode(node -> {
                }).onWay(way -> {
                }).onRelation(relation -> {
                    relationCount.incrementAndGet();
                    if (relationCount.get() > 2) {
                        throw new RuntimeException("Problem processing relation!");
                    }
                }).parse();
            }
        });

        assertEquals(0, completedCount.get());
    }

}
