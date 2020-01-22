package com.wolt.osm.parallelpbf;

import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Parallel OSM PBF format writer.
 * <p>
 * See https://github.com/woltapp/parallelpbf for the details and usage example.
 */
@Slf4j
public class ParallelBinaryWriter implements Closeable {
    /**
     * Number of threads to use.
     */
    private final int threads;

    /**
     * Output writer.
     */
    private final BlobWriter writer;

    /**
     * Writer frontend-to-writing-threads interface.
     */
    private final LinkedBlockingQueue<OsmEntity> writeQueue;

    /**
     * Sets OSM PBF file to write and number of threads to use.
     *
     * @param outputStream Any OutputStream pointing to the file to write OSM PBF data.
     * @param noThreads    Number of threads to use. The best results can be achieved when this value
     *                     is set to number of available CPU cores or twice the number of available CPU cores.
     *                     Each thread will use up to 192MB of ram to keep blob data and actually may grow up to
     *                     several hundreds of megabytes.
     * @param boundBox     Output file bbox.
     */
    public ParallelBinaryWriter(final OutputStream outputStream, final int noThreads, final BoundBox boundBox) {
        this.threads = noThreads;
        this.writer = new BlobWriter(outputStream);
        writeQueue = new LinkedBlockingQueue<>(noThreads);
    }

    /**
     * Sets OSM PBF file to write and number of threads to use. This version will not
     * write bounding box to the header.
     *
     * @param outputStream Any OutputStream pointing to the file to write OSM PBF data.
     * @param noThreads    Number of threads to use. The best results can be achieved when this value
     *                     is set to number of available CPU cores or twice the number of available CPU cores.
     *                     Each thread will use up to 192MB of ram to keep blob data and actually may grow up to
     *                     several hundreds of megabytes.
     */
    public ParallelBinaryWriter(final OutputStream outputStream, final int noThreads) {
        this.threads = noThreads;
        this.writer = new BlobWriter(outputStream);
        writeQueue = new LinkedBlockingQueue<>(noThreads);
    }

    /**
     * Write entity to the OSM PBF file. Thread-safe.
     *
     * @param entity Node/Way/Relation entity, other entity types are ignored.
     * @return true if Object queued for writing, false in case of error
     */
    public boolean write(final OsmEntity entity) {
        try {
            writeQueue.put(entity);
        } catch (InterruptedException e) {
            log.warn("Unable to send entity for writing: {}", e.getMessage(), e);
            return false;
        }
        return true;
        /*if (entity instanceof Node) {
            Node node = (Node) entity;
            write(node);
        } else if (entity instanceof Way) {
            Way way = (Way) entity;
            write(way);
        } else if (entity instanceof Relation) {
            Relation relation = (Relation) entity;
            write(relation);
        } else {
            log.error("Unknown entity type: {}", entity);
        }*/
    }

    /**
     * Finishes OSM PBF file. **Must** be called or file may be left unfinished.
     * @throws IOException When something goes wrong.
     */
    @Override
    public void close() throws IOException {

    }
}
