package akashihi.osm.parallelpbf.blob;

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