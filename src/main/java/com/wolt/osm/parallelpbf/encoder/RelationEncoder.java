package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.Info;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.RelationMember;
import crosby.binary.Osmformat;

/**
 * Encodes for Relation structure. Keeps data for the next blob
 * production in RAM and form byte[] blob in request.
 *
 * Encoder is stateful and can't be used after 'write' call is issued.
 * Encoder is not thread-safe.
 */
public final class RelationEncoder extends OsmEntityEncoder<Relation> {
    /**
     * Single relation entry is built of role ide of int,
     * member id of long and type with one byte size.
     */
    private static final int RELATION_ENTRY_SIZE = 13;

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
    private Osmformat.PrimitiveGroup.Builder relations = Osmformat.PrimitiveGroup.newBuilder();

    /**
     * Block-wide string table encoder.
     */
    private final StringTableEncoder stringEncoder;

    /**
     * Constructor.
     * @param stringTableEncoder Block-wide string encoder.
     */
    public RelationEncoder(final StringTableEncoder stringTableEncoder) {
        this.stringEncoder = stringTableEncoder;
    }

    @Override
    protected void addImpl(final Relation r) {
        Osmformat.Relation.Builder relation = Osmformat.Relation.newBuilder();

        relation.setId(r.getId());

        r.getTags().forEach((k, v) -> {
            relation.addKeys(stringEncoder.getStringIndex(k));
            relation.addVals(stringEncoder.getStringIndex(v));
        });
        tagsLength = tagsLength + r.getTags().size() * MEMBER_ENTRY_SIZE;

        Osmformat.Info info = r.getInfo() != null ? Osmformat.Info.newBuilder()
                .setUserSid(stringEncoder.getStringIndex(r.getInfo().getUsername()))
                .setVisible(r.getInfo().isVisible())
                .setUid(r.getInfo().getUid())
                .setVersion(r.getInfo().getVersion())
                .setTimestamp(r.getInfo().getTimestamp())
                .setChangeset(r.getInfo().getChangeset())
                .build() : Osmformat.Info.getDefaultInstance();
        relation.setInfo(info);

//        if(r.getInfo() != null) {
//            r.setInfo(new Info(r.getInfo().getUid(),
//                r.getInfo().getUsername(),
//                r.getInfo().getVersion(),
//                r.getInfo().getTimestamp(),
//                r.getInfo().getChangeset(),
//                r.getInfo().isVisible()));
//        }

        long member = 0;
        for (RelationMember rm : r.getMembers()) {
            relation.addRolesSid(stringEncoder.getStringIndex(rm.getRole()));
            relation.addMemids(rm.getId() - member);
            member = rm.getId();
            relation.addTypes(Osmformat.Relation.MemberType.valueOf(rm.getType().ordinal()));
        }
        membersLength = membersLength + r.getMembers().size() * RELATION_ENTRY_SIZE;

        relations.addRelations(relation);
    }

    @Override
    public int estimateSize() {
        return relations.getRelationsCount() * MEMBER_ENTRY_SIZE + membersLength + tagsLength;
    }

    @Override
    protected Osmformat.PrimitiveGroup.Builder writeImpl() {
        return relations;
    }
}
