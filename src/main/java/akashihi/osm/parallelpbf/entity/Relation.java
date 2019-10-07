package akashihi.osm.parallelpbf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * OSM Relation entity.
 *
 * Groups several OSM entities (including other relations)
 * to the single logical entity.
 *
 * @see RelationMember
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Relation extends OsmEntity {
    /**
     * Entity constructor.
     * @param id Sets required object id during construction.
     */
    public Relation(final long id) {
        super(id);
    }

    /**
     * Ordered list of relation members. Can be empty.
     */
    private final List<RelationMember> members = new LinkedList<>();
}
