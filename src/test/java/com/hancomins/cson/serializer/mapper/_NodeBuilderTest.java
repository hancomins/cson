package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONException;
import com.hancomins.cson.serializer.CSONValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class _NodeBuilderTest {



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
    @DisplayName("기본 노드 생성 테스트")
    void test() {
        _ObjectNode node = new _ObjectNode();
        nodeBuilder = new _NodeBuilder(null);
        ClassSchema classSchema = ClassSchemaMap.getInstance().getTypeInfo(TestClass.class);
        _ObjectNode rootObjectNode = nodeBuilder.makeNode(classSchema);
        assertNotNull(rootObjectNode);
        _ObjectNode aNode = rootObjectNode.getNode("a");
        assertNotNull(aNode);
        assertTrue(aNode.isEndPoint());
        _ObjectNode kNode = rootObjectNode.getNode("b").getNode("obj").getNode("k");
        List<Integer> idList = kNode.getFileSchemedPointerList().stream().map(_SchemaPointer::getParentId).collect(Collectors.toList());
        assertNotNull(kNode);
        assertTrue(kNode.isEndPoint());
        assertEquals(2, kNode.getFileSchemedPointerList().size());

        _SchemaPointer schemaPointer = rootObjectNode.getClassSchemaPointerList().get(0);
        int rootID = schemaPointer.getId();
        _ObjectNode objNode = rootObjectNode.getNode("b").getNode("obj");
        int objID = objNode.getClassSchemaPointerList().get(0).getId();
        int parentID = objNode.getClassSchemaPointerList().get(0).getParentId();

        List<Integer> classSchemaIDList = new ArrayList<>();
        classSchemaIDList.add(rootID);
        classSchemaIDList.add(objID);
        assertEquals(rootID, parentID);
        assertTrue(classSchemaIDList.containsAll(idList));




    }

    public static class ConflictParentClass {
        ConflictClass conflictClass = new ConflictClass();
    }

    public static class ConflictClass {
        String a = "a";
        @CSONValue("a")
        TestClass b = new TestClass();
    }

    @Test
    @DisplayName("키 충돌 예외 발생 테스트")
    void testForConflictValueType() {
        try {
            _ObjectNode node = new _ObjectNode();
            nodeBuilder = new _NodeBuilder(null);
            ClassSchema classSchema = ClassSchemaMap.getInstance().getTypeInfo(ConflictParentClass.class);
            _ObjectNode objectNode = nodeBuilder.makeNode(classSchema);
            fail();
        } catch (CSONException csonException) {
            csonException.printStackTrace();
        }
    }


    public static class ItemClassTest {
        String value = "a";

    }

    public static class CollectionTestClass {
        //List<ItemClassTest> list = new ArrayList<>();
        //@CSONValue("list[0]")
        //String zeroIndex = "zeroIndex";


        List<Set<ItemClassTest>> list;

    }

    @Test
    @DisplayName("컬렉션 노드 테스트")
    void testForCollectionNode() {
        ClassSchema classSchema = ClassSchemaMap.getInstance().getTypeInfo(CollectionTestClass.class);
        nodeBuilder = new _NodeBuilder(null);
        _ObjectNode objectNode = nodeBuilder.makeNode(classSchema);

        System.out.println(objectNode);
    }





}