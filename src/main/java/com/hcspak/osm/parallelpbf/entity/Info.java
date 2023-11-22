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

package com.hcspak.osm.parallelpbf.entity;

/**
 * An OSM entity metadata. Stores author and versioning information.
 * <p>
 * Most of publicly available PBF files will not contain author id,
 * name and changeset id due to privacy regulations.
 *
 * @param uid       Author id of that object version. May be null.
 * @param username  Author username of that object version. May be null.
 * @param version   Object version. May be null or '-1', both cases marks
 *                  missing version.
 * @param timestamp Object version creation timestamp in milliseconds
 *                  since epoch time.
 * @param changeset Changeset related to that version.
 * @param visible   If set false, ut indicates that the current object version
 *                  has been created by a delete operation on the OSM API.
 *                  <p>
 *                  May be null in which case should be understood as 'true'
 */
public record Info(int uid, String username, int version, long timestamp, long changeset, boolean visible) {
}
