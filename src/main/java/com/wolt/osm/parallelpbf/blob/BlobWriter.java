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
 * <p>
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
     * Blob writing helper. Adds headerBlob and size to the stream.
     * Stream is locked during output operation.
     * @param blob Blob to write.
     * @param type Type of that blob.
     * @return false in case of error, true otherwise.
     */
    private boolean write(final byte[] blob, final String type) {
        // Form headerBlob
        byte[] headerBlob = Fileformat.BlobHeader.newBuilder()
                .setType(type)
                .setDatasize(blob.length)
                .build().toByteArray();

        // Get size of the headerBlob
        byte[] size = ByteBuffer.allocate(INT_SIZE).putInt(headerBlob.length).array();

        // Write it to the output stream
        synchronized (output) {
            try {
                output.write(size);
                output.write(headerBlob);
                output.write(blob);
            } catch (IOException e) {
                log.error("Error while writing data blob: {}", e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * Writes data blob to the OutputStream. Blob will be compresed, prepended with HeaderBlob
     * and its size.
     * OutputFileStream will be locked during IO operation.
     *
     * @param blob binary blob to write.
     * @return false in case of error, true otherwise.
     */
    public boolean writeData(final byte[] blob) {
        // Form DataBlob
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        byte[] compressedBlob = new byte[blob.length];
        compressor.setInput(blob);
        compressor.finish();
        int compressedBlobLength = compressor.deflate(compressedBlob);
        compressor.end();
        byte[] dataBlob = Fileformat.Blob.newBuilder()
                .setRawSize(blob.length)
                .setZlibData(ByteString.copyFrom(compressedBlob, 0, compressedBlobLength))
                .build().toByteArray();

        return write(dataBlob, BlobInformation.TYPE_OSM_DATA);
    }

    /**
     * Writes header blob to the OutputStream. Blob will be  prepended with HeaderBlob
     * and its size.
     * OutputFileStream will be locked during IO operation.
     *
     * @param blob binary blob to write.
     * @return false in case of error, true otherwise.
     */
    public boolean writeHeader(final byte[] blob) {
        // Form DataBlob
        byte[] dataBlob = Fileformat.Blob.newBuilder()
                    .setRaw(ByteString.copyFrom(blob))
                    .build().toByteArray();

        return write(dataBlob, BlobInformation.TYPE_OSM_HEADER);
    }
}
