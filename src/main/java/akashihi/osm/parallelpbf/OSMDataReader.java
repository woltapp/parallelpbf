package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.*;
import akashihi.osm.parallelpbf.parser.RelationParser;
import com.google.protobuf.InvalidProtocolBufferException;
import crosby.binary.Osmformat;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

@Slf4j
public class OSMDataReader extends OSMReader {
    /**
     * Nano degrees scale.
     */
    private static final double NANO = .000000001;

    /**
     * Changeset processing callback. Must be reentrant.
     */
    private final Consumer<Long> changesetsCb;

    /**
     * Nodes processing callback. Must be reentrant.
     */
    private final Consumer<Node> nodesCb;

    /**
     * Ways processing callback. Must be reentrant.
     */
    private final Consumer<Way> waysCb;

    /**
     * Relations processing callback. Must be reentrant.
     */
    private final Consumer<Relation> relationsCb;

    /**
     * Blob's stringtable is published here during parse process.
     */
    private Osmformat.StringTable stringTable;

    OSMDataReader(final byte[] blob, final Semaphore tasksLimiter, final Consumer<Node> onNodes, final Consumer<Way> onWays, final Consumer<Relation> onRelations, final Consumer<Long> onChangesets) {
        super(blob, tasksLimiter);
        this.nodesCb = onNodes;
        this.waysCb = onWays;
        this.relationsCb = onRelations;
        this.changesetsCb = onChangesets;
    }

    private Map<String, String> parseTags(final List<Integer> keys, final List<Integer> values) {
        HashMap<String, String> result = new HashMap<>();
        for (int indx = 0; indx < keys.size(); ++indx) {
            String key = stringTable.getS(keys.get(indx)).toStringUtf8();
            String value = stringTable.getS(values.get(indx)).toStringUtf8();
            result.put(key, value);
        }
        return result;
    }

    private <M> Info parseInfo(final M message) {
        Osmformat.Info infoMessage = null;
        if (message instanceof Osmformat.Node) {
            Osmformat.Node nodeMessage = (Osmformat.Node) message;
            if (nodeMessage.hasInfo()) {
                infoMessage = nodeMessage.getInfo();
            }
        }
        if (message instanceof Osmformat.Way) {
            Osmformat.Way wayMessage = (Osmformat.Way) message;
            if (wayMessage.hasInfo()) {
                infoMessage = wayMessage.getInfo();
            }
        }
        if (message instanceof Osmformat.Relation) {
            Osmformat.Relation relMessage = (Osmformat.Relation) message;
            if (relMessage.hasInfo()) {
                infoMessage = relMessage.getInfo();
            }
        }
        if (infoMessage != null) {
            String username = stringTable.getS(infoMessage.getUserSid()).toStringUtf8();
            return new Info(infoMessage.getUid(), username, infoMessage.getVersion(), infoMessage.getTimestamp(), infoMessage.getChangeset(), infoMessage.getVisible());
        }
        return null;
    }

    private void relationParser(final Osmformat.Relation relationMessage) {
        long memberId = 0;
        Relation relation = new Relation(relationMessage.getId());
        relation.setTags(parseTags(relationMessage.getKeysList(), relationMessage.getValsList()));
        relation.setInfo(parseInfo(relationMessage));
        for (int indx = 0; indx < relationMessage.getRolesSidCount(); ++indx) {
            String role = stringTable.getS(relationMessage.getRolesSid(indx)).toStringUtf8();
            memberId += relationMessage.getMemids(indx);
            RelationMember.Type type = RelationMember.Type.get(relationMessage.getTypes(indx).getNumber());
            RelationMember member = new RelationMember(memberId, role, type);
            relation.getMembers().add(member);
        }

        log.debug(relation.toString());
        relationsCb.accept(relation);
    }

    private void wayParser(final Osmformat.Way wayMessage) {
        long nodeId = 0;
        Way way = new Way(wayMessage.getId());
        way.setTags(parseTags(wayMessage.getKeysList(), wayMessage.getValsList()));
        way.setInfo(parseInfo(wayMessage));
        for (Long node : wayMessage.getRefsList()) {
            nodeId += node;
            way.getNodes().add(nodeId);
        }
        log.debug(way.toString());
        waysCb.accept(way);
    }

    private void parseDenseNodes(final Osmformat.DenseNodes nodes, final int granularity, final long latOffset, final long lonOffset, final int dateGranularity) {
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
                    String key = stringTable.getS(keyIndex).toStringUtf8();
                    String value = stringTable.getS(valueIndex).toStringUtf8();
                    node.getTags().put(key, value);
                }
            }
            if (nodes.hasDenseinfo()) {
                Osmformat.DenseInfo infoMessage = nodes.getDenseinfo();
                uid += infoMessage.getUid(indx);
                usernameStringId += infoMessage.getUserSid(indx);
                String username = stringTable.getS(usernameStringId).toStringUtf8();
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
            nodesCb.accept(node);

        }
    }

    private Consumer<Osmformat.Node> makeNodeParser(final int granularity, final long latOffset, final long lonOffset) {
        return (nodeMessage) -> {
            double latitude = NANO * (latOffset + (granularity * nodeMessage.getLat()));
            double longitude = NANO * (lonOffset + (granularity * nodeMessage.getLon()));
            Node node = new Node(nodeMessage.getId(), latitude, longitude);
            node.setTags(parseTags(nodeMessage.getKeysList(), nodeMessage.getValsList()));
            node.setInfo(parseInfo(nodeMessage));
            log.debug(node.toString());
            nodesCb.accept(node);
        };
    }

    /**
     * Extracts primitives groups from the Blob and parses them.
     * <p>
     * In case callback for some of the primitives is not set, it will
     * be ignored and not parsed.
     *
     * @param message Raw OSMData blob.
     * @throws RuntimeException in case of protobuf parsing error.
     */
    @Override
    protected void read(final byte[] message) {
        Osmformat.PrimitiveBlock primitives;
        try {
            primitives = Osmformat.PrimitiveBlock.parseFrom(message);
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing OSMData block: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        stringTable = primitives.getStringtable();
        List<Osmformat.PrimitiveGroup> groups = primitives.getPrimitivegroupList();
        for (Osmformat.PrimitiveGroup group : groups) {
            if (nodesCb != null) {
                Consumer<Osmformat.Node> nodeParser = makeNodeParser(primitives.getGranularity(), primitives.getLatOffset(), primitives.getLonOffset());
                group.getNodesList().forEach(nodeParser);
                if (group.hasDense()) {
                    parseDenseNodes(group.getDense(), primitives.getGranularity(), primitives.getLatOffset(), primitives.getLonOffset(), primitives.getDateGranularity());
                }
            }
            if (waysCb != null) {
                group.getWaysList().forEach(this::wayParser);
            }
            if (relationsCb != null) {
                var parser = new RelationParser<Osmformat.Relation, Consumer<Relation>>(relationsCb, stringTable);
                group.getRelationsList().forEach(parser::parse);
            }
            if (changesetsCb != null) {
                group.getChangesetsList().forEach(changeMessage -> {
                    long id = changeMessage.getId();
                    log.debug("ChangeSet id: {}", id);
                    changesetsCb.accept(id);
                });
            }
        }
    }
}
