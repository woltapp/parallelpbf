package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.BoundBox;
import com.wolt.osm.parallelpbf.entity.Header;
import crosby.binary.Osmformat;

/**
 * HeaderBlock encoder.
 */
public final class OsmHeaderEncoder extends OsmEncoder {
    /**
     * Wraps bound box to OSM PBF entity.
     * @param boundBox Bound box to wrap.
     * @return HeaderBBox entity.
     */
    private static Osmformat.HeaderBBox encodeBoundBox(final BoundBox boundBox) {
        return Osmformat.HeaderBBox.newBuilder()
                .setLeft(doubleToNanoScaled(boundBox.left()))
                .setTop(doubleToNanoScaled(boundBox.top()))
                .setRight(doubleToNanoScaled(boundBox.right()))
                .setBottom(doubleToNanoScaled(boundBox.bottom()))
                .build();
    }

    /**
     * Generates OSM PBF header and add (optional) bounding box to it.
     * Header values are predefined and can't be set right now.
     * @param boundBox Bounding box to include into header. May be null.
     * @return array of bytes with binary representation of the header.
     */
    public static byte[] encodeHeader(final BoundBox boundBox) {
        Osmformat.HeaderBlock.Builder blob = Osmformat.HeaderBlock.newBuilder();

        if (boundBox != null) {
            blob.setBbox(encodeBoundBox(boundBox));
        }

        blob.addRequiredFeatures(Header.FEATURE_OSM_SCHEMA);
        blob.addRequiredFeatures(Header.FEATURE_DENSE_NODES);

        blob.setWritingprogram("parallelpbf");

        return blob.build().toByteArray();
    }
}
