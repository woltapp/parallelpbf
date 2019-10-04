package akashihi.osm.parallelpbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class OSMHeaderReader extends OSMReader {
    private static Logger logger = LoggerFactory.getLogger(OSMHeaderReader.class);

    OSMHeaderReader(byte[] blob, Semaphore tasksLimiter) {
        super(blob, tasksLimiter);
    }

    @Override
    protected void read() {
        logger.trace("Parsing OSM header");
    }
}
