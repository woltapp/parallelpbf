package akashihi.osm.parallelpbf.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Way extends OsmEntity {
    public Way(long id) {
        super(id);
    }

    List<Long> nodes = new LinkedList<>();
}
