package com.wolt.osm.parallelpbf.blob;

import com.google.protobuf.ByteString;
import crosby.binary.Fileformat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

@RequiredArgsConstructor
@Slf4j
public class BlobWriter {
    /**
     * Output data stream.
     */
    private final OutputStream output;

    public boolean write(byte[] blob) {
        return write(blob, BlobInformation.TYPE_OSM_DATA);
    }

    public boolean write(byte[] blob, String type) {
        if (!BlobInformation.TYPE_OSM_DATA.equals(type) && !BlobInformation.TYPE_OSM_HEADER.equals(type)) {
            log.error("Unsupported Blob type: {}", type);
            return false;
        }

        // Compress blob
        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
        byte[] compressedBlob = new byte[blob.length];
        compresser.setInput(blob);
        compresser.finish();
        int compressedBlobLength = compresser.deflate(compressedBlob);
        compresser.end();

        // Form DataBlob
        byte[] dataBlob = Fileformat.Blob.newBuilder()
                .setRawSize(blob.length)
                .setZlibData(ByteString.copyFrom(compressedBlob, 0, compressedBlobLength))
                .build().toByteArray();

        // Form headerBlob
        byte[] headerBlob = Fileformat.BlobHeader.newBuilder()
                .setType(type)
                .setDatasize(dataBlob.length)
                .build().toByteArray();

        // Get size of the headerBlob
        byte[] size = ByteBuffer.allocate(4).putInt(headerBlob.length).array();

        // Write it to the output stream
        synchronized (output) {
            try {
                output.write(size);
                output.write(headerBlob);
                output.write(dataBlob);
            } catch (IOException e) {
                log.error("Error while writing data blob: {}", e.getMessage(), e);
                return false;
            }
        }
        return true;
    }
}
