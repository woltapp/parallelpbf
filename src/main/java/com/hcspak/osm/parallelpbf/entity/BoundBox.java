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
 * Wrapper of the bounding box of the map parsed.
 *
 * @param left   Lesser longitude of a bounding box.
 * @param top    Lesser latitude of a bounding box.
 * @param right  Bigger longitude of a bounding box.
 * @param bottom Bigger latitude of a bounding gox.
 */

public record BoundBox(double left, double top, double right, double bottom) {
}
