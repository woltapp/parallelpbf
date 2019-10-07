package akashihi.osm.parallelpbf.entity;

import lombok.Data;

/**
 * Wrapper of the bounding box of the map parsed.
 */
@Data
public final class BoundBox {
    /**
     * Lesser longitude of a bounding box.
     */
    private final double left;

    /**
     * Lesser latitude of a bounding box.
     */
    private final double top;

    /**
     * Bigger longitude of a bounding box.
     */
    private final double right;

    /**
     * Bigger latitude of a bounding gox.
     */
    private final double botttom;
}
