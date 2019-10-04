package akashihi.osm.parallelpbf;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Fileformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public abstract class OSMReader implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(OSMReader.class);

    private final byte[] blob;
    private final Semaphore tasksLimiter;

    public OSMReader(byte[] blob, Semaphore tasksLimiter) {
        this.blob = blob;
        this.tasksLimiter = tasksLimiter;
    }

    @Override
    public void run() {
        try {
            Fileformat.Blob blobData = Fileformat.Blob.parseFrom(blob);
            byte[] payload;
            if (blobData.hasZlibData()) {
                Inflater decompresser = new Inflater();
                decompresser.setInput(blobData.getZlibData().toByteArray());
                payload = new byte[blobData.getRawSize()];
                int uncompressedSize = decompresser.inflate(payload);
                if (uncompressedSize != blobData.getRawSize()) {
                    logger.error("Expected {} bytes after decompression, but got {}", blobData.getRawSize(), uncompressedSize);
                    throw new RuntimeException("Invalid blob payload size");
                }
            } else if (blobData.hasRaw()) {
                payload = blobData.getRaw().toByteArray();
            } else {
                throw new RuntimeException("Only RAW or ZLib blob formats are supported");
            }
            this.read(payload);
        } catch (InvalidProtocolBufferException | DataFormatException e) {
            logger.error("Error parsing Blob: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            tasksLimiter.release();
        }
    }

    protected byte[] getBlob() {
        return blob;
    }

    protected abstract void read(byte[] message);
}
