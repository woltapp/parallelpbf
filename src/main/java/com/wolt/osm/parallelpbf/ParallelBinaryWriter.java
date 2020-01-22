package com.wolt.osm.parallelpbf;

import com.wolt.osm.parallelpbf.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

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
     * Output data stream.
     */
    private final OutputStream output;

    /**
     * Sets OSM PBF file to write and number of threads to use.
     *
     * @param outputStream Any OutputStream pointing to the file to write OSM PBF data.
     * @param noThreads    Number of threads to use. The best results can be achieved when this value
     *                     is set to number of available CPU cores or twice the number of available CPU cores.
     *                     Each thread will use up to 192MB of ram to keep blob data and actually may grow up to
     *                     several hundreds of megabytes.
     * @param header       Output file header information.
     * @param boundBox     Output file bbox.
     */
    public ParallelBinaryWriter(final OutputStream outputStream, final int noThreads,
                                final Header header, final BoundBox boundBox) {
        this.threads = noThreads;
        this.output = outputStream;
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
     * @param header       Output file header information.
     */
    public ParallelBinaryWriter(final OutputStream outputStream, final int noThreads, final Header header) {
        this.threads = noThreads;
        this.output = outputStream;
    }

    /**
     * Write entity to the OSM PBF file. Thread-safe.
     *
     * @param entity Node/Way/Relation entity, other entity types are ignored.
     */
    public void write(final OsmEntity entity) {
        if (entity instanceof Node) {
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
        }
    }

    /**
     * Write Node to the OSM PBF file. Thread-safe.
     *
     * @param node Node entity.
     */
    public void write(final Node node) {

    }

    /**
     * Write Way to the OSM PBF file. Thread-safe.
     *
     * @param way Way entity.
     */
    public void write(final Way way) {

    }

    /**
     * Write Relation to the OSM PBF file. Thread-safe.
     *
     * @param relation Relation entity.
     */
    public void write(final Relation relation) {

    }

    /**
     * Write changeset id to the OSM PBF file. Thread-safe.
     * @param changeset Changeset id.
     */
    public void write(final Long changeset) {

    }

    /**
     * Finishes OSM PBF file. **Must** be called or file may be left unfinished.
     * @throws IOException When something goes wrong.
     */
    @Override
    public void close() throws IOException {

    }
}
