package akashihi.osm.parallelpbf.entity;

import lombok.Data;

@Data
public class NodeInfo {
    final int uid;
    final String username;
    final int version;
    final long timestamp;
    final long changeset;
    final boolean visible;
}
