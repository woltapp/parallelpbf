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

/**
 * A better wrapper over BlobHeader.
 * <p>
 * Keeps blob size and blob type together for future processing.
 *
 * @param size Data blob size.
 * @param type Data blob type.
 */
public record BlobInformation(Integer size, String type) {
    /* OSM PBF Fileformat block types. See https://wiki.openstreetmap.org/wiki/PBF_Format for the details */
    /**
     * OSMData type block.
     */
    public static final String TYPE_OSM_DATA = "OSMData";
    /**
     * OSMHeader type block.
     */
    public static final String TYPE_OSM_HEADER = "OSMHeader";
}
