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

package com.wolt.osm.parallelpbf.blob;

import lombok.Data;

/**
 * A better wrapper over BlobHeader.
 *
 * Keeps blob size and blob type together for future processing.
 */
@Data
public class BlobInformation {
    public static final String TYPE_OSM_DATA = "OSMData";
    public static final String TYPE_OSM_HEADER = "OSMHeader";
    /**
     * Data blob size.
     */
    private final Integer size;

    /**
     * Data blob type.
     */
    private final String type;
}
