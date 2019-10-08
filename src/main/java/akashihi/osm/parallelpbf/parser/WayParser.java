package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Way;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Implements OSM Way parser.
 *
 */
@Slf4j
public final class WayParser extends BaseParser<Osmformat.Way, Consumer<Way>> {
    /**
     * Parent compatible constructor that sets callback and string table.
     * @param callback Callback to call on successful parse.
     * @param stringTable String table to use while parsing.
     */
    public WayParser(final Consumer<Way> callback, final Osmformat.StringTable stringTable) {
        super(callback, stringTable);
    }

    @Override
    public void parse(final Osmformat.Way message) {
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
