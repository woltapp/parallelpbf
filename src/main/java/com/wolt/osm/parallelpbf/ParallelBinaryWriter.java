package com.wolt.osm.parallelpbf;

import com.wolt.osm.parallelpbf.blob.BlobWriter;
import com.wolt.osm.parallelpbf.encoder.OsmHeaderEncoder;
import com.wolt.osm.parallelpbf.entity.BoundBox;
import com.wolt.osm.parallelpbf.entity.OsmEntity;
import com.wolt.osm.parallelpbf.io.OSMWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
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
     * List of worker threads.
     */
    private final LinkedList<Thread> workers = new LinkedList<>();

    /**
     * Header writing procedure.
     * @param boundBox Optional bounding box to include into header.
     * @return false in case of error, true otherwise.
     */
    private boolean writeHeader(final BoundBox boundBox) {
        return writer.writeHeader(OsmHeaderEncoder.encodeHeader(boundBox));
    }

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
        this.writer = new BlobWriter(outputStream);
        this.threads = noThreads;
        writeQueue = new LinkedBlockingQueue<>(noThreads);
        if (!writeHeader(boundBox)) {
            throw new RuntimeException("Error while creating writer and writing header");
        }
    }

    /**
     * Starts reading threads.
     */
    public void start() {
        for (int indx = 0; indx < this.threads; ++indx) {
            Thread worker = new Thread(new OSMWriter(writer, writeQueue));
            worker.start();
            workers.push(worker);
        }
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
    }

    /**
     * Finishes OSM PBF file. **Must** be called or file may be left unfinished.
     */
    @Override
    public void close() {
        workers.forEach((worker) -> {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for OSMWriter to stop");
            }
        });
    }
}
