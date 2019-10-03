package akashihi.osm.parallelpbf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ParalelBinaryParserExample {
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream input = new FileInputStream("/home/chollya/Downloads/sample.pbf");
        ParallelBinaryParser parser = new ParallelBinaryParser(input, 1);
        parser.parse();
    }
}
