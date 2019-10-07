package akashihi.osm.parallelpbf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * OSM Way entity.
 *
 * Way is a ordered, therefore directed, collection of nodes.
 *
 * @see Node
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Way extends OsmEntity {
    /**
     * Constructs Way setting mandatory fields.
     * @param id Required object id.
     */
    public Way(final long id) {
        super(id);
    }

    /**
     * Ordered list of nodes, making way. Should contain at least one node.
     *
     * @see Node
     */
    private final List<Long> nodes = new LinkedList<>();
}
