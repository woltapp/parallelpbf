package akashihi.osm.parallelpbf;

import akashihi.osm.parallelpbf.entity.Node;
import akashihi.osm.parallelpbf.entity.NodeInfo;
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

    OSMDataReader(byte[] blob, Semaphore tasksLimiter, Consumer<Node> parseNodes) {
        super(blob, tasksLimiter);
        this.parseNodes = parseNodes;
    }

    private void parseChangesets(List<Osmformat.ChangeSet> changesetsList) {
    }

    private void parseRelations(List<Osmformat.Relation> relationsList) {
    }

    private void parseWays(List<Osmformat.Way> waysList) {
    }

    private void parseDenseNodes(Osmformat.DenseNodes dense) {
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
                NodeInfo info = new NodeInfo(infoMessage.getUid(), infoMessage.getUserSid(), infoMessage.getVersion(), infoMessage.getTimestamp(), infoMessage.getChangeset(), infoMessage.getVisible());
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
                    parseDenseNodes(group.getDense());
                }
            }
            parseWays(group.getWaysList());
            parseRelations(group.getRelationsList());
            parseChangesets(group.getChangesetsList());
        }
    }
}
