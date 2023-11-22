package com.hcspak.osm.parallelpbf.blob;

import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Fileformat;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlobWriterTest {
  @Test
  void testWriter() throws InvalidProtocolBufferException, DataFormatException {
    String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt";
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    BlobWriter testedObject = new BlobWriter(output);
    assertTrue(testedObject.writeData(expected.getBytes()));

    byte[] data = output.toByteArray();

    ByteBuffer size = ByteBuffer.wrap(data);
    assertEquals(11, size.getInt());


    byte[] headerBinary = Arrays.copyOfRange(data, 4, 4 + 11);
    Fileformat.BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(headerBinary);
    assertEquals(BlobInformation.TYPE_OSM_DATA, blobHeader.getType());
    assertEquals(81, blobHeader.getDatasize());

    byte[] blobBinary = Arrays.copyOfRange(data, 15, 15 + 81);
    Fileformat.Blob blob = Fileformat.Blob.parseFrom(blobBinary);
    assertEquals(89, blob.getRawSize());

    byte[] content = blob.getZlibData().toByteArray();
    byte[] uncompressedString = new byte[89];
    Inflater inflater = new Inflater();
    inflater.setInput(content);
    inflater.finished();
    inflater.inflate(uncompressedString);

    assertEquals(expected, new String(uncompressedString));
  }

  @Test
  void testWriterHeader() throws InvalidProtocolBufferException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    BlobWriter testedObject = new BlobWriter(output);
    assertTrue(testedObject.writeHeader(new byte[1]));

    byte[] data = output.toByteArray();

    ByteBuffer size = ByteBuffer.wrap(data);
    assertEquals(13, size.getInt());


    byte[] headerBinary = Arrays.copyOfRange(data, 4, 4 + 13);
    Fileformat.BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(headerBinary);
    assertEquals(BlobInformation.TYPE_OSM_HEADER, blobHeader.getType());
  }
}