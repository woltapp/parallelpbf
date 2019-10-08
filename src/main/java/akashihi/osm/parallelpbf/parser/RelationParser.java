package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Relation;
import akashihi.osm.parallelpbf.entity.RelationMember;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Implements OSM Relation parser.
 *
 * @param <M> PBF relation type.
 * @param <T> Relation typed callback.
 */
@Slf4j
public final class RelationParser<M extends Osmformat.Relation, T extends Consumer<Relation>> extends BaseParser<M, T> {
    /**
     * Parent compatible constructor, that sets callback ans string table.
     * @param callback Callback to call on successful parse.
     * @param stringTable String table to use while parsing.
     */
    public RelationParser(final T callback, final Osmformat.StringTable stringTable) {
        super(callback, stringTable);
    }

    @Override
    public void parse(final M message) {
        long memberId = 0;
        Relation relation = new Relation(message.getId());
        relation.setTags(parseTags(message.getKeysList(), message.getValsList()));
        relation.setInfo(parseInfo(message));
        for (int indx = 0; indx < message.getRolesSidCount(); ++indx) {
            String role = getStringTable().getS(message.getRolesSid(indx)).toStringUtf8();
            memberId += message.getMemids(indx);
            RelationMember.Type type = RelationMember.Type.get(message.getTypes(indx).getNumber());
            RelationMember member = new RelationMember(memberId, role, type);
            relation.getMembers().add(member);
        }

        log.debug(relation.toString());
        getCallback().accept(relation);
    }
}
