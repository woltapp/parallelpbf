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

package org.akashihi.osm.parallelpbf.reader;

import org.akashihi.osm.parallelpbf.TestObjectsFactory;
import org.akashihi.osm.parallelpbf.entity.Node;
import org.akashihi.osm.parallelpbf.entity.Relation;
import org.akashihi.osm.parallelpbf.entity.Way;
import com.google.protobuf.ByteString;
import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class OSMDataReaderTest {
    private Semaphore limiter;

    private Osmformat.PrimitiveGroup primitiveGroupMessage = Osmformat.PrimitiveGroup.newBuilder()
            .addNodes(TestObjectsFactory.nodeMessage)
            .setDense(TestObjectsFactory.denseNodesMessage)
            .addWays(TestObjectsFactory.wayMessage)
            .addRelations(TestObjectsFactory.relationMessage)
            .addChangesets(TestObjectsFactory.changesetMessage)
            .build();

    private Osmformat.PrimitiveBlock primitivesMessage = Osmformat.PrimitiveBlock.newBuilder()
            .addPrimitivegroup(primitiveGroupMessage)
            .setStringtable(TestObjectsFactory.stringTable)
            .setGranularity(1)
            .setDateGranularity(1)
            .setLatOffset(0)
            .setLonOffset(0)
            .build();

    private byte[] blob = Fileformat.Blob.newBuilder().setRaw(primitivesMessage.toByteString()).build().toByteArray();

    @BeforeEach
    void setUp() {
        limiter = new Semaphore(0);
    }

    @Test
    void testInvalidBlob() {
        var blob = Fileformat.Blob.newBuilder().setRaw(ByteString.copyFromUtf8("failme")).build().toByteArray();

        var testedObject = new OSMDataReader(blob, limiter, null, null, null, null);
        assertThrows(RuntimeException.class, testedObject::run);
    }

    @Test
    void testNodesParse() {
        Consumer<Node> checker = (node -> assertEquals(TestObjectsFactory.nodeMessage.getId(), node.getId()));
        var testedObject = new OSMDataReader(blob, limiter, checker, null, null, null);
        testedObject.run();
    }

    @Test
    void testWaysParse() {
        Consumer<Way> checker = (way -> assertEquals(TestObjectsFactory.wayMessage.getId(), way.getId()));
        var testedObject = new OSMDataReader(blob, limiter, null, checker, null, null);
        testedObject.run();
    }

    @Test
    void testRelationsParse() {
        Consumer<Relation> checker = (relation -> assertEquals(TestObjectsFactory.relationMessage.getId(), relation.getId()));
        var testedObject = new OSMDataReader(blob, limiter, null, null, checker, null);
        testedObject.run();
    }

    @Test
    void testChangesetsParse() {
        Consumer<Long> checker = (changeset -> assertEquals(TestObjectsFactory.changesetMessage.getId(), changeset.longValue()));
        var testedObject = new OSMDataReader(blob, limiter, null, null, null, checker);
        testedObject.run();
    }
}