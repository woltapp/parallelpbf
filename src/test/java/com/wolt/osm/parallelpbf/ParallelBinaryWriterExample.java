package com.wolt.osm.parallelpbf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.wolt.osm.parallelpbf.entity.*;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ParallelBinaryWriterExample {
    private ParallelBinaryWriter writer;

    private void processNodes(Node node) {
        writer.write(node);
    }

    private void processWays(Way way) {
        writer.write(way);
    }

    private void processRelations(Relation relation) {
        writer.write(relation);
    }

    @SneakyThrows
    private void closeOnComplete() {
        writer.close();
    }

    private void execute() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        String outputFilename = System.getProperty("java.io.tmpdir")+"/parallel.pbf";
        File outputFile = new File(outputFilename);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();
        OutputStream output = new FileOutputStream(outputFile);

        writer = new ParallelBinaryWriter(output,1, null);
        writer.start();

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf");
        new ParallelBinaryParser(input, 1)
                .onComplete(this::closeOnComplete)
                .onNode(this::processNodes)
                .onWay(this::processWays)
                .onRelation(this::processRelations)
                .parse();
        output.close();
    }

    public static void main(String[] args) throws Exception {
        new ParallelBinaryWriterExample().execute();
    }}
