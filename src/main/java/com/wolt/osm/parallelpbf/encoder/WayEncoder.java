package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;

/**
 * Encodes for Way structure. Keeps data for the next blob
 * production in RAM and form byte[] blob in request.
 * <p>
 * Encoder is stateful and can't be used after 'write' call is issued.
 * Encoder is not thread-safe.
 */
public final class WayEncoder extends OsmEntityEncoder<Way> {
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
    private final Osmformat.PrimitiveGroup.Builder ways = Osmformat.PrimitiveGroup.newBuilder();

    /**
     * Block-wide string table encoder.
     */
    private final StringTableEncoder stringEncoder;

    /**
     * Default constructor.
     * @param stringTableEncoder Block-wide string encoder.
     */
    public WayEncoder(final StringTableEncoder stringTableEncoder) {
        super();
        this.stringEncoder = stringTableEncoder;
    }

    /**
     * Add wy to the encoder.
     * @param w Way to add.
     * @throws IllegalStateException when call after write() call.
     */
    @Override
    protected void addImpl(final Way w) {
        Osmformat.Way.Builder way = Osmformat.Way.newBuilder();
        way.setId(w.getId());
        w.getTags().forEach((k, v) -> {
            way.addKeys(stringEncoder.getStringIndex(k));
            way.addVals(stringEncoder.getStringIndex(v));
        });

        tagsLength = tagsLength + w.getTags().size() * MEMBER_ENTRY_SIZE;

        Osmformat.Info info =
                w.getInfo() != null ? Osmformat.Info.getDefaultInstance().toBuilder()
                .setChangeset(w.getInfo().changeset())
                .setTimestamp(w.getInfo().timestamp())
                .setUid(w.getInfo().uid())
                .setUserSid(stringEncoder.getStringIndex(w.getInfo().username()))
                .setVersion(w.getInfo().version())
                .setVisible(w.getInfo().visible())
                .build() : Osmformat.Info.getDefaultInstance();
        way.setInfo(info);

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
     * Size is calculated as 8 bytes per each way plus 8 bytes per each tag plus 4 bytes each member..
     * As protobuf will compact the values in arrays, actual size expected to be smaller.
     * @return Estimated approximate maximum size of a blob.
     */
    @Override
    public int estimateSize() {
        return membersLength + tagsLength + ways.getWaysCount() * MEMBER_ENTRY_SIZE;
    }

    @Override
    protected Osmformat.PrimitiveGroup.Builder writeImpl() {
        return ways;
    }
}
