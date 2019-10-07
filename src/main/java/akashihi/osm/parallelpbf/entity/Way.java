package akashihi.osm.parallelpbf.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class Way {
    final Long id;
    Map<String, String> tags = new HashMap<>();
    List<Long> nodes = new LinkedList<>();
    NodeInfo info;
}
