package com.wolt.osm.parallelpbf.io;

import com.wolt.osm.parallelpbf.blob.BlobWriter;
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
     * (Shared) BlobWriter for this OSMWriter.
     * BlobWriter.write() call expected to be thread-safe.
     */
    private final BlobWriter writer;

    /**
     * Writer frontend-to-writing-threads interface.
     */
    private final LinkedBlockingQueue<OsmEntity> writeQueue;

    /**
     * OSMWriter constructor.
     * @param output Shared BlobWriter
     * @param queue input queue with entities.
     */
    public OSMWriter(final BlobWriter output, final LinkedBlockingQueue<OsmEntity> queue) {
        this.writer = output;
        this.writeQueue = queue;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("OSMWriter");
        while (true) {
            try {
                OsmEntity entity = writeQueue.take();
                if (entity instanceof Node) {
                    Node node = (Node) entity;
                    //write(node);
                } else if (entity instanceof Way) {
                    Way way = (Way) entity;
                    //write(way);
                } else if (entity instanceof Relation) {
                    Relation relation = (Relation) entity;
                    //write(relation);
                } else {
                    log.error("Unknown entity type: {}", entity);
                }

            } catch (InterruptedException e) {
                log.debug("OSMWriter requested to stop");
                return;
            }
        }
    }
}
