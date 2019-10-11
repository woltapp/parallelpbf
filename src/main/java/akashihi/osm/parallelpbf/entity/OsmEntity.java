package akashihi.osm.parallelpbf.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for the all OSM entities.
 *
 * All OSM v0.6 API entities have id and tags,
 * presented as unique keys with their values.
 *
 * For a PBF format we also store metadata for the entity.
 * @see Info
 */
@Data
public abstract class OsmEntity {
    /**
     * Entry id.
     */
    private final long id;

    /**
     * Entry tags map. May be empty.
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * Entry metadata, can be null.
     *
     * @see Info
     */
    private Info info;
}
