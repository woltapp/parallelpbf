package com.wolt.osm.parallelpbf.encoder;

/**
 * Base class for all encoders, provides common encoding functions.
 */
public abstract class OsmEncoder {
    /**
     * Single tag entry (key or value) is a integer index,
     * so 4 bytes per entry.
     */
    protected static final int TAG_ENTRY_SIZE = 4;

    /**
     * Single member entry (key or value) is a long value,
     * so 8 bytes per entry keeping both of them.
     */
    protected static final int MEMBER_ENTRY_SIZE = 8;

    /**
     * Conversion from nano- to non-scaled.
     */
    private static final double NANO = 1e9;

    /**
     * Convert double to nano-scaled long.
     * @param value double to convert.
     * @return value multiplied to 1e9 and rounded then.
     */
    protected static long doubleToNanoScaled(final double value) {
        return Math.round(value * NANO);
    }
}
