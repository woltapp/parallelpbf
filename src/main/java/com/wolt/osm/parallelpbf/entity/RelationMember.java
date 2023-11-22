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

import lombok.Data;

/**
 * Single relation participant.
 *
 * @param id   Id of referenced entity.
 * @param role Role of the referenced entity in the relation.
 *             Can be null.
 * @param type Type of the referencing entity.
 * @see Relation
 */
public record RelationMember(Long id, String role, com.wolt.osm.parallelpbf.entity.RelationMember.Type type) {
    /**
     * Defines relation member types.
     * <p>
     * The values of the enum participants are linked to
     * the underlying protobuf definitions.
     */
    public enum Type {
        /**
         * Relation member is Node.
         *
         * @see Node
         */
        NODE(0),

        /**
         * Relation member is Way.
         *
         * @see Way
         */
        WAY(1),

        /**
         * Relation member is another Relation.
         *
         * @see Relation
         */
        RELATION(2);

        /**
         * A related protobuf relation member id.
         */
        private final int value;

        /**
         * Constructor for enum entry value.
         *
         * @param v Protobuf relation member id.
         * @see crosby.binary.Osmformat.Relation.MemberType
         */
        Type(final int v) {
            this.value = v;
        }

        /**
         * Finds proper enum entry by protobuf MemberType value.
         *
         * @param v Protobuf relation member id.
         * @return Matching enum entry.
         * @throws IllegalArgumentException in case of unknown member id.
         */
        public static Type get(final int v) {
            for (Type t : Type.values()) {
                if (t.value == v) {
                    return t;
                }
            }
            throw new IllegalArgumentException();
        }
    }

}
