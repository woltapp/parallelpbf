package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.BoundBox;
import akashihi.osm.parallelpbf.entity.Header;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ParalelBinaryParserExample {

    private StringBuilder output = new StringBuilder();

    private void processHeader(Header header) {
        synchronized (output) {
            output.append(header);
        }
    }

    private void processBoundingBox(BoundBox bbox) {
        synchronized (output) {
            output.append(bbox);
        }
    }

    private void printOnCompletions() {
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

        parser.parse();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new ParalelBinaryParserExample().execute();
    }
}
