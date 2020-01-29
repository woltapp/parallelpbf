package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.OsmEntity;

/**
 * Entity specific extension of OsmEncoder.
 * @param <T> Type of entity that encoder supports.
 */
public abstract class OsmEntityEncoder<T extends OsmEntity> extends OsmEncoder {
    /**
     * 'Write was called' flag.
     */
    private boolean built = false;

    protected abstract void addImpl(T entity);
    protected abstract byte[] writeImpl();

    /**
     * Add entity to the encoder.
     * @param entity Entity to add.
     * @throws IllegalStateException when call after write() call.
     */
    public void add(T entity) {
        if (built) {
            throw new IllegalStateException("Encoder content is already written");
        }
        addImpl(entity);
    }

    /**
     * Provides approximate size of the future blob.
     * @return Estimated approximate maximum size of a blob.
     */
    public abstract int estimateSize();

    /**
     * Build a blob from the collected data. Encoder will become
     * unusable after that call.
     * @return OSM PBF primitiveBlock blob.
     */
    public byte[] write() {
        built = true;
        return writeImpl();
    }
}
