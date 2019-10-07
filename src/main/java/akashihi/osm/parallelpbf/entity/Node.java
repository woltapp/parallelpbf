package akashihi.osm.parallelpbf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Node extends OsmEntity {
    public Node(long id, double lat, double lon) {
        super(id);
        this.lat = lat;
        this.lon = lon;
    }

    final double lat;
    final double lon;
}
