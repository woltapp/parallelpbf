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

package org.akashihi.osm.parallelpbf.blob;

import lombok.var;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@Tag("BlobReader")
class ReadHeaderLengthTest {
    @Test
    void testReadBlob() throws IOException {
        final Integer testLength = 0xDEADCAFE;
        var bytes = ByteBuffer.allocate(4).putInt(testLength).array();

        var blobStream = new ByteArrayInputStream(bytes);

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlobHeaderLength();

        assertTrue(actual.isPresent());
        assertEquals(testLength, actual.get());
    }

    @Test
    void testReadBlobTooBig() throws IOException {
        final Integer testLength = 65 * 1024;
        var bytes = ByteBuffer.allocate(4).putInt(testLength).array();

        var blobStream = new ByteArrayInputStream(bytes);

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlobHeaderLength();

        assertFalse(actual.isPresent());
    }
}