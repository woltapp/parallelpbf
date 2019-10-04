package akashihi.osm.parallelpbf.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Node {
    final long id;
    final double lat;
    final double lon;
    Map<String, String> tags = new HashMap<>();
    NodeInfo info;
}
