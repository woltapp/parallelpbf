package com.wolt.osm.parallelpbf.io;

import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.encoder.DenseNodesEncoder;
import com.wolt.osm.parallelpbf.encoder.OsmEntityEncoder;
import com.wolt.osm.parallelpbf.encoder.RelationEncoder;
import com.wolt.osm.parallelpbf.encoder.WayEncoder;
import com.wolt.osm.parallelpbf.entity.Node;
import com.wolt.osm.parallelpbf.entity.OsmEntity;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.Way;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

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
     * Writes contents of dense nodes encoder to the writer
     * and resets encoder.
     * @param <T> Type of encoder to flush.
     * @param encoder OsmEntityEncoder to flush.
     * @param encoderReset callback to the encoder reset procedure.
     */
    private <T extends OsmEntity> void flush(final OsmEntityEncoder<T> encoder, final Runnable encoderReset) {
        byte[] blob = encoder.write();
        writer.writeData(blob);
        encoderReset.run();
    }

    /**
     * Writes entity to the encoder and flushes to the
     * writer in case of hitting size limit.
     * @param <T> Type of entity to write.
     * @param entity entity do write.
     * @param encoder encoder to use.
     * @param encoderReset callback to the encoder reset procedure.
     */
    private <T extends OsmEntity> void write(
            final T entity,
            final OsmEntityEncoder<T> encoder,
            final Runnable encoderReset) {
        encoder.add(entity);
        if (encoder.estimateSize() > LIMIT_BLOB_SIZE) {
            flush(encoder, encoderReset);
        }
    }

    /**
     * NodesEncoder reset/create function.
     */
    private void nodesReset() {
        this.nodesEncoder = new DenseNodesEncoder();
    }

    /**
     * WaysEncoder reset/create function.
     */
    private void wayReset() {
        this.wayEncoder = new WayEncoder();
    }

    /**
     * WaysEncoder reset/create function.
     */
    private void relationReset() {
        this.relationEncoder = new RelationEncoder();
    }

    /**
     * OSMWriter constructor.
     * @param output Shared BlobWriter
     * @param queue input queue with entities.
     */
    public OSMWriter(final BlobWriter output, final LinkedBlockingQueue<OsmEntity> queue) {
        this.writer = output;
        this.writeQueue = queue;
        nodesReset();
        wayReset();
        relationReset();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("OSMWriter");
        while (true) {
            try {
                OsmEntity entity = writeQueue.take();
                if (entity instanceof Node) {
                    write((Node) entity, nodesEncoder, this::nodesReset);
                } else if (entity instanceof Way) {
                    write((Way) entity, wayEncoder, this::wayReset);
                } else if (entity instanceof Relation) {
                    write((Relation) entity, relationEncoder, this::relationReset);
                } else {
                    log.error("Unknown entity type: {}", entity);
                }

            } catch (InterruptedException e) {
                if (nodesEncoder.estimateSize() > 0) {
                    flush(nodesEncoder, this::nodesReset);
                }
                if (wayEncoder.estimateSize() > 0) {
                    flush(wayEncoder, this::wayReset);
                }
                if (relationEncoder.estimateSize() > 0) {
                    flush(relationEncoder, this::relationReset);
                }
                log.debug("OSMWriter requested to stop");
                return;
            }
        }
    }
}
