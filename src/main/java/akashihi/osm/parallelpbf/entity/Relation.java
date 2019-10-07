package akashihi.osm.parallelpbf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Relation extends OsmEntity {
    public Relation(long id) {
        super(id);
    }

    private List<RelationMember> members = new LinkedList<>();
}
