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

package com.wolt.osm.parallelpbf.entity;

import java.util.List;

/**
 * OSM PBF file header. Is not a part of the OSM v0.6 API.
 *
 * @param requiredFeatures List of features, required to read and process PBF data.
 * @param optionalFeatures List of optional features, that mey be present in PBF file.
 *                         <p>
 *                         Please, pay attention, that those features may include soring features,
 *                         like 'Sort.Type_then_ID', 'Sort.Geographic' etc.
 *                         Due to the asynchronous nature of parallel processing,
 *                         that order is not guaranteed to be kept during
 *                         reading procedure.
 * @param writingProgram   Optional name of the PBF file's origination program. May be null.
 * @param source           Optional source information. May be null.
 */
public record Header(List<String> requiredFeatures, List<String> optionalFeatures, String writingProgram,
                     String source) {
    /*
    Definition os OSM PBF features. See https://wiki.openstreetmap.org/wiki/PBF_Format for details.
     */
  /**
   * "OsmSchema-V0.6" — File contains data with the OSM v0.6 schema.
   */
  public static final String FEATURE_OSM_SCHEMA = "OsmSchema-V0.6";

  /**
   * "DenseNodes" — File contains dense nodes and dense info.
   */
  public static final String FEATURE_DENSE_NODES = "DenseNodes";

  /**
   * "HistoricalInformation" — File contains historical OSM data.
   */
  public static final String FEATURE_HISTORICAL_INFORMATION = "HistoricalInformation";

}
