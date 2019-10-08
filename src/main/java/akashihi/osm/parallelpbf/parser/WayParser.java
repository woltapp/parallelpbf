package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Implements OSM Way parser.
 *
 * @param <M> PBF way type.
 * @param <T> Way typed callback.
 */
@Slf4j
public final class WayParser<M extends Osmformat.Way, T extends Consumer<Way>> extends BaseParser<M, T> {
    /**
     * Parent compatible constructor that sets callback and string table.
     * @param callback Callback to call on successful parse.
     * @param stringTable String table to use while parsing.
     */
    public WayParser(final T callback, final Osmformat.StringTable stringTable) {
        super(callback, stringTable);
    }

    @Override
    public void parse(final M message) {
        long nodeId = 0;
        Way way = new Way(message.getId());
        way.setTags(parseTags(message.getKeysList(), message.getValsList()));
        way.setInfo(parseInfo(message));
        for (Long node : message.getRefsList()) {
            nodeId += node;
            way.getNodes().add(nodeId);
        }
        log.debug(way.toString());
        getCallback().accept(way);
    }
}
