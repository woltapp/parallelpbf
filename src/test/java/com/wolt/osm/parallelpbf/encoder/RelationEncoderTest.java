package com.wolt.osm.parallelpbf.encoder;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.RelationMember;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelationEncoderTest {
    @Test
    void testRelationSize() {
        RelationMember member = new RelationMember(2L, "test", RelationMember.Type.WAY);
        Relation relation = new Relation(1L);
        relation.getTags().put("test", "test");
        relation.getMembers().add(member);

        RelationEncoder testedObject = new RelationEncoder();
        testedObject.add(relation);

        assertEquals(33, testedObject.estimateSize());
    }

    @Test
    public void testWrite() throws InvalidProtocolBufferException {
        String str = "test";
        RelationMember member1 = new RelationMember(2L, "test", RelationMember.Type.WAY);
        RelationMember member2 = new RelationMember(3L, "forward", RelationMember.Type.RELATION);
        RelationMember member3 = new RelationMember(6L, "stop", RelationMember.Type.NODE);
        Relation relation = new Relation(1L);
        relation.getTags().put(str, str);
        relation.getMembers().add(member1);
        relation.getMembers().add(member2);
        relation.getMembers().add(member3);

        RelationEncoder testedObject = new RelationEncoder();
        testedObject.add(relation);

        byte[] blob = testedObject.write();

        Osmformat.PrimitiveBlock actual = Osmformat.PrimitiveBlock.parseFrom(blob);

        Osmformat.StringTable stringTable  = actual.getStringtable();
        assertEquals(ByteString.EMPTY, stringTable.getS(0));
        assertEquals(str, stringTable.getS(1).toStringUtf8());

        Osmformat.Relation r = actual.getPrimitivegroup(0).getRelations(0);
        assertEquals(1, r.getId());
        assertEquals(1, r.getKeys(0));
        assertEquals(1, r.getVals(0));

        assertEquals(1, r.getRolesSid(0));
        assertEquals(2, r.getRolesSid(1));
        assertEquals(3, r.getRolesSid(2));

        assertEquals(Osmformat.Relation.MemberType.WAY, r.getTypes(0));
        assertEquals(Osmformat.Relation.MemberType.RELATION, r.getTypes(1));
        assertEquals(Osmformat.Relation.MemberType.NODE, r.getTypes(2));

        assertEquals(2, r.getMemids(0));
        assertEquals(1, r.getMemids(1));
        assertEquals(3, r.getMemids(2));
    }

    @Test
    public void testNoUseAfterWrite() {
        Relation relation = new Relation(1L);

        RelationEncoder testedObject = new RelationEncoder();
        testedObject.add(relation);
        testedObject.write();
        assertThrows(IllegalStateException.class, () -> testedObject.add(relation));
    }

}