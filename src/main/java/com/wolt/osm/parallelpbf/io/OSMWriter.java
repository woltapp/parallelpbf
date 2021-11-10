package com.wolt.osm.parallelpbf.io;

import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.encoder.DenseNodesEncoder;
import com.wolt.osm.parallelpbf.encoder.OsmEncoder;
import com.wolt.osm.parallelpbf.encoder.OsmEntityEncoder;
import com.wolt.osm.parallelpbf.encoder.RelationEncoder;
import com.wolt.osm.parallelpbf.encoder.StringTableEncoder;
import com.wolt.osm.parallelpbf.encoder.WayEncoder;
import com.wolt.osm.parallelpbf.entity.Node;
import com.wolt.osm.parallelpbf.entity.OsmEntity;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Main handler for the OSM entities. Accepts entities over
 * the writer queue and stores them to the corresponding encoder.
 * On encoder overflow/close request encoders content is sent
 * to the writer.
 */
@Slf4j
public final class OSMWriter implements Runnable {
    /**
     * Blob should not be bigger then 16M, but we limit to
     * 15M for a safety, as we do estimate size approximately.
     */
    private static final int LIMIT_BLOB_SIZE = 15 * 1024 * 1024;

    /**
     * (Shared) BlobWriter for this OSMWriter.
     * BlobWriter.write() call expected to be thread-safe.
     */
    private final BlobWriter writer;

    /**
     * Writer frontend-to-writing-threads interface.
     */
    private final LinkedBlockingQueue<OsmEntity> writeQueue;

    /**
     * Current(!) densenodes block encoder.
     */
    private OsmEntityEncoder<Node> nodesEncoder;

    /**
     * Current(!) ways block encoder.
     */
    private OsmEntityEncoder<Way> wayEncoder;

    /**
     * Current(!) relation block encoder.
     */
    private OsmEntityEncoder<Relation> relationEncoder;

    /**
     * Block-wide stringtable encoder.
     */
    private StringTableEncoder stringEncoder;

    /**
     * Time spent to write Nodes in nanoseconds.
     */
    private Long encodingNodes = 0L;

    /**
     * Time spent to write Ways in nanoseconds.
     */
    private Long encodingWays = 0L;

    /**
     * Time spent to write Relations in nanoseconds.
     */
    private Long encodingRelations = 0L;

    /**
     * Total number of Nodes written.
     */
    private Long totalNodes = 0L;

    /**
     * Total number of Ways written.
     */
    private Long totalWays = 0L;

    /**
     * Total number of Relations written.
     */
    private Long totalRelations = 0L;

    /**
     * Time to write buffered elements to disk in nanoseconds.
     */
    private Long totalFlushTime = 0L;


    /**
     * OSMWriter constructor.
     *
     * @param output Shared BlobWriter
     * @param queue  input queue with entities.
     */
    public OSMWriter(final BlobWriter output, final LinkedBlockingQueue<OsmEntity> queue) {
        this.writer = output;
        this.writeQueue = queue;
        encodersReset();
    }

    /**
     * Writes contents of encoders to the writer
     * and resets encoders.
     *
     * @param nodesSize    Estimated size of nodes group.
     * @param waysSize     Estimated size of ways group.
     * @param relationSize Estimated size of relations group.
     */
    private void flush(final int nodesSize, final int waysSize, final int relationSize) {
        long startTime = System.currentTimeMillis();
        if (nodesSize + waysSize + relationSize > 0) {
            Osmformat.PrimitiveBlock.Builder block = Osmformat.PrimitiveBlock.newBuilder()
                    .setStringtable(stringEncoder.getStrings());
            if (nodesSize > 0) {
                block.setGranularity(OsmEncoder.GRANULARITY)
                        .setLatOffset(0)
                        .setLonOffset(0)
                        .addPrimitivegroup(nodesEncoder.write());
            }
            if (waysSize > 0) {
                block.addPrimitivegroup(wayEncoder.write());
            }
            if (relationSize > 0) {
                block.addPrimitivegroup(relationEncoder.write());
            }
            byte[] blob = block.build().toByteArray();
            writer.writeData(blob);
        }

        encodersReset();
        long endTime = System.currentTimeMillis();

        log.info("Time spent flushing in ms {}", endTime - startTime);
        totalFlushTime += endTime - startTime;
    }

    /**
     * Encoder reset function. Recreates all the encoders in proper order.
     */
    private void encodersReset() {
        this.stringEncoder = new StringTableEncoder();
        this.nodesEncoder = new DenseNodesEncoder(this.stringEncoder);
        this.wayEncoder = new WayEncoder(this.stringEncoder);
        this.relationEncoder = new RelationEncoder(this.stringEncoder);
    }

    @SuppressWarnings("java:S2189")
    @Override
    public void run() {
        Thread.currentThread().setName("OSMWriter");
        while (true) {
            try {
                OsmEntity entity = writeQueue.take();
                process(entity);
            } catch (InterruptedException e) {
                // Thread is getting ready to die, but first,
                // drain remaining elements on the queue and process them.
                final LinkedList<OsmEntity> remainingObjects = new LinkedList<>();
                writeQueue.drainTo(remainingObjects);
                for (OsmEntity entity : remainingObjects) {
                    process(entity);
                }
                flush(nodesEncoder.estimateSize(), wayEncoder.estimateSize(), relationEncoder.estimateSize());
                if (log.isDebugEnabled()) {
                    log.debug("OSMWriter requested to stop");
                    log.debug("Time spend encoding nodes {} seconds.", TimeUnit.NANOSECONDS.toSeconds(encodingNodes));
                    log.debug("Time spend encoding ways {} seconds.", TimeUnit.NANOSECONDS.toSeconds(encodingWays));
                    log.debug("Time spend encoding relations {} seconds.",
                        TimeUnit.NANOSECONDS.toSeconds(encodingRelations));
                    log.debug("Total time spent in flush calls in ms {}.", totalFlushTime);
                    log.debug("Nodes processed {}, ways processed {}, relations processed {}", totalNodes, totalWays,
                        totalRelations);
                }
                return;
            }
        }
    }

    /**
     * Converts given {@link OsmEntity} to protobuf element and flushes queue to disk if necessary.
     * @param entity a Node, Way or Relation to be persisted.
     */
    private void process(final OsmEntity entity) {
        if (entity instanceof Node) {
            long startTime = System.nanoTime();
            nodesEncoder.add((Node) entity);
            long endTime = System.nanoTime();
            encodingNodes += endTime - startTime;
            totalNodes += 1;
        } else if (entity instanceof Way) {
            long startTime = System.nanoTime();
            wayEncoder.add((Way) entity);
            long endTime = System.nanoTime();
            encodingWays += endTime - startTime;
            totalWays += 1;
        } else if (entity instanceof Relation) {
            long startTime = System.nanoTime();
            relationEncoder.add((Relation) entity);
            long endTime = System.nanoTime();
            encodingRelations += endTime - startTime;
            totalRelations += 1;
        } else {
            log.error("Unknown entity type: {}", entity);
        }

        int nodesSize = nodesEncoder.estimateSize();
        int waysSize = wayEncoder.estimateSize();
        int relationSize = relationEncoder.estimateSize();

        int blobSize = nodesSize + waysSize + relationSize + stringEncoder.getStringSize();
        if (blobSize > LIMIT_BLOB_SIZE) {
            flush(nodesSize, waysSize, relationSize);
        }
    }
}
