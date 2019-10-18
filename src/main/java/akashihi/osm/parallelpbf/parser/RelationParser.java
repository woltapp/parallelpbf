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

package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Relation;
import akashihi.osm.parallelpbf.entity.RelationMember;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.util.function.Consumer;

/**
 * Implements OSM Relation parser.
 *
 */
@Slf4j
public final class RelationParser extends BaseParser<Osmformat.Relation, Consumer<Relation>> {
    /**
     * Parent compatible constructor that sets callback and string table.
     * @param callback Callback to call on successful parse.
     * @param stringTable String table to use while parsing.
     */
    public RelationParser(final Consumer<Relation> callback, final Osmformat.StringTable stringTable) {
        super(callback, stringTable);
    }

    @Override
    public void parse(final Osmformat.Relation message) {
        long memberId = 0;
        var relation = new Relation(message.getId());
        relation.setTags(parseTags(message.getKeysList(), message.getValsList()));
        relation.setInfo(parseInfo(message));
        for (int indx = 0; indx < message.getRolesSidCount(); ++indx) {
            String role = getStringTable().getS(message.getRolesSid(indx)).toStringUtf8();
            memberId += message.getMemids(indx);
            var type = RelationMember.Type.get(message.getTypes(indx).getNumber());
            var member = new RelationMember(memberId, role, type);
            relation.getMembers().add(member);
        }

        log.debug(relation.toString());
        getCallback().accept(relation);
    }
}
