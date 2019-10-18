/*
 * This file is part of parallelpbf.
 *
 *     parallelpbf is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

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
