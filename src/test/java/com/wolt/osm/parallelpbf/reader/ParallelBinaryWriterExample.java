package com.wolt.osm.parallelpbf.reader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.wolt.osm.parallelpbf.ParallelBinaryParser;
import com.wolt.osm.parallelpbf.ParallelBinaryWriter;
import com.wolt.osm.parallelpbf.entity.*;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ParallelBinaryWriterExample {
    private ParallelBinaryWriter writer;

    private void processNodes(Node node) {
        //nodesCounter.incrementAndGet();
    }

    private void processWays(Way way) {
        //waysCounter.incrementAndGet();
    }

    private void processRelations(Relation way) {
        //relationsCounter.incrementAndGet();
    }

    private void processChangesets(Long id) {
        //changesetsCounter.incrementAndGet();
    }

    @SneakyThrows
    private void printOnCompletions() {
        writer.close();
    }

    private void execute() throws IOException {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        String outputFilename = System.getProperty("java.io.tmpdir")+"/parallel.pbf";
        File outputFile = new File(outputFilename);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();
        OutputStream output = new FileOutputStream(outputFile);

        writer = new ParallelBinaryWriter(output,1);

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.pbf");
        new ParallelBinaryParser(input, 1)
                .onComplete(this::printOnCompletions)
                .onNode(this::processNodes)
                .onWay(this::processWays)
                .onRelation(this::processRelations)
                .onChangeset(this::processChangesets)
                .parse();
        output.close();
    }

    public static void main(String[] args) throws IOException {
        new ParallelBinaryWriterExample().execute();
    }}
