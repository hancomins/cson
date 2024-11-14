package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.serializer.CSONValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class _NodeBuilderTest {
    public static class TestClass {
        String a = "a";

        @CSONValue("b.obj.k")
        String objA = "objA";

        @CSONValue("b.obj")
        TestClassB b = new TestClassB();
    }

    public static class TestClassB {
        String k = "k";
    }


    _NodeBuilder nodeBuilder;


    @Test
    void test() {
        _ObjectNode node = new _ObjectNode();
        nodeBuilder = new _NodeBuilder(null);
        ClassSchema classSchema = ClassSchemaMap.getInstance().getTypeInfo(TestClass.class);
        _ObjectNode objectNode = nodeBuilder.makeNode(classSchema);

        assertNotNull(objectNode);
        _ObjectNode aNode = objectNode.getNode("a");
        assertNotNull(aNode);
        assertTrue(aNode.isEndPoint());
        _ObjectNode kNode = objectNode.getNode("b").getNode("obj").getNode("k");
        assertNotNull(kNode);
        assertTrue(kNode.isEndPoint());
        assertEquals(2, kNode.getFileSchemedPointerList().size());
    }



}