package com.wolt.osm.parallelpbf.blob;

import com.google.protobuf.ByteString;
import crosby.binary.Fileformat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

/**
 * Fileformat writer. Should be shared between all the OSMWriters
 * as it owns single OutputStream.
 *
 * Accepts blob to write with the type and serializes it to the
 * output stream. Writing to the stream is synchronized, so it
 * is thread safe.
 */
@RequiredArgsConstructor
@Slf4j
public class BlobWriter {
    /**
     * Size of a int, prepending the HeaderBlock.
     */
    private static final int INT_SIZE = 4;
    /**
     * Output data stream.
     */
    private final OutputStream output;

    /**
     * Shortcut for writing OSMData block.
     * @param blob binary blob to write.
     * @return false in case of error, true otherwise.
     */
    public boolean write(final byte[] blob) {
        return write(blob, BlobInformation.TYPE_OSM_DATA);
    }

    /**
     * Writes blob to the OutputStream. Blob will be compressed, if
     * applicable, prepended with HeaderBlob and its size.
     * OutputFileStream will be locked during IO operation.
     * @param blob biary blob to write.
     * @param type Type of the blob.
     * @return false in case of error, true otherwise.
     */
    public boolean write(final byte[] blob, final String type) {
        boolean compress;
        if (BlobInformation.TYPE_OSM_DATA.equals(type)) {
            compress = true;
        } else if (BlobInformation.TYPE_OSM_HEADER.equals(type)) {
            compress = false;
        } else {
            log.error("Unsupported Blob type: {}", type);
            return false;
        }

        // Form DataBlob
        byte[] dataBlob;
        if (compress) {
            Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
            byte[] compressedBlob = new byte[blob.length];
            compressor.setInput(blob);
            compressor.finish();
            int compressedBlobLength = compressor.deflate(compressedBlob);
            compressor.end();
            dataBlob = Fileformat.Blob.newBuilder()
                    .setRawSize(blob.length)
                    .setZlibData(ByteString.copyFrom(compressedBlob, 0, compressedBlobLength))
                    .build().toByteArray();
        } else {
            dataBlob = Fileformat.Blob.newBuilder()
                    .setRaw(ByteString.copyFrom(blob))
                    .build().toByteArray();
        }

        // Form headerBlob
        byte[] headerBlob = Fileformat.BlobHeader.newBuilder()
                .setType(type)
                .setDatasize(dataBlob.length)
                .build().toByteArray();

        // Get size of the headerBlob
        byte[] size = ByteBuffer.allocate(INT_SIZE).putInt(headerBlob.length).array();

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
