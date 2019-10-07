package akashihi.osm.parallelpbf.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
abstract class OsmEntity {
    final long id;
    Map<String, String> tags = new HashMap<>();
    Info info;
}
