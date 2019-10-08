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