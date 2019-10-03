package akashihi.osm.parallelpbf;

import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public final class ParallelBinaryParser {
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

    private final ExecutorService executor;
    private final Semaphore tasksLimiter;
    private final BlobReader reader;

    /**
     * Processses blob with osm data asynchronously.
     * @param blob Blob to process
     * @param type Blob type, either OSMHeader or OSMData
     */
    protected void processDataBlob(byte[] blob, String type) {
        try {
            tasksLimiter.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            executor.execute(tasksLimiter::release);
        }catch (RejectedExecutionException e) {
            tasksLimiter.release();
            logger.error("Failed to start processing of blob");
        }
    }

    public ParallelBinaryParser(InputStream input, int noThreads) {
        reader = new BlobReader(input);
        executor = Executors.newFixedThreadPool(noThreads);
        tasksLimiter = new Semaphore(noThreads);
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
        Optional<byte[]> blob = Optional.empty();
        do {
            blob = reader.readBlobHeaderLength().flatMap(reader::readBlobHeader).flatMap(blobinfo -> reader.readBlob(blobinfo.getSize()));
        } while (blob.isPresent());
    }
}
