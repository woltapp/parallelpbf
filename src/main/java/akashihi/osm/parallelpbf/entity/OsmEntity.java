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

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for the all OSM entities.
 *
 * All OSM v0.6 API entities have id and tags,
 * presented as unique keys with their values.
 *
 * For a PBF format we also store metadata for the entity.
 * @see Info
 */
@Data
public abstract class OsmEntity {
    /**
     * Entry id.
     */
    private final long id;

    /**
     * Entry tags map. May be empty.
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * Entry metadata, can be null.
     *
     * @see Info
     */
    private Info info;
}
