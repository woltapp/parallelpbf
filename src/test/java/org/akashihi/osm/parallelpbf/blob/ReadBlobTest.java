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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("BlobReader")
class ReadBlobTest {

    @Test
    void testReadBlob() throws IOException {
        final String testString = "test blob";

        var blobStream = IOUtils.toInputStream(testString, "UTF-8");

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlob(testString.length());

        assertTrue(actual.isPresent());
        assertEquals(testString, new String(actual.get(), StandardCharsets.UTF_8));
    }

    @Test
    void testReadBlobShort() throws IOException {
        final String testString = "test blob";

        var blobStream = IOUtils.toInputStream(testString, "UTF-8");

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlob(testString.length()*2);

        assertFalse(actual.isPresent());
    }

    @Test
    void testReadBlobFailure() throws IOException {
        final String testString = "test blob";

        InputStream blobStream = mock(InputStream.class);
        expect(blobStream.read(anyObject())).andStubThrow(new IOException());
        replay(blobStream);

        var testedObject = new BlobReader(blobStream);
        var actual = testedObject.readBlob(testString.length());

        assertFalse(actual.isPresent());
    }
}