package akashihi.osm.parallelpbf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ParalelBinaryParserExample {
    public static void main(String[] args) throws FileNotFoundException {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        FileInputStream input = new FileInputStream("/home/chollya/Downloads/sample.pbf");
        ParallelBinaryParser parser = new ParallelBinaryParser(input, 1);
        parser.parse();
    }
}
