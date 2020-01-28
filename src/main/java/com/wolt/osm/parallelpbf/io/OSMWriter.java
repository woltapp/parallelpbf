package com.wolt.osm.parallelpbf.io;

import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.encoder.DenseNodesEncoder;
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
    private DenseNodesEncoder nodesEncoder;

    /**
     * Current(!) ways block encoder.
     */
    private WayEncoder wayEncoder;

    /**
     * Writes contents of dense nodes encoder to the writer
     * and resets encoder.
     */
    private void flushNodes() {
        byte[] blob = nodesEncoder.write();
        writer.writeData(blob);
        nodesEncoder = new DenseNodesEncoder();
    }

    /**
     * Writes node to the encoder and flushes to the
     * writer in case of hitting size limit.
     * @param node Node do write.
     */
    private void write(final Node node) {
        nodesEncoder.addNode(node);
        if (nodesEncoder.estimateSize() > LIMIT_BLOB_SIZE) {
            flushNodes();
        }
    }

    /**
     * Writes contents of way encoder to the writer
     * and resets encoder.
     */
    private void flushWay() {
        byte[] blob = wayEncoder.write();
        writer.writeData(blob);
        wayEncoder = new WayEncoder();
    }

    /**
     * Writes ways to the encoder and flushes to the
     * writer in case of hitting size limit.
     * @param way Way to write.
     */
    private void write(final Way way) {
        wayEncoder.add(way);
        if (wayEncoder.estimateSize() > LIMIT_BLOB_SIZE) {
            flushWay();
        }
    }

    /**
     * OSMWriter constructor.
     * @param output Shared BlobWriter
     * @param queue input queue with entities.
     */
    public OSMWriter(final BlobWriter output, final LinkedBlockingQueue<OsmEntity> queue) {
        this.writer = output;
        this.writeQueue = queue;
        nodesEncoder = new DenseNodesEncoder();
        wayEncoder = new WayEncoder();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("OSMWriter");
        while (true) {
            try {
                OsmEntity entity = writeQueue.take();
                if (entity instanceof Node) {
                    write((Node) entity);
                } else if (entity instanceof Way) {
                    write((Way) entity);
                } else if (entity instanceof Relation) {
                    Relation relation = (Relation) entity;
                    //write(relation);
                } else {
                    log.error("Unknown entity type: {}", entity);
                }

            } catch (InterruptedException e) {
                if (nodesEncoder.estimateSize() > 0) {
                    flushNodes();
                }
                if (wayEncoder.estimateSize() > 0) {
                    flushWay();
                }
                log.debug("OSMWriter requested to stop");
                return;
            }
        }
    }
}
