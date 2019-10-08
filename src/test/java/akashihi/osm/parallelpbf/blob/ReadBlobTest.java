package akashihi.osm.parallelpbf.blob;

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