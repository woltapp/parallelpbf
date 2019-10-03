package akashihi.osm.parallelpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ParallelBinaryParser {
    private static Logger logger = LoggerFactory.getLogger(ParallelBinaryParser.class);
    /**
     * Relations processing callback. Must be reentrant.
     */
    private Consumer<List<Osmformat.Relation>> parseRelations;

    /**
     * Nodes processing callback. Must be reentrant.
     */
    private Consumer<List<Osmformat.Node>> parseNodes;

    /**
     * Ways processing callback. Must be reentrant.
     */
    private Consumer<List<Osmformat.Way>> parseWays;

    /**
     * Header processing callback. Must be reentrant.
     */
    private Consumer<List<Osmformat.HeaderBlock>> parseHeader;

    /**
     * Callback that will be called, when all blocks are parsed.
     */
    private Consumer<Void> complete;

    /**
     * Input data stream
     */
    private final InputStream input;

    protected Optional<byte[]> readFromStream(int bytesToRead) {
        byte[] buffer = new byte[bytesToRead];
        try {
            int bytesRead = input.read(buffer);
            if (bytesRead != bytesToRead) {
                return Optional.empty();
            }
        } catch (IOException e) {
            logger.error("Error reading from the stream: {}", e.getMessage(), e);
            return Optional.empty();
        }
        return Optional.of(buffer);
    }

    /**
     * Reads next blob header length from the current stream position.
     * As blob header length is just 4 bytes in network byte order, this functions makes no
     * checks and will return garbage if called within a wrong stream position
     *
     * @return length of next block header or 0 if can't be read.
     */
    protected int readBlobHeaderLength() {
        final int MAX_HEADER_SIZE = 64 * 1024;
        final int SIZE_FIELD_LENGTH = 4;
        Optional<byte[]> blobHeaderLengthBuffer = readFromStream(SIZE_FIELD_LENGTH);
        if (!blobHeaderLengthBuffer.isPresent()) {
            return 0;
        }
        ByteBuffer blobHeaderLengthWrapped = ByteBuffer.wrap(blobHeaderLengthBuffer.get());
        int blobHeaderLength = blobHeaderLengthWrapped.getInt();
        if (blobHeaderLength > MAX_HEADER_SIZE) {
            logger.error("BlobHeader size is too big: {}", blobHeaderLength);
            return 0;
        }
        logger.info("Read BlobHeaderLength: {}", blobHeaderLength);
        return blobHeaderLength;
    }

    /**
     * Reads next blob header from the current stream position. Size of the header is
     * specified in the parameters. As BlobHeader is a protobuf entity, basic validity checking
     * is made and 0 will be returned in case of failure. Same 0 will be returned if header can't be read fully
     * or eof is reached.
     * @param headerLength Number of bytes to read and interpret as BlobHeader
     * @return Size of the following Blob in bytes or 0 in case of read error.
     */
    protected int readBlobHeader(int headerLength) {
        final int MAX_BLOB_SIZE = 32 * 1024 *1024;
        Optional<byte[]> blobHeaderBuffer = readFromStream(headerLength);
        if (!blobHeaderBuffer.isPresent()) {
            return 0;
        }

        Fileformat.BlobHeader header = null;
        try {
            header = Fileformat.BlobHeader.parseFrom(blobHeaderBuffer.get());
            logger.info("Got BlobHeader with type: {}, data size: {}", header.getType(), header.getDatasize());
            if (header.getDatasize() > MAX_BLOB_SIZE) {
                logger.error("Blob size is too big: {}", header.getDatasize());
                return 0;
            }
            return header.getDatasize();
        } catch (InvalidProtocolBufferException e) {
            logger.info("Failed to parse BlobHeader: {}", e.getMessage(), e);
            return 0;
        }
    }

    protected Optional<byte[]> readBlob(int blobLength) {
        return readFromStream(blobLength);
    }

    public ParallelBinaryParser(InputStream input, int noThreads) {
        this.input = input;
    }

    public void setRelationsCallback(Consumer<List<Osmformat.Relation>> parseRelations) {
        this.parseRelations = parseRelations;
    }

    public void setNodesCallback(Consumer<List<Osmformat.Node>> parseNodes) {
        this.parseNodes = parseNodes;
    }

    public void setsetWaysCallback(Consumer<List<Osmformat.Way>> parseWays) {
        this.parseWays = parseWays;
    }

    public void setHeaderCallback(Consumer<List<Osmformat.HeaderBlock>> parseHeader) {
        this.parseHeader = parseHeader;
    }

    public void setCompleteCallback(Consumer<Void> complete) {
        this.complete = complete;
    }

    public void parse() {
        boolean eof = false;
        do {
            int headerLength = readBlobHeaderLength();
            if (headerLength > 0) {
                int blobLength = readBlobHeader(headerLength);
                if (blobLength > 0) {
                    readBlob(blobLength);
                } else {
                    eof = true;
                }
            } else {
                eof = true;
            }
        } while (!eof);
    }
}
