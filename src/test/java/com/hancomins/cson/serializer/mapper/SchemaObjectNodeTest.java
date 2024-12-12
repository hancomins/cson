package com.hancomins.cson.serializer.mapper;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SchemaObjectNodeTest {

    public static class TestA {
        public int a;
        @CSONValue("a")
        public String b;

        public AtomicInteger aa = new AtomicInteger();

        @CSONValueSetter("a")
        public void setValue(int value) {
            aa.set(value);
        }

        TestB testB;
        TestB testB_1;

        @CSONValue("testB.a")
        public int aTestB;

    }

    public static class TestB {
        public int a;
        public String b;

        public void TestB() {
            a = 1;
            b = "1";
        }
    }


    @Test
    void test() {
        //TypeSchema typeSchema = TypeSchema.create(TestA.class);

        SchemaObjectNode rootNode =  RootObjectNodeMap.getInstance().getObjectNode(TestA.class);
        assertNotNull(rootNode);
        ISchemaNode node = rootNode.get("a");
        assertNotNull(node);




        ISchemaNode schemaNodeB = rootNode.get("testB");
        ISchemaNode schemaNodeB_1 = rootNode.get("testB_1");


        assertNotEquals(schemaNodeB.toString(), schemaNodeB_1.toString());



        SchemaFieldNormal normal = (SchemaFieldNormal) ((SchemaObjectNode) schemaNodeB).get("a");

        //normal.setValue(new TestA(), 1);

        /*TestA testA = new TestA();
        if(nodeType == NodeType.NORMAL_FIELD) {
            SchemaFieldNormal valueNode = (SchemaFieldNormal) node;
            valueNode.setValue(testA, 1);
            assertEquals(testA.a, 1);
            assertEquals(testA.b, "1");
            assertEquals(testA.aa.get(), 1);
        } else {
            fail();
        }*/
    }


}