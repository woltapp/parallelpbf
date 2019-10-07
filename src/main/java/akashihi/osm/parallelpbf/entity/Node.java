package akashihi.osm.parallelpbf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OSM Node entity.
 *
 * Node is a most basic building block of the OSM database.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Node extends OsmEntity {
    /**
     * Constructs Node setting mandatory fields.
     * @param id Required node id.
     * @param latitude Node latitude.
     * @param longitude Node longitude
     */
    public Node(final long id, final double latitude, final double longitude) {
        super(id);
        this.lat = latitude;
        this.lon = longitude;
    }

    /**
     * Node latitude.
     */
    private final double lat;

    /**
     * Node longitude.
     */
    private final double lon;
}
