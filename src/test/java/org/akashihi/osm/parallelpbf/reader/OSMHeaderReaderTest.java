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

import org.akashihi.osm.parallelpbf.entity.BoundBox;
import org.akashihi.osm.parallelpbf.entity.Header;
import com.google.protobuf.ByteString;
import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class OSMHeaderReaderTest {

    private Semaphore limiter;

    @BeforeEach
    void setUp() {
        limiter = new Semaphore(0);
    }

    @Test
    void testInvalidBlob() {
        var blob = Fileformat.Blob.newBuilder().setRaw(ByteString.copyFromUtf8("failme")).build().toByteArray();

        var testedObject = new OSMHeaderReader(blob, limiter, null, null);
        assertThrows(RuntimeException.class, testedObject::run);
    }

    @Test
    void testFailOnRequiredFeature() {
        var header = Osmformat.HeaderBlock.newBuilder()
                .addRequiredFeatures("failme")
                .build().toByteArray();
        var blob = Fileformat.Blob.newBuilder().setRaw(ByteString.copyFrom(header)).build().toByteArray();

        var testedObject = new OSMHeaderReader(blob, limiter, null, null);
        assertThrows(RuntimeException.class, testedObject::run);
    }

    @Test
    void testHeaderParse() {
        var headerMessage = Osmformat.HeaderBlock.newBuilder()
                .addRequiredFeatures("DenseNodes")
                .addOptionalFeatures("test_feature")
                .setSource("test")
                .setWritingprogram("junit5")
                .build().toByteArray();
        var blob = Fileformat.Blob.newBuilder().setRaw(ByteString.copyFrom(headerMessage)).build().toByteArray();

        Consumer<Header> checker = (header) -> {
          assertEquals("DenseNodes", header.getRequiredFeatures().get(0));
          assertEquals("test_feature", header.getOptionalFeatures().get(0));
          assertEquals("test", header.getSource());
          assertEquals("junit5", header.getWritingProgram());
        };
        var testedObject = new OSMHeaderReader(blob, limiter, checker, null);
        testedObject.run();
    }

    @Test
    void testNoBoundBox() {
        var headerMessage = Osmformat.HeaderBlock.newBuilder()
                .addRequiredFeatures("DenseNodes")
                .build().toByteArray();
        var blob = Fileformat.Blob.newBuilder().setRaw(ByteString.copyFrom(headerMessage)).build().toByteArray();

        Consumer<BoundBox> checker = (bbox) -> {
            fail("BoundBox is missing");
        };
        var testedObject = new OSMHeaderReader(blob, limiter, null, checker);
        testedObject.run();
    }
    @Test
    void testBoundBoxParse() {
        var boundBoxMessage = Osmformat.HeaderBBox.newBuilder()
                .setLeft(1000000000)
                .setTop(2000000000)
                .setRight(3000000000L)
                .setBottom(4000000000L)
                .build();
        var headerMessage = Osmformat.HeaderBlock.newBuilder()
                .addRequiredFeatures("DenseNodes")
                .setBbox(boundBoxMessage)
                .build().toByteArray();
        var blob = Fileformat.Blob.newBuilder().setRaw(ByteString.copyFrom(headerMessage)).build().toByteArray();

        Consumer<BoundBox> checker = (bbox) -> {
            assertEquals(1, bbox.getLeft(), 0.1);
            assertEquals(2, bbox.getTop(), 0.1);
            assertEquals(3, bbox.getRight(), 0.1);
            assertEquals(4, bbox.getBottom(), 0.1);
        };
        var testedObject = new OSMHeaderReader(blob, limiter, null, checker);
        testedObject.run();
    }

}