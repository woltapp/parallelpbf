package akashihi.osm.parallelpbf;

import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
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
     * @param information Blob's size and type
     * @return
     */
    protected Optional<? extends Future<?>> processDataBlob(BlobInformation information) {
        Optional<byte[]> blob = reader.readBlob(information.getSize());
        Optional<Runnable> handler = blob.flatMap(value -> {
            switch (information.getType()) {
                case "OSMHeader":
                    return Optional.of(new OSMHeaderReader(value, tasksLimiter));
                case "OSMData":
                    return Optional.of(new OSMDataReader(value, tasksLimiter));
                default:
                    return Optional.empty();
            }
        });
        return handler.flatMap(value -> {
            try {
                tasksLimiter.acquire();
                return Optional.of(executor.submit(value));
            } catch (InterruptedException e) {
                logger.error("Failed to acquire processing slot: {}", e.getMessage(), e);
                return Optional.empty();
            } catch (RejectedExecutionException e) {
                tasksLimiter.release();
                logger.error("Failed to start processing of blob: {}", e.getMessage(), e);
                return Optional.empty();
            }
        });
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
        Optional<? extends Future<?>> blob;
        do {
            blob = reader.readBlobHeaderLength().flatMap(reader::readBlobHeader).flatMap(this::processDataBlob);
        } while (blob.isPresent());
        executor.shutdown();
    }
}
