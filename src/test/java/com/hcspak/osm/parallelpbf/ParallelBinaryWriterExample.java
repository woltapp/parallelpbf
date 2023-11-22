package com.hcspak.osm.parallelpbf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.hcspak.osm.parallelpbf.entity.Node;
import com.hcspak.osm.parallelpbf.entity.Relation;
import com.hcspak.osm.parallelpbf.entity.Way;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

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

  private void execute() throws IOException {
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.TRACE);

    String outputFilename = System.getProperty("java.io.tmpdir") + "/parallel.pbf";
    File outputFile = new File(outputFilename);
    Files.deleteIfExists(outputFile.toPath());
    Files.createFile(outputFile.toPath());
    OutputStream output = new FileOutputStream(outputFile);

    writer = new ParallelBinaryWriter(output, 1, null);
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

  public static void main(String[] args) throws IOException {
    new ParallelBinaryWriterExample().execute();
  }
}
