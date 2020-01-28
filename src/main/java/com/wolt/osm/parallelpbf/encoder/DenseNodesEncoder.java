package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import com.wolt.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;

import java.util.HashMap;
import java.util.Map;

/**
 * Encodes for DenseNodes structure. Keeps data for the next blob
 * production in RAM and form byte[] blob in request.
 *
 * Encoder is stateful and can't be used after 'write' call is issued.
 * Encoder is not thread-safe.
 */
public final class DenseNodesEncoder extends OsmEncoder {
    /**
     * Coordinates grid default granularity.
     */
    private static final int GRANULARITY = 100;

    /**
     * Single mode uses 3 long values: id, lat, lon.
     * So single node will use 24 bytes.
     */
    private static final int NODE_ENTRY_SIZE = 24;

    /**
     * Single tag entry (key or value) is a integer index,
     * so 4 bytes per entry.
     */
    private static final int TAG_ENTRY_SIZE = 4;

    /**
     * Keeps current maximum string index value.
     */
    private Integer stringIndex = 0;

    /**
     * Size of strings kept in the string table.
     */
    private Integer stringTableSize = 0;

    /**
     * Reverse index mapping - for string already stored in the table it will map
     * string values back to their indices.
     */
    private Map<String, Integer> indexMap = new HashMap<>();

    /**
     * The string table.
     */
    private Osmformat.StringTable.Builder strings = Osmformat.StringTable.newBuilder();

    /**
     * Current value of NodeId for delta coding.
     */
    private long id = 0;

    /**
     * Current value of lat millis for delta coding.
     */
    private long lat = 0;

    /**
     * Current value of lon millis for delta coding.
     */
    private long lon = 0;

    /**
     * DensNodes blob.
     */
    private Osmformat.DenseNodes.Builder nodes = Osmformat.DenseNodes.newBuilder();

    /**
     * Adds string to the string table and adds string size to the stringtable size.
     * @param str String to add.
     * @return String index in table.
     */
    private int addStringToTable(final String str) {
        stringTableSize = stringTableSize + str.length();
        strings.addS(ByteString.copyFromUtf8(str));
        return ++stringIndex;
    }

    /**
     * Finds stringtable index for a supplied string. Will return either existing index for a string
     * or add string to the stringtable and emit a new index.
     * @param s String to index.
     * @return Strings index in the stringtable.
     */
    private int getStringIndex(final String s) {
        return indexMap.computeIfAbsent(s, this::addStringToTable);
    }

    /**
     * Default constructor.
     */
    public DenseNodesEncoder() {
        strings.addS(ByteString.EMPTY); //First entry with index 0 is always empty.
    }

    /**
     * Adds a node to the encoder.
     * @param node Node to add.
     */
    public void addNode(final Node node) {
        node.getTags().forEach((k, v) -> {
            nodes.addKeysVals(getStringIndex(k));
            nodes.addKeysVals(getStringIndex(v));
        });
        nodes.addKeysVals(0); //Index zero means 'end of tags for node'

        id = node.getId() - id;
        nodes.addId(id);

        long latMillis = doubleToNanoScaled(node.getLat() / GRANULARITY);
        long lonMillis = doubleToNanoScaled(node.getLon() / GRANULARITY);

        lat = latMillis - lat;
        lon = lonMillis - lon;
        nodes.addLat(lat);
        nodes.addLon(lon);
    }

    /**
     * Provides approximate size of the future blob.
     * Size is calculated as length of all strings in the string tables
     * plus 24 bytes per each node plus 4 bytes per each tag, including closing tags.
     * As protobuf will compact the values in arrays, actual size expected to be smaller.
     * @return Estimated approximate maximum size of a blob.
     */
    public int estimateSize() {
        return stringTableSize + nodes.getIdCount() * NODE_ENTRY_SIZE + nodes.getKeysValsCount() * TAG_ENTRY_SIZE;
    }

    /**
     * Build a blob from the collected data. Encoder will become
     * unusable after that call.
     * @return OSM PBF primitiveBlock blob.
     */
    public byte[] write() {
        Osmformat.PrimitiveGroup.Builder nodesGroup = Osmformat.PrimitiveGroup.newBuilder().setDense(nodes);
        return Osmformat.PrimitiveBlock.newBuilder()
                .setGranularity(GRANULARITY)
                .setLatOffset(0)
                .setLonOffset(0)
                .setStringtable(strings)
                .addPrimitivegroup(nodesGroup)
                .build()
                .toByteArray();
    }
}
