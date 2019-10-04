package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.BoundBox;
import akashihi.osm.parallelpbf.entity.Header;
import akashihi.osm.parallelpbf.entity.Node;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicInteger;

public class ParalelBinaryParserExample {

    private final StringBuilder output = new StringBuilder();
    private AtomicInteger nodesCounter = new AtomicInteger();

    private void processHeader(Header header) {
        synchronized (output) {
            output.append(header);
            output.append("\n");
        }
    }

    private void processBoundingBox(BoundBox bbox) {
        synchronized (output) {
            output.append(bbox);
            output.append("\n");
        }
    }

    private void processNodes(Node node) {
        nodesCounter.incrementAndGet();
    }

    private void printOnCompletions() {
        output.append("Node count: ");
        output.append(nodesCounter.get());
        output.append("\n");

        System.out.println("Reading results:");
        System.out.println(output);
    }

    private void execute() throws FileNotFoundException {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        FileInputStream input = new FileInputStream("/home/chollya/Downloads/sample.pbf");
        ParallelBinaryParser parser = new ParallelBinaryParser(input, 1);

        parser.setHeaderCallback(this::processHeader);
        parser.setBoundBoxCallback(this::processBoundingBox);
        parser.setCompleteCallback(this::printOnCompletions);
        parser.setNodesCallback(this::processNodes);

        parser.parse();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new ParalelBinaryParserExample().execute();
    }
}
