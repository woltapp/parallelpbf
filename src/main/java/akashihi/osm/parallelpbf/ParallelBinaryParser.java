package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.BoundBox;
import akashihi.osm.parallelpbf.entity.Header;
import akashihi.osm.parallelpbf.entity.Node;
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
    private Consumer<Node> parseNodes;

    /**
     * Ways processing callback. Must be reentrant.
     */
    private Consumer<List<Osmformat.Way>> parseWays;

    /**
     * Header processing callback. Must be reentrant.
     */
    private Consumer<Header> parseHeader;

    /**
     * Header processing callback. Must be reentrant.
     */
    private Consumer<BoundBox> parseBoundBox;

    /**
     * Callback that will be called, when all blocks are parsed.
     */
    private Runnable complete;

    private final ExecutorService executor;
    private final Semaphore tasksLimiter;
    private final BlobReader reader;

    private Optional<OSMReader> makeReaderForBlob(byte[] blob, BlobInformation information) {
        switch (information.getType()) {
            case "OSMHeader":
                return Optional.of(new OSMHeaderReader(blob, tasksLimiter, parseHeader, parseBoundBox));
            case "OSMData":
                return Optional.of(new OSMDataReader(blob, tasksLimiter, parseNodes));
            default:
                return Optional.empty();
        }
    }

    private Optional<? extends Future<?>> runReaderAsync(OSMReader reader) {
        try {
            tasksLimiter.acquire();
        } catch (InterruptedException e) {
            logger.error("Failed to acquire processing slot: {}", e.getMessage(), e);
            return Optional.empty();
        }
        try {
            return Optional.of(executor.submit(reader));
        } catch (RejectedExecutionException e) {
            tasksLimiter.release();
            logger.error("Failed to start processing of blob: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Processses blob with osm data asynchronously.
     *
     * @param information Blob's size and type
     * @return
     */
    protected Optional<? extends Future<?>> processDataBlob(BlobInformation information) {
        return reader.readBlob(information.getSize())
                .flatMap(value -> makeReaderForBlob(value, information))
                .flatMap(this::runReaderAsync);
    }

    public ParallelBinaryParser(InputStream input, int noThreads) {
        reader = new BlobReader(input);
        executor = Executors.newFixedThreadPool(noThreads);
        tasksLimiter = new Semaphore(noThreads);
    }

    public void setRelationsCallback(Consumer<List<Osmformat.Relation>> parseRelations) {
        this.parseRelations = parseRelations;
    }

    public void setNodesCallback(Consumer<Node> parseNodes) {
        this.parseNodes = parseNodes;
    }

    public void setsetWaysCallback(Consumer<List<Osmformat.Way>> parseWays) {
        this.parseWays = parseWays;
    }

    public void setHeaderCallback(Consumer<Header> parseHeader) {
        this.parseHeader = parseHeader;
    }

    public void setBoundBoxCallback(Consumer<BoundBox> parseBoundBox) {
        this.parseBoundBox = parseBoundBox;
    }

    public void setCompleteCallback(Runnable complete) {
        this.complete = complete;
    }

    public void parse() {
        Optional<? extends Future<?>> blob;
        do {
            blob = reader.readBlobHeaderLength().flatMap(reader::readBlobHeader).flatMap(this::processDataBlob);
        } while (blob.isPresent());
        executor.shutdown();
        if (complete != null) {
            complete.run();
        }
    }
}
