package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicInteger;

public class ParalelBinaryParserExample {

    private final StringBuilder output = new StringBuilder();
    private AtomicInteger nodesCounter = new AtomicInteger();
    private AtomicInteger waysCounter = new AtomicInteger();
    private AtomicInteger relationsCounter = new AtomicInteger();
    private AtomicInteger changesetsCounter = new AtomicInteger();

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

    private void processWays(Way way) {
        waysCounter.incrementAndGet();
    }

    private void processRelations(Relation way) {
        relationsCounter.incrementAndGet();
    }

    private void processChangesets(Long id) {
        changesetsCounter.incrementAndGet();
    }

    private void printOnCompletions() {
        output.append("Node count: ");
        output.append(nodesCounter.get());
        output.append("\n");

        output.append("Way count: ");
        output.append(waysCounter.get());
        output.append("\n");

        output.append("Relations count: ");
        output.append(relationsCounter.get());
        output.append("\n");

        output.append("Changesets count: ");
        output.append(changesetsCounter.get());
        output.append("\n");

        System.out.println("Reading results:");
        System.out.println(output);
    }

    private void execute() throws FileNotFoundException {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        FileInputStream input = new FileInputStream("/home/chollya/Downloads/sample.pbf");
        new ParallelBinaryParser(input, 1)
                .onHeader(this::processHeader)
                .onBoundBox(this::processBoundingBox)
                .onComplete(this::printOnCompletions)
                .onNode(this::processNodes)
                .onWay(this::processWays)
                .onRelation(this::processRelations)
                .onChangeset(this::processChangesets)
                .parse();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new ParalelBinaryParserExample().execute();
    }
}
