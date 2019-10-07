package akashihi.osm.parallelpbf.entity;

import lombok.Data;

/**
 * An OSM entity metadata. Stores author and versioning information.
 *
 * Most of publicly available PBF files will not contain author id,
 * name and changeset id due to privacy regulations.
 *
 */
@Data
public final class Info {
    /**
     * Author id of that object version. May be null.
     */
    private final int uid;

    /**
     * Author username of that object version. May be null.
     */
    private final String username;

    /**
     * Object version. May be null or '-1', both cases marks
     * missing version.
     */
    private final int version;

    /**
     * Object version creation timestamp in milliseconds
     * since epoch time.
     */
    private final long timestamp;

    /**
     * Changeset related to that version.
     */
    private final long changeset;

    /**
     * If set false, ut indicates that the current object version
     * has been created by a delete operation on the OSM API.
     *
     * May be null in which case should be understood as 'true'
     */
    private final boolean visible;
}
