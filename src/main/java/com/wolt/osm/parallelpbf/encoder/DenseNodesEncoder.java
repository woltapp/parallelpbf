package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import com.wolt.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;

import java.util.HashMap;
import java.util.Map;

public class DenseNodesEncoder {
    Integer stringIndex = 0;
    Integer stringTableSize = 0;
    private Map<String, Integer> indexMap = new HashMap<>();
    long id = 0;
    long lat = 0;
    long lon = 0;
    private Osmformat.StringTable.Builder strings = Osmformat.StringTable.newBuilder();
    private Osmformat.DenseNodes.Builder nodes = Osmformat.DenseNodes.newBuilder();
    private Osmformat.PrimitiveBlock.Builder block = Osmformat.PrimitiveBlock.newBuilder();

    /**
     * Conversion from nano- to non-scaled.
     */
    private static final double NANO = 1e9;

    /**
     * Convert double to nano-scaled long.
     * @param value double to convert.
     * @return value multiplied to 1e9 and rounded then.
     */
    private static long doubleToNanoScaled(final double value) {
        return Math.round(value * NANO);
    }

    private int getStringIndex(String s) {
        return indexMap.computeIfAbsent(s, (str) -> {
            stringTableSize = stringTableSize + str.length();
            strings.addS(ByteString.copyFromUtf8(str));
            return ++stringIndex;
        });
    }

    public DenseNodesEncoder() {
        strings.addS(ByteString.EMPTY); //First entry with index 0 is always empty.

        //Dense nodes default values.
        block.setGranularity(100)
                .setLatOffset(0)
                .setLonOffset(0);
    }

    public void addNode(Node node) {
        node.getTags().forEach((k, v) -> {
            nodes.addKeysVals(getStringIndex(k));
            nodes.addKeysVals(getStringIndex(v));
        });
        nodes.addKeysVals(0); //Index zero means 'end of tags for node'

        id = node.getId() - id;
        nodes.addId(id);

        long lat_millis = doubleToNanoScaled(node.getLat()/100);
        long lon_millis = doubleToNanoScaled(node.getLon()/100);

        lat = lat_millis - lat;
        lon = lon_millis - lon;
        nodes.addLat(lat);
        nodes.addLon(lon);
    }

    public int estimateSize() {
        return stringTableSize + nodes.getIdCount() * 24;
    }

    public byte[] write() {
        Osmformat.PrimitiveGroup.Builder nodesGroup = Osmformat.PrimitiveGroup.newBuilder().setDense(nodes);
        return block.setStringtable(strings).addPrimitivegroup(nodesGroup).build().toByteArray();
    }
}
