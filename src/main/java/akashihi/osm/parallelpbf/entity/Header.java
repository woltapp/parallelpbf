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

import java.util.List;

/**
 * OSM PBF file header. Is not a part of the OSM v0.6 API.
 */
@Data
public class Header {
    /**
     * List of features, required to read and process PBF data.
     */
    private final List<String> requiredFeatures;
    /**
     * List of optional features, that mey be present in PBF file.
     *
     * Please, pay attention, that those features may include soring features,
     * like 'Sort.Type_then_ID', 'Sort.Geographic' etc.
     * Due to the asynchronous nature of parallel processing,
     * that order is not guaranteed to be kept during
     * reading procedure.
     */
    private final List<String> optionalFeatures;

    /**
     * Optional name of the PBF file's origination program. May be null.
     */
    private String writingProgram;

    /**
     * Optional source information. May be null.
     */
    private String source;
}
