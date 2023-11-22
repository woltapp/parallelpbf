package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.entity.OsmEntity;
import crosby.binary.Osmformat;

/**
 * Entity specific extension of OsmEncoder.
 *
 * @param <T> Type of entity that encoder supports.
 */
public abstract class OsmEntityEncoder<T extends OsmEntity> extends OsmEncoder {
  /**
   * 'Write was called' flag.
   */
  private boolean built = false;

  /**
   * Type specific write implementation.
   *
   * @param entity Osm entity to add to the encoder.
   */
  protected abstract void addImpl(T entity);

  /**
   * Type specific group writer implementation.
   *
   * @return Group with entities of T type.
   */
  protected abstract Osmformat.PrimitiveGroup.Builder writeImpl();

  /**
   * Add entity to the encoder.
   *
   * @param entity Entity to add.
   * @throws IllegalStateException when call after write() call.
   */
  public void add(final T entity) {
    if (built) {
      throw new IllegalStateException("Encoder content is already written");
    }
    addImpl(entity);
  }

  /**
   * Provides approximate size of the future blob.
   *
   * @return Estimated approximate maximum size of a blob.
   */
  public abstract int estimateSize();

  /**
   * Build a blob from the collected data. Encoder will become
   * unusable after that call.
   *
   * @return OSM PBF primitiveBlock blob.
   */
  public Osmformat.PrimitiveGroup.Builder write() {
    built = true;
    return writeImpl();
  }
}
