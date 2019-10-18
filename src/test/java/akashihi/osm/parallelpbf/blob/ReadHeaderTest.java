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

package akashihi.osm.parallelpbf.blob;

import crosby.binary.Fileformat;
import lombok.var;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("BlobReader")
class ReadHeaderTest {
    @Test
    void testReadHeader() {
        var testHeader = Fileformat.BlobHeader.newBuilder()
                .setType("OSMHeader")
                .setDatasize(1024).build();

        var blobStream = new ByteArrayInputStream(testHeader.toByteArray());

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlobHeader(testHeader.getSerializedSize());

        assertTrue(actual.isPresent());
        assertEquals(testHeader.getType(), actual.get().getType());
        assertEquals(testHeader.getDatasize(), actual.get().getSize().intValue());
    }

    @Test
    void testReadHeaderTooBig() {
        var testHeader = Fileformat.BlobHeader.newBuilder()
                .setType("OSMHeader")
                .setDatasize(33 * 1024 * 1024).build();

        var blobStream = new ByteArrayInputStream(testHeader.toByteArray());

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlobHeader(testHeader.getSerializedSize());

        assertFalse(actual.isPresent());
    }

    @Test
    void testReadHeaderInvalidFormat() throws IOException {
        var blobStream = IOUtils.toInputStream("test blob", "UTF-8");

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlobHeader(4);

        assertFalse(actual.isPresent());
    }
}