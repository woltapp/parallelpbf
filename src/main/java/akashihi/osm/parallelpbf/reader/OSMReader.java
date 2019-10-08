package akashihi.osm.parallelpbf.reader;

import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Fileformat;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Base class for Blob handlers.
 * Provides blob decompression and runs proper decoder on a
 * raw blob.
 *
 * This class have no shared context, thus can be safely
 * executed on parallel.
 */
@Slf4j
public abstract class OSMReader implements Runnable {
    /**
     * Incoming blob to process.
     */
    private final byte[] blob;

    /**
     * Part of throttling system.
     * Reader takes the semaphore while running
     * and releases on completion. Therefore caller
     * may wait for some number of semaphores to be available and
     * do not overload thread pool.
     */
    private final Semaphore tasksLimiter;

    /**
     * Sets base parameters.
     * @param blobValue The blob to parse.
     * @param tasksLimiterValue Task limiting semaphore.
     */
    @SuppressWarnings("EI_EXPOSE_REP2")
    OSMReader(final byte[] blobValue, final Semaphore tasksLimiterValue) {
        this.blob = blobValue;
        this.tasksLimiter = tasksLimiterValue;
    }

    /**
     * Parses blob data by decompressing it, if needed and passing
     * to read() function.
     *
     * @throws RuntimeException if blob is unsupported format or other error did happened.
     */
    @Override
    public void run() {
        try {
            Fileformat.Blob blobData = Fileformat.Blob.parseFrom(blob);
            byte[] payload;
            if (blobData.hasZlibData()) {
                payload = decompress(blobData);
            } else if (blobData.hasRaw()) {
                payload = blobData.getRaw().toByteArray();
            } else {
                throw new RuntimeException("Only RAW or ZLib blob formats are supported");
            }
            this.read(payload);
        } catch (InvalidProtocolBufferException | DataFormatException e) {
            log.error("Error parsing Blob: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            tasksLimiter.release();
        }
    }

    /**
     * Decompresses blob using Zlib algorithm.
     * @param blobData binary data to decompress.
     * @return uncompressed raw blob data
     * @throws DataFormatException in case of decompression error.
     * @throws RuntimeException if decompressed size differs from expected one.
     */
    private byte[] decompress(final Fileformat.Blob blobData) throws DataFormatException {
        byte[] payload;
        Inflater decompresser = new Inflater();
        decompresser.setInput(blobData.getZlibData().toByteArray());
        payload = new byte[blobData.getRawSize()];
        int uncompressedSize = decompresser.inflate(payload);
        if (uncompressedSize != blobData.getRawSize()) {
            log.error("Expected {} bytes after decompression, but got {}", blobData.getRawSize(), uncompressedSize);
            throw new RuntimeException("Invalid blob payload size");
        }
        return payload;
    }

    /**
     * Actual decoding should happen here.
     * @param message Raw OSMHeader or OSMData blob.
     */
    protected abstract void read(byte[] message);
}
