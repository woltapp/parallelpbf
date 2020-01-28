package com.wolt.osm.parallelpbf.encoder;

/**
 * Base class for all encoders, provides common encoding functions.
 */
public abstract class OsmEncoder {
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
