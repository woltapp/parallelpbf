package akashihi.osm.parallelpbf.parser;

import akashihi.osm.parallelpbf.entity.Info;
import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;

class ParseTestObjects {
    static final Info info = new Info(1, "test", 3, 4, 5, true);
    static final Osmformat.Info infoMessage = Osmformat.Info.newBuilder().setUid(1).setUserSid(2).setVersion(3).setTimestamp(4).setChangeset(5).setVisible(true).build();
    static final Osmformat.StringTable stringTable = Osmformat.StringTable.newBuilder()
            .addS(ByteString.copyFromUtf8(""))
            .addS(ByteString.copyFromUtf8("fail"))
            .addS(ByteString.copyFromUtf8("test"))
            .addS(ByteString.copyFromUtf8("tag"))
            .addS(ByteString.copyFromUtf8("value")).build();

}
