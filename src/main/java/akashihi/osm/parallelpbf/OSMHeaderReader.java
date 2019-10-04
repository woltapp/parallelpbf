package akashihi.osm.parallelpbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class OSMHeaderReader implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(OSMHeaderReader.class);

    private final byte[] blob;
    private final Semaphore tasksLimiter;

    public OSMHeaderReader(byte[] blob, Semaphore tasksLimiter) {
        this.blob = blob;
        this.tasksLimiter = tasksLimiter;
    }

    @Override
    public void run() {
        logger.trace("Parsing OSM header");
        tasksLimiter.release();
    }
}
