package akashihi.osm.parallelpbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class OSMDataReader extends OSMReader {
    private static Logger logger = LoggerFactory.getLogger(OSMDataReader.class);

    OSMDataReader(byte[] blob, Semaphore tasksLimiter) {
        super(blob, tasksLimiter);
    }

    @Override
    protected void read(byte[] message) {
        logger.trace("Parsing OSM data");
    }
}
