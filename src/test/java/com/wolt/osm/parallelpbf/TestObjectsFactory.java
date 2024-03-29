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

package com.wolt.osm.parallelpbf;

import com.wolt.osm.parallelpbf.entity.*;
import com.google.protobuf.ByteString;
import crosby.binary.Osmformat;

public class TestObjectsFactory {
    public static final Info info = new Info(1, "test", 3, 4, 5, true);

    public static final Osmformat.Info infoMessage = Osmformat.Info.newBuilder().setUid(1).setUserSid(2).setVersion(3).setTimestamp(4).setChangeset(5).setVisible(true).build();
    public static final Osmformat.Info infoMessageWithNullVisibleFlag = Osmformat.Info.newBuilder()
             .setUid(1).setUserSid(2).setVersion(3).setTimestamp(4).setChangeset(5).build();

    public static final Osmformat.StringTable stringTable = Osmformat.StringTable.newBuilder()
            .addS(ByteString.copyFromUtf8(""))
            .addS(ByteString.copyFromUtf8("fail"))
            .addS(ByteString.copyFromUtf8("test"))
            .addS(ByteString.copyFromUtf8("tag"))
            .addS(ByteString.copyFromUtf8("value")).build();

    public static final Osmformat.Node nodeMessage = Osmformat.Node.newBuilder()
            .setId(1)
            .setLat(1000000000)
            .setLon(2000000000)
            .addKeys(3)
            .addVals(4)
            .setInfo(TestObjectsFactory.infoMessage)
            .build();

    public static final Osmformat.DenseInfo denseInfo = Osmformat.DenseInfo.newBuilder()
            .addUid(1)
            .addUserSid(2)
            .addVersion(3)
            .addTimestamp(4)
            .addChangeset(5)
            .addVisible(true)
            .build();
    public static final Osmformat.DenseNodes denseNodesMessage = Osmformat.DenseNodes.newBuilder()
            .addId(1)
            .addLat(1000000000)
            .addLon(2000000000)
            .addKeysVals(3).addKeysVals(4).addKeysVals(0)
            .setDenseinfo(denseInfo)
            .build();

    public static final Osmformat.Way wayMessage = Osmformat.Way.newBuilder()
            .setId(1)
            .addKeys(3)
            .addVals(4)
            .setInfo(TestObjectsFactory.infoMessage)
            .addRefs(9000)
            .build();

    public static final Osmformat.Way wayMessageWithNullVisibleFlag = Osmformat.Way.newBuilder()
            .setId(1)
            .addKeys(3)
            .addVals(4)
            .setInfo(TestObjectsFactory.infoMessageWithNullVisibleFlag)
            .addRefs(9000)
            .build();

    public static final Osmformat.Relation relationMessage = Osmformat.Relation.newBuilder()
            .setId(1)
            .addKeys(3)
            .addVals(4)
            .setInfo(TestObjectsFactory.infoMessage)
            .addMemids(9000)
            .addTypes(Osmformat.Relation.MemberType.NODE)
            .addRolesSid(1)
            .build();

    public static final Osmformat.ChangeSet changesetMessage = Osmformat.ChangeSet.newBuilder().setId(1).build();

    public static final String testTag = "test";

    public static Node node() {
        Node node = new Node(1, 10.0, 50.0);
        node.getTags().put(testTag, testTag);
        return node;
    }

    public static Way way() {
        Way way = new Way(1L);
        way.getTags().put(TestObjectsFactory.testTag, TestObjectsFactory.testTag);
        way.getNodes().add(3L);
        way.getNodes().add(6L);
        way.getNodes().add(2L);
        way.setInfo(info);
        return way;
    }

    public static Relation relation() {
        RelationMember member1 = new RelationMember(2L, "test", RelationMember.Type.WAY);
        RelationMember member2 = new RelationMember(3L, "forward", RelationMember.Type.RELATION);
        RelationMember member3 = new RelationMember(6L, "stop", RelationMember.Type.NODE);
        Relation relation = new Relation(1L);
        relation.getTags().put(TestObjectsFactory.testTag, TestObjectsFactory.testTag);
        relation.getMembers().add(member1);
        relation.getMembers().add(member2);
        relation.getMembers().add(member3);
        relation.setInfo(info);
        return relation;
    }
}
