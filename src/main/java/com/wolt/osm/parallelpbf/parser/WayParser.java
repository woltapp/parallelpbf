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

package com.wolt.osm.parallelpbf.parser;

import com.wolt.osm.parallelpbf.entity.Way;
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
        if (log.isDebugEnabled()) {
            log.debug(way.toString());
        }
        getCallback().accept(way);
    }
}
