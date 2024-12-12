package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
        ClassSchema classSchema = ClassSchemaMap.getInstance().getClassSchema(TestClass.class);
        _ObjectNode rootObjectNode = nodeBuilder.makeNode(classSchema);
        assertNotNull(rootObjectNode);
        _ObjectNode aNode = (_ObjectNode) rootObjectNode.getNode("a");
        assertNotNull(aNode);
        assertTrue(aNode.isEndPoint());
        _ObjectNode kNode = rootObjectNode.getObjectNode("b").getObjectNode("obj").getObjectNode("k");
        List<Integer> idList = kNode.getFieldSchemedPointerList().stream().map(_SchemaPointer::getParentId).collect(Collectors.toList());
        assertNotNull(kNode);
        assertTrue(kNode.isEndPoint());
        assertEquals(2, kNode.getFieldSchemedPointerList().size());

        _SchemaPointer schemaPointer = rootObjectNode.getNodeSchemaPointerList().get(0);
        int rootID = schemaPointer.getId();
        _ObjectNode objNode = rootObjectNode.getObjectNode("b").getObjectNode("obj");
        int objID = objNode.getNodeSchemaPointerList().get(0).getId();
        int parentID = objNode.getNodeSchemaPointerList().get(0).getParentId();

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
            ClassSchema classSchema = ClassSchemaMap.getInstance().getClassSchema(ConflictParentClass.class);
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


        //List<Set<ItemClassTest>> list;

        Set<String> stringSet;



        //@CSONValue("stringSet[0]")
        //String value;

    }

    @Test
    @DisplayName("컬렉션 노드 테스트")
    void testForCollectionNode() {
        ClassSchema classSchema = ClassSchemaMap.getInstance().getClassSchema(CollectionTestClass.class);
        nodeBuilder = new _NodeBuilder(null);
        _ObjectNode objectNode = nodeBuilder.makeNode(classSchema);

        _CollectionNode collectionNode = (_CollectionNode) objectNode.getNode("stringSet");
        assertNotNull(collectionNode);



        System.out.println(objectNode);
    }








}