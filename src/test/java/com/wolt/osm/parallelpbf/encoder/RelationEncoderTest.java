package com.wolt.osm.parallelpbf.encoder;

import com.wolt.osm.parallelpbf.TestObjectsFactory;
import com.wolt.osm.parallelpbf.entity.Relation;
import com.wolt.osm.parallelpbf.entity.RelationMember;
import crosby.binary.Osmformat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelationEncoderTest {
    private StringTableEncoder stringEncoder;

    @BeforeEach
    public void setUp() {
        stringEncoder = new StringTableEncoder();
    }

    @Test
    void testRelationSize() {
        RelationEncoder testedObject = new RelationEncoder(stringEncoder);
        testedObject.add(TestObjectsFactory.relation());

        assertEquals(55, testedObject.estimateSize());
    }

    @Test
    public void testWrite() {
        RelationEncoder testedObject = new RelationEncoder(stringEncoder);
        testedObject.add(TestObjectsFactory.relation());

        Osmformat.PrimitiveGroup actual = testedObject.write().build();

        Osmformat.Relation r = actual.getRelations(0);
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

        assertEquals(3, r.getInfo().getVersion());
        assertEquals(1, r.getInfo().getUid());
        assertEquals(5, r.getInfo().getChangeset());



    }
}