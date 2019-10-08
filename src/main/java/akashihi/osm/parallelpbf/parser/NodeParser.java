package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Info;
import akashihi.osm.parallelpbf.entity.Node;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.util.function.Consumer;

/**
 * Implements OSM Way parser.
 *
 * Can parse both primitive and dense nodes.
 */
@Slf4j
public final class NodeParser extends BaseParser<Osmformat.Node, Consumer<Node>> {
    /**
     * Nano degrees scale.
     */
    private static final double NANO = .000000001;

    /**
     * Granularity, units of nanodegrees, used to store coordinates.
     */
    private final int granularity;

    /**
     * Offset value between the output coordinates coordinates and the granularity grid, in units of nanodegrees.
     * Latitude part.
     */
    private final long latOffset;

    /**
     * Offset value between the output coordinates coordinates and the granularity grid, in units of nanodegrees.
     * Longitude part.
     */
    private final long lonOffset;

    /**
     * Granularity of dates, normally represented in units of milliseconds since the 1970 epoch.
     */
    private final int dateGranularity;

    /**
     * Sets all the node parsing parameters from the primitive message.
     * @param callback Callback to call on successful parse.
     * @param stringTable String table to use while parsing.
     * @param granularityValue Grid granularity value.
     * @param latOffsetValue Latitude offset of the grid.
     * @param lonOffsetValue Longitude offset of the grid.
     * @param dateGranularityValue Date granularity value.
     */
    public NodeParser(final Consumer<Node> callback,
                      final Osmformat.StringTable stringTable,
                      final int granularityValue,
                      final long latOffsetValue,
                      final long lonOffsetValue,
                      final int dateGranularityValue) {
        super(callback, stringTable);
        this.granularity = granularityValue;
        this.latOffset = latOffsetValue;
        this.lonOffset = lonOffsetValue;
        this.dateGranularity = dateGranularityValue;
    }

    @Override
    public void parse(final Osmformat.Node message) {
        double latitude = NANO * (latOffset + (granularity * message.getLat()));
        double longitude = NANO * (lonOffset + (granularity * message.getLon()));
        Node node = new Node(message.getId(), latitude, longitude);
        node.setTags(parseTags(message.getKeysList(), message.getValsList()));
        node.setInfo(parseInfo(message));
        log.debug(node.toString());
        getCallback().accept(node);
    }

    /**
     * Parses nodes in DenseFormat.
     * @param nodes list of DenseNodes messages.
     */
    public void parse(final Osmformat.DenseNodes nodes) {
        int tagsKeyValuePointer = 0;
        long id = 0;
        double latitude = 0;
        double longitude = 0;

        long timestamp = 0;
        long changeset = 0;
        int uid = 0;
        int usernameStringId = 0;
        for (int indx = 0; indx < nodes.getIdCount(); indx++) {
            id += nodes.getId(indx);
            latitude += NANO * (latOffset + (granularity * nodes.getLat(indx)));
            longitude += NANO * (lonOffset + (granularity * nodes.getLon(indx)));

            Node node = new Node(id, latitude, longitude);
            if (nodes.getKeysValsCount() > 0) {
                while (true) {
                    int keyIndex = nodes.getKeysVals(tagsKeyValuePointer);
                    ++tagsKeyValuePointer;
                    if (keyIndex == 0) {
                        break;
                    }
                    int valueIndex = nodes.getKeysVals(tagsKeyValuePointer);
                    ++tagsKeyValuePointer;
                    String key = getStringTable().getS(keyIndex).toStringUtf8();
                    String value = getStringTable().getS(valueIndex).toStringUtf8();
                    node.getTags().put(key, value);
                }
            }
            if (nodes.hasDenseinfo()) {
                var infoMessage = nodes.getDenseinfo();
                uid += infoMessage.getUid(indx);
                usernameStringId += infoMessage.getUserSid(indx);
                String username = getStringTable().getS(usernameStringId).toStringUtf8();
                changeset += infoMessage.getChangeset(indx);
                timestamp += infoMessage.getTimestamp(indx);
                int version = infoMessage.getVersion(indx);
                boolean visible;
                if (infoMessage.getVisibleCount() > 0) {
                    visible = infoMessage.getVisible(indx);
                } else {
                    visible = true;
                }
                Info info = new Info(uid, username, version, timestamp * dateGranularity, changeset, visible);
                node.setInfo(info);
            }
            log.debug(node.toString());
            getCallback().accept(node);

        }
    }
}
