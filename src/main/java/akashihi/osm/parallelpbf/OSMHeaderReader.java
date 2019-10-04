package akashihi.osm.parallelpbf;

import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class OSMHeaderReader extends OSMReader {
    private static Logger logger = LoggerFactory.getLogger(OSMHeaderReader.class);

    OSMHeaderReader(byte[] blob, Semaphore tasksLimiter) {
        super(blob, tasksLimiter);
    }

    @Override
    protected void read(byte[] message) {
        Osmformat.HeaderBlock headerData;
        try {
            headerData = Osmformat.HeaderBlock.parseFrom(message);
        } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing OSMHeader block: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        if (headerData.hasBbox()) {
            logger.debug("BBox present");
        }
        logger.debug("Required features: {}", headerData.getRequiredFeaturesList());
        logger.debug("Optional features: {}", headerData.getOptionalFeaturesList());

        if (headerData.hasWritingprogram()) {
            logger.debug("Writing program: {}", headerData.getWritingprogram());
        }
        if (headerData.hasSource()) {
            logger.debug("Source: {}", headerData.getSource());
        }
    }
}
