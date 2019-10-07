package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.Node;
import akashihi.osm.parallelpbf.entity.Info;
import akashihi.osm.parallelpbf.entity.Way;
import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class OSMDataReader extends OSMReader {
    private static Logger logger = LoggerFactory.getLogger(OSMDataReader.class);

    /**
     * Nodes processing callback. Must be reentrant.
     */
    final private Consumer<Node> parseNodes;

    /**
     * Ways processing callback. Must be reentrant.
     */
    final private Consumer<Way> parseWays;

    OSMDataReader(byte[] blob, Semaphore tasksLimiter, Consumer<Node> parseNodes, Consumer<Way> parseWays) {
        super(blob, tasksLimiter);
        this.parseNodes = parseNodes;
        this.parseWays = parseWays;
    }

    private void parseChangesets(List<Osmformat.ChangeSet> changesetsList) {
    }

    private void parseRelations(List<Osmformat.Relation> relationsList) {
    }

    private Consumer<Osmformat.Way> makeWayParser(Osmformat.StringTable strings) {
        return (wayMessage) -> {
            long nodeId = 0;
            Way way = new Way(wayMessage.getId());
            for(int indx = 0; indx < wayMessage.getKeysCount(); ++indx) {
                String key = strings.getS(wayMessage.getKeys(indx)).toStringUtf8();
                String value = strings.getS(wayMessage.getVals(indx)).toStringUtf8();
                way.getTags().put(key, value);
            }
            if (wayMessage.hasInfo()) {
                Osmformat.Info infoMessage = wayMessage.getInfo();
                String username = strings.getS(infoMessage.getUserSid()).toStringUtf8();
                Info info = new Info(infoMessage.getUid(), username, infoMessage.getVersion(), infoMessage.getTimestamp(), infoMessage.getChangeset(), infoMessage.getVisible());
                way.setInfo(info);
            }
            for(Long node : wayMessage.getRefsList()) {
                nodeId+=node;
                way.getNodes().add(nodeId);
            }
            logger.debug(way.toString());
            parseWays.accept(way);
        };
    }

    private void parseDenseNodes(Osmformat.DenseNodes nodes, int granularity, long lat_offset, long lon_offset, int date_granularity, Osmformat.StringTable strings) {
        int keyval_position = 0;
        long id = 0;
        double latitude = 0;
        double longitude = 0;

        long timestamp = 0;
        long changeset = 0;
        int uid = 0;
        int user_sid = 0;
        for(int indx = 0; indx < nodes.getIdCount(); indx++) {
            id += nodes.getId(indx);
            latitude += .000000001 * (lat_offset + (granularity * nodes.getLat(indx)));
            longitude += .000000001 * (lon_offset + (granularity * nodes.getLon(indx)));

            Node node = new Node(id, latitude, longitude);
            if (nodes.getKeysValsCount() > 0) {
                while(true) {
                    int key_indx = nodes.getKeysVals(keyval_position);
                    ++keyval_position;
                    if (key_indx == 0) {
                        break;
                    }
                    int val_indx = nodes.getKeysVals(keyval_position);
                    ++keyval_position;
                    String key = strings.getS(key_indx).toStringUtf8();
                    String value = strings.getS(val_indx).toStringUtf8();
                    node.getTags().put(key, value);
                }
            }
            if (nodes.hasDenseinfo()) {
                Osmformat.DenseInfo infoMessage = nodes.getDenseinfo();
                uid += infoMessage.getUid(indx);
                user_sid += infoMessage.getUserSid(indx);
                String username = strings.getS(user_sid).toStringUtf8();
                changeset += infoMessage.getChangeset(indx);
                timestamp += infoMessage.getTimestamp(indx);
                int version = infoMessage.getVersion(indx);
                boolean visible;
                if (infoMessage.getVisibleCount() > 0) {
                    visible = infoMessage.getVisible(indx);
                } else {
                    visible = true;
                }
                Info info = new Info(uid, username, version, timestamp * date_granularity, changeset, visible);
                node.setInfo(info);
            }
            logger.debug(node.toString());
            parseNodes.accept(node);

        }
    }

    private Consumer<Osmformat.Node> makeNodeParser(int granularity, long lat_offset, long lon_offset, Osmformat.StringTable strings) {
        return (nodeMessage) -> {
            double latitude = .000000001 * (lat_offset + (granularity * nodeMessage.getLat()));
            double longitude = .000000001 * (lon_offset + (granularity * nodeMessage.getLon()));
            Node node = new Node(nodeMessage.getId(), latitude, longitude);
            for(int indx = 0; indx < nodeMessage.getKeysCount(); ++indx) {
                String key = strings.getS(nodeMessage.getKeys(indx)).toStringUtf8();
                String value = strings.getS(nodeMessage.getVals(indx)).toStringUtf8();
                node.getTags().put(key, value);
            }
            if (nodeMessage.hasInfo()) {
                Osmformat.Info infoMessage = nodeMessage.getInfo();
                String username = strings.getS(infoMessage.getUserSid()).toStringUtf8();
                Info info = new Info(infoMessage.getUid(), username, infoMessage.getVersion(), infoMessage.getTimestamp(), infoMessage.getChangeset(), infoMessage.getVisible());
                node.setInfo(info);
            }
            logger.debug(node.toString());
            parseNodes.accept(node);
        };
    }

    @Override
    protected void read(byte[] message) {
        Osmformat.PrimitiveBlock primitives;
        try {
            primitives = Osmformat.PrimitiveBlock.parseFrom(message);
        } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing OSMData block: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        List<Osmformat.PrimitiveGroup> groups = primitives.getPrimitivegroupList();
        for (Osmformat.PrimitiveGroup group: groups) {
            if (parseNodes != null) {
                Consumer<Osmformat.Node> nodeParser = makeNodeParser(primitives.getGranularity(), primitives.getLatOffset(), primitives.getLonOffset(), primitives.getStringtable());
                group.getNodesList().forEach(nodeParser);
                if (group.hasDense()) {
                    parseDenseNodes(group.getDense(), primitives.getGranularity(), primitives.getLatOffset(), primitives.getLonOffset(), primitives.getDateGranularity(), primitives.getStringtable());
                }
            }
            if (parseWays != null) {
                Consumer<Osmformat.Way> wayParser = makeWayParser(primitives.getStringtable());
                group.getWaysList().forEach(wayParser);
            }
            parseRelations(group.getRelationsList());
            parseChangesets(group.getChangesetsList());
        }
    }
}