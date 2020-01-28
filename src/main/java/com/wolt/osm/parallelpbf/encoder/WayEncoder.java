package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;

/**
 * Encodes for Way structure. Keeps data for the next blob
 * production in RAM and form byte[] blob in request.
 *
 * Encoder is stateful and can't be used after 'write' call is issued.
 * Encoder is not thread-safe.
 */
public final class WayEncoder extends OsmEntityEncoder {
    /**
     * Single member entry (key or value) is a long value,
     * so 8 bytes per entry.
     */
    private static final int MEMBER_ENTRY_SIZE = 8;

    /**
     * Length of all members arrays, calculated as sum of all members entries of each
     * way.
     */
    private int membersLength = 0;

    /**
     * Length of all tags (keys/vals) arrays, calculated as sum of all tags entries of each
     * way.
     */
    private int tagsLength = 0;

    /**
     * Ways builder.
     */
    private Osmformat.PrimitiveGroup.Builder ways = Osmformat.PrimitiveGroup.newBuilder();

    /**
     * 'Write was called' flag.
     */
    private boolean built = false;

    /**
     * Default constructor.
     */
    public WayEncoder() {
        super();
    }

    /**
     * Add wy to the encoder.
     * @param w Way to add.
     */
    public void add(final Way w) {
        if (built) {
            throw new IllegalStateException("Encoder content is already written");
        }
        Osmformat.Way.Builder way = Osmformat.Way.newBuilder();

        way.setId(w.getId());

        w.getTags().forEach((k, v) -> {
            way.addKeys(this.getStringIndex(k));
            way.addVals(this.getStringIndex(v));
        });
        tagsLength = tagsLength + w.getTags().size() * MEMBER_ENTRY_SIZE;

        long member = 0;
        for (long node : w.getNodes()) {
            way.addRefs(node - member);
            member = node;
        }
        membersLength = membersLength + w.getNodes().size() * MEMBER_ENTRY_SIZE;

        ways.addWays(way);
    }

    /**
     * Provides approximate size of the future blob.
     * Size is calculated as length of all strings in the string tables
     * plus 8 bytes per each way plus 8 bytes per each tag plus 4 bytes each member..
     * As protobuf will compact the values in arrays, actual size expected to be smaller.
     * @return Estimated approximate maximum size of a blob.
     */
    @Override
    public int estimateSize() {
        return this.getStringSize() + membersLength + tagsLength + ways.getWaysCount() * MEMBER_ENTRY_SIZE;
    }

    @Override
    public byte[] write() {
        built = true;
        return Osmformat.PrimitiveBlock.newBuilder()
                .setStringtable(this.getStrings())
                .addPrimitivegroup(ways)
                .build()
                .toByteArray();
    }
}
