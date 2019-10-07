package akashihi.osm.parallelpbf.blob;

import lombok.Data;

/**
 * A better wrapper over BlobHeader.
 *
 * Keeps blob size and blob type together for future processing.
 */
@Data
public class BlobInformation {
    /**
     * Data blob size.
     */
    private final Integer size;

    /**
     * Data blob type.
     */
    private final String type;
}
