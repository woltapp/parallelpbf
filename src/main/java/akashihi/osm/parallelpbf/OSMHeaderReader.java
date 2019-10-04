package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.BoundBox;
import akashihi.osm.parallelpbf.entity.Header;
import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public final class OSMHeaderReader extends OSMReader {
    private static Logger logger = LoggerFactory.getLogger(OSMHeaderReader.class);

    /**
     * Header processing callback. Must be reentrant.
     */
    private final Consumer<Header> parseHeader;

    /**
     * Header processing callback. Must be reentrant.
     */
    private final Consumer<BoundBox> parseBoundBox;

    OSMHeaderReader(byte[] blob, Semaphore tasksLimiter, Consumer<Header> parseHeader, Consumer<BoundBox> parseBoundBox) {
        super(blob, tasksLimiter);
        this.parseHeader = parseHeader;
        this.parseBoundBox = parseBoundBox;
    }

    private boolean checkRequiredFeatures(List<String> features) {
        Optional<String> unsupported = features.stream()
                .filter(f -> !f.equalsIgnoreCase("OsmSchema-V0.6"))
                .filter(f -> !f.equalsIgnoreCase("DenseNodes"))
                .filter(f -> !f.equalsIgnoreCase("HistoricalInformation"))
                .findAny();
        unsupported.ifPresent(s -> logger.error("Unsupported required feature found: {}", s));
        return !unsupported.isPresent();
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

        if (!checkRequiredFeatures(headerData.getRequiredFeaturesList())) {
            throw new RuntimeException("Can't proceed with unsupported features");
        }

        Header header = new Header(headerData.getRequiredFeaturesList(), headerData.getOptionalFeaturesList());
        if (headerData.hasWritingprogram()) {
            header.setWritingProgram(headerData.getWritingprogram());
        }
        if (headerData.hasSource()) {
            header.setSource(headerData.getSource());
        }
        logger.debug("Header: {}", header.toString());
        if (parseHeader != null) {
            parseHeader.accept(header);
        }

        if (headerData.hasBbox()) {
            BoundBox bbox = new BoundBox(headerData.getBbox().getLeft() / 1e9,
                    headerData.getBbox().getTop() / 1e9,
                    headerData.getBbox().getRight() / 1e9,
                    headerData.getBbox().getBottom() / 1e9);
            logger.debug("Bounding box: {}", bbox.toString());
            if (parseBoundBox != null) {
                parseBoundBox.accept(bbox);
            }
        }
    }
}
