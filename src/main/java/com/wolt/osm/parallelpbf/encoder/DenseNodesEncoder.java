package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import java.util.concurrent.TimeUnit;

/**
 * Encodes for DenseNodes structure. Keeps data for the next blob
 * production in RAM and form byte[] blob in request.
 * <p>
 * Encoder is stateful and can't be used after 'write' call is issued.
 * Encoder is not thread-safe.
 */
public final class DenseNodesEncoder extends OsmEntityEncoder<Node> {
    /**
     * Single mode uses 3 long values: id, lat, lon.
     * So single node will use 24 bytes.
     */
    private static final int NODE_ENTRY_SIZE = 24;

    /**
     * Block-wide string table encoder.
     */
    private final StringTableEncoder stringEncoder;

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
     * Current value of UserStringId for delta coding.
     */
    private int infoUserSid = 0;

    /**
     * Current value of Changeset for delta coding.
     */
    private long infoChangeset = 0L;

    /**
     * Current value of Uid for delta coding.
     */
    private int  infoUid = 0;

    /**
     * Current value of Timestamp for delta coding.
     */
    private long infoTimestamp = 0L;

    /**
     * DensNodes blob.
     */
    private final Osmformat.DenseNodes.Builder nodes = Osmformat.DenseNodes.newBuilder();

    /**
     * Default constructor.
     * @param stringTableEncoder Block-wide sting encoder.
     */
    public DenseNodesEncoder(final StringTableEncoder stringTableEncoder) {
        super();
        this.stringEncoder = stringTableEncoder;
    }

    /**
     * Adds a node to the encoder.
     * @param node Node to add.
     * @throws IllegalStateException when call after write() call.
     */
    @Override
    protected void addImpl(final Node node) {
        node.getTags().forEach((k, v) -> {
            nodes.addKeysVals(stringEncoder.getStringIndex(k));
            nodes.addKeysVals(stringEncoder.getStringIndex(v));
        });
        nodes.addKeysVals(0); //Index zero means 'end of tags for node'

        nodes.addId(node.getId() - id);
        id = node.getId();

        if (node.getInfo() != null) {
            int newUserSid = stringEncoder.getStringIndex(node.getInfo().username());
            nodes.getDenseinfoBuilder().addVersion(node.getInfo().version())
                .addChangeset(node.getInfo().changeset() - infoChangeset).addUid(node.getInfo().uid() - infoUid)
                .addUserSid(newUserSid - infoUserSid)
                .addTimestamp(TimeUnit.MICROSECONDS.toMillis(node.getInfo().timestamp() - infoTimestamp))
                .addVisible(node.getInfo().visible());
            infoChangeset = node.getInfo().changeset();
            infoUid = node.getInfo().uid();
            infoTimestamp = node.getInfo().timestamp();
            infoUserSid = newUserSid;
        }

        long latMillis = doubleToNanoScaled(node.getLat() / GRANULARITY);
        long lonMillis = doubleToNanoScaled(node.getLon() / GRANULARITY);

        nodes.addLat(latMillis - lat);
        nodes.addLon(lonMillis - lon);
        lat = latMillis;
        lon = lonMillis;
    }

    /**
     * Provides approximate size of the future blob.
     * Size is calculated as 24 bytes per each node plus 4 bytes per each tag, including closing tags.
     * As protobuf will compact the values in arrays, actual size expected to be smaller.
     * @return Estimated approximate maximum size of a blob.
     */
    @Override
    public int estimateSize() {
        return nodes.getIdCount() * NODE_ENTRY_SIZE + nodes.getKeysValsCount() * TAG_ENTRY_SIZE;
    }

    @Override
    protected Osmformat.PrimitiveGroup.Builder writeImpl() {
        if (nodes.getKeysValsList().stream().noneMatch(i -> i != 0)) {
            // Exceptional case - all nodes in the block are tagless, meaning
            // that tags array must be empty
            nodes.clearKeysVals();
        }
        return Osmformat.PrimitiveGroup.newBuilder().setDense(nodes);
    }
}
