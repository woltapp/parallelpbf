package akashihi.osm.parallelpbf;

import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
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
     * Reads next blob header length from the current stream position.
     * As blob header length is just 4 bytes in network byte order, this functions makes no
     * checks and will return garbage if called within a wrong stream position
     * @return length of next block header or 0 if can't be read.
     */
    protected int readBlobHeaderLength() {
        try {
            byte[] blobHeaderLengthBuffer = new byte[4];
            int bytesRead = input.read(blobHeaderLengthBuffer);
            if (bytesRead != blobHeaderLengthBuffer.length) {
                return 0;
            }
            ByteBuffer blobHeaderLengthWrapped = ByteBuffer.wrap(blobHeaderLengthBuffer);
            int blobHeaderLength = blobHeaderLengthWrapped.getInt();
            logger.info("Read BlobHeaderLength: {}", blobHeaderLength);
            return blobHeaderLength;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Input data stream
     */
    private final InputStream input;

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
        do {
            int blobHeaderLength = readBlobHeaderLength();
            if (blobHeaderLength > 0) {
                logger.info("Trying to get header");
                //int blobLength = readBlobHeader(blobHeaderLength);
            }
        } while (false);
    }
}
