package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ObjectSchemaContainerTest {



    public static class TestClass {
        String a = "a";

        TestClass2 b;

        @CSONValue("b.obj")
        TestClassC c;

        @CSONValue("b.ab")
        BigInteger d;

        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

    }

    public static class TestClassC {

        @CSONValue("k")
        String a = "a";

        @CSONValue("inner.k")
        String k = "aaa";

    }


    public static class TestClass2 {

        @CSONValue("obj.k")
        public String k = "";

        public int a = 0;
        public String ab = "0";
    }

    @Test
    @DisplayName("Enum test")
    void enumTest() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("dayOfWeek", "TUESDAY");

        TestClass testClass = new TestClass();
        ObjectMapper ObjectMapper = new ObjectMapper();

        ObjectMapper.toObject(csonObject.toString(), testClass);

        assertEquals(testClass.dayOfWeek, DayOfWeek.TUESDAY);
    }



    @Test
    @DisplayName("기본 노드 생성 테스트")
    void putTestDefault() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("k", "aaa");
        csonObject.put("$.inner.k", "bbb");
        ObjectMapper ObjectMapper = new ObjectMapper();
        TestClassC testClassC = ObjectMapper.toObject(csonObject.toString(),new TestClassC());
        assertEquals(testClassC.a, "aaa");
        assertEquals(testClassC.k, "bbb");

    }

    @Test
    void putTest() {


        CSONObject csonObject = new CSONObject();
        csonObject.put("a", "aaa");
        //csonObject.put("$.a.b", 10000);
        csonObject.put("$.b.a", "10000");
        csonObject.put("$.b.ab", 10000);
        csonObject.put("$.b.obj.k", "k");

        System.out.println(csonObject);

        TestClass testClass = new TestClass();
        ObjectMapper ObjectMapper = new ObjectMapper();

        ObjectMapper.toObject(csonObject.toString(), testClass);

        assertEquals(testClass.a, "aaa");
        assertEquals(testClass.b.a, 10000);
        assertEquals(testClass.b.ab, "10000");


        assertNotNull(testClass.d);
        assertEquals(testClass.d.toString(), "10000");

        assertNotNull(testClass.c);
        assertEquals(testClass.b.k, "k");
        assertEquals("k", testClass.c.a);





    }


    public static class DefaultCollectionClass {
        //List<String> stringList;

        List<Set<String>> stringSetList;

        @CSONValue("stringSetList")
        List<ArrayDeque<String>> arrayDequeList;

        List<List<List<Integer>>> integerList;
    }

    @Test
    @DisplayName("컬렉션 노드 생성 테스트")
    void putTestCollection() {
        CSONObject csonObject = new CSONObject();
        /*csonObject.put("$.stringList[0]", "aaa");
        csonObject.put("$.stringList[1]", "bbb");
        csonObject.put("$.stringList[2]", "ccc");*/

        csonObject.put("$.stringSetList[0][0]", "aaa");
        csonObject.put("$.stringSetList[0][1]", "bbb");
        csonObject.put("$.stringSetList[0][2]", "ccc");
        csonObject.put("$.stringSetList[1][0]", "aaa1");
        csonObject.put("$.stringSetList[1][1]", "bbb1");
        csonObject.put("$.stringSetList[1][2]", "ccc1");

        csonObject.put("$.integerList[0][0][0]", "1");
        csonObject.put("$.integerList[0][0][1]", "2");
        csonObject.put("$.integerList[0][0][2]", "3");
        csonObject.put("$.integerList[0][1][0]", "4");
        csonObject.put("$.integerList[0][1][1]", "5");
        csonObject.put("$.integerList[0][1][2]", "6");
        csonObject.put("$.integerList[0][2][0]", "7");
        csonObject.put("$.integerList[0][2][1]", "8");
        csonObject.put("$.integerList[0][2][2]", "9");
        csonObject.put("$.integerList[1][0][0]", "1");
        csonObject.put("$.integerList[1][0][1]", "2");
        csonObject.put("$.integerList[1][0][2]", "3");
        csonObject.put("$.integerList[1][1][0]", "4");

        csonObject.put("$.emptyList[1][1][0]", "4");






        System.out.println(csonObject);

        DefaultCollectionClass defaultCollectionClass = new DefaultCollectionClass();
        ObjectMapper ObjectMapper = new ObjectMapper();

        ObjectMapper.toObject(csonObject.toString(), defaultCollectionClass);

        /*assertNotNull(defaultCollectionClass.stringList);
        assertEquals(defaultCollectionClass.stringList.size(), 3);
        assertEquals(defaultCollectionClass.stringList.get(0), "aaa");
        assertEquals(defaultCollectionClass.stringList.get(1), "bbb");
        assertEquals(defaultCollectionClass.stringList.get(2), "ccc");*/

        assertEquals(defaultCollectionClass.stringSetList.size(), 2);
        assertNotNull(defaultCollectionClass.stringSetList.get(0));
        assertNotNull(defaultCollectionClass.stringSetList.get(1));
        assertEquals(defaultCollectionClass.stringSetList.get(0).size(), 3);
        assertEquals(defaultCollectionClass.stringSetList.get(1).size(), 3);
        assertTrue(defaultCollectionClass.stringSetList.get(0).contains("aaa"));
        assertTrue(defaultCollectionClass.stringSetList.get(0).contains("bbb"));
        assertTrue(defaultCollectionClass.stringSetList.get(0).contains("ccc"));
        assertTrue(defaultCollectionClass.stringSetList.get(1).contains("aaa1"));
        assertTrue(defaultCollectionClass.stringSetList.get(1).contains("bbb1"));
        assertTrue(defaultCollectionClass.stringSetList.get(1).contains("ccc1"));

        assertNotNull(defaultCollectionClass.arrayDequeList);
        assertEquals(defaultCollectionClass.arrayDequeList.size(), 2);
        assertNotNull(defaultCollectionClass.arrayDequeList.get(0));
        assertNotNull(defaultCollectionClass.arrayDequeList.get(1));
        assertEquals(defaultCollectionClass.arrayDequeList.get(0).size(), 3);
        assertEquals(defaultCollectionClass.arrayDequeList.get(1).size(), 3);
        assertEquals(defaultCollectionClass.arrayDequeList.get(0).poll(), "aaa");
        assertEquals(defaultCollectionClass.arrayDequeList.get(0).poll(), "bbb");
        assertEquals(defaultCollectionClass.arrayDequeList.get(0).poll(), "ccc");
        assertEquals(defaultCollectionClass.arrayDequeList.get(1).poll(), "aaa1");
        assertEquals(defaultCollectionClass.arrayDequeList.get(1).poll(), "bbb1");
        assertEquals(defaultCollectionClass.arrayDequeList.get(1).poll(), "ccc1");

        assertNotNull(defaultCollectionClass.integerList);
        assertEquals(defaultCollectionClass.integerList.size(), 2);
        assertNotNull(defaultCollectionClass.integerList.get(0));
        assertNotNull(defaultCollectionClass.integerList.get(1));
        assertEquals(defaultCollectionClass.integerList.get(0).size(), 3);
        assertEquals(defaultCollectionClass.integerList.get(1).size(), 2);
        assertEquals(defaultCollectionClass.integerList.get(0).get(0).size(), 3);
        assertEquals(defaultCollectionClass.integerList.get(0).get(1).size(), 3);
        assertEquals(defaultCollectionClass.integerList.get(0).get(2).size(), 3);
        assertEquals(defaultCollectionClass.integerList.get(1).get(0).size(), 3);
        assertEquals(defaultCollectionClass.integerList.get(1).get(1).size(), 1);
        assertEquals(defaultCollectionClass.integerList.get(0).get(0).get(0), 1);
        assertEquals(defaultCollectionClass.integerList.get(0).get(0).get(1), 2);
        assertEquals(defaultCollectionClass.integerList.get(0).get(0).get(2), 3);
        assertEquals(defaultCollectionClass.integerList.get(0).get(1).get(0), 4);
        assertEquals(defaultCollectionClass.integerList.get(0).get(1).get(1), 5);
        assertEquals(defaultCollectionClass.integerList.get(0).get(1).get(2), 6);
        assertEquals(defaultCollectionClass.integerList.get(0).get(2).get(0), 7);
        assertEquals(defaultCollectionClass.integerList.get(0).get(2).get(1), 8);
        assertEquals(defaultCollectionClass.integerList.get(0).get(2).get(2), 9);
        assertEquals(defaultCollectionClass.integerList.get(1).get(0).get(0), 1);
        assertEquals(defaultCollectionClass.integerList.get(1).get(0).get(1), 2);
        assertEquals(defaultCollectionClass.integerList.get(1).get(0).get(2), 3);


    }

    public static class ObjectInCollectionClass {
        List<TestClassC> testClassCList;
    }




    @Test
    @DisplayName("컬렉션 내 객체 생성 테스트")
    void putTestObjectInCollection() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("$.testClassCList[0].k", "aaa");
        csonObject.put("$.testClassCList[0].inner.k", "bbb");
        csonObject.put("$.testClassCList[1].k", "aaa1");
        csonObject.put("$.testClassCList[1].inner.k", "bbb1");

        System.out.println(csonObject);

        ObjectInCollectionClass objectInCollectionClass = new ObjectInCollectionClass();
        ObjectMapper ObjectMapper = new ObjectMapper();

        ObjectMapper.toObject(csonObject.toString(), objectInCollectionClass);

        assertNotNull(objectInCollectionClass.testClassCList);
        assertEquals(objectInCollectionClass.testClassCList.size(), 2);
        assertEquals(objectInCollectionClass.testClassCList.get(0).a, "aaa");
        assertEquals(objectInCollectionClass.testClassCList.get(0).k, "bbb");
        assertEquals(objectInCollectionClass.testClassCList.get(1).a, "aaa1");
        assertEquals(objectInCollectionClass.testClassCList.get(1).k, "bbb1");
    }


    public static class MapTestClass {


        @CSONValue("stringsMap.c")
        String c = "";

        Map<String, String> stringsMap;

        @CSONValue("stringsMap")
        Map<String, Double> doubleMap;
    }

    @Test
    @DisplayName("맵 노드 생성 테스트")
    public void mapTest() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("$.stringsMap.a", "1000");
        csonObject.put("$.stringsMap.b", "2000");
        csonObject.put("$.stringsMap.c", "3000");

        System.out.println(csonObject);

        MapTestClass mapTestClass = new MapTestClass();
        ObjectMapper ObjectMapper = new ObjectMapper();

        ObjectMapper.toObject(csonObject.toString(), mapTestClass);


        assertNotNull(mapTestClass.stringsMap);
        assertEquals(3, mapTestClass.stringsMap.size());
        assertEquals("1000", mapTestClass.stringsMap.get("a"));
        assertEquals("2000", mapTestClass.stringsMap.get("b"));
        assertEquals("3000", mapTestClass.stringsMap.get("c"));

        assertEquals("3000", mapTestClass.c);




        assertEquals(mapTestClass.doubleMap.size(), 3);
        assertEquals(mapTestClass.doubleMap.get("a"), Double.valueOf(1000));
        assertEquals(mapTestClass.doubleMap.get("b"), Double.valueOf(2000));
        assertEquals(mapTestClass.doubleMap.get("c"), Double.valueOf(3000));

    }

    public static class GetterSetterTestClass {

        String a;
        int intA;
        Collection<Integer> collectionB;
        Map<String, String> map;
        ArrayList<List<TestClass>> list;

        @CSONValueSetter("obj.a")
        public void setA(String a) {
            this.a = a;
        }

        @CSONValueSetter("obj.a")
        public void setIntA(int a) {
            this.intA = a;
        }

        @CSONValueSetter("obj.b")
        public void setCollectionB(Collection<Integer> collection) {
            assertEquals(3, collection.size());
            this.collectionB = collection;
        }

        // 중첩 컬렉션
        @CSONValueSetter("array.b.c")
        public void setCollectionC(Collection<List<TestClass>> collection) {
            ArrayList<List<TestClass>> list = new ArrayList<>(collection);

            assertEquals(2, list.size());
            assertEquals(2, list.get(0).size());
            assertEquals(2, list.get(1).size());
            assertEquals("1000", list.get(0).get(0).a);
            assertEquals("2000", list.get(0).get(1).a);
            assertEquals("3000", list.get(1).get(0).a);
            assertEquals("4000", list.get(1).get(1).a);
            assertEquals("dsafasdf", list.get(0).get(0).c.a);
            assertEquals("dsafasdf", list.get(0).get(0).c.k);
            assertEquals("dsafasdf", list.get(0).get(0).c.k);
            this.list = list;
        }


        @CSONValueSetter("obj.test")
        public void setTestObject(TestClass testObject) {
            assertEquals("2000", testObject.a);
            assertEquals("dsafasdf", testObject.c.a);
        }

        @CSONValueSetter("obj.test.map")
        public void setTestMap(Map<String, String> map) {
            this.map = map;
            assertEquals(2, map.size());
            assertEquals("1000", map.get("a"));
            assertEquals("2000", map.get("b"));
        }


    }

    @Test
    @DisplayName("Getter Setter 테스트")
    public void getterSetterTest() {

        CSONObject csonObject = new CSONObject();
        csonObject.put("$.obj.a", "1000");
        csonObject.put("$.obj.test.a", "2000");
        csonObject.put("$.obj.test.b.obj.k", "dsafasdf");

        csonObject.put("$.obj.b[0]", "1000");
        csonObject.put("$.obj.b[1]", "2000");
        csonObject.put("$.obj.b[2]", "33333");

        csonObject.put("$.obj.test.map.a", "1000");
        csonObject.put("$.obj.test.map.b", "2000");

        csonObject.put("$.array.b.c[0][0].a", "1000");
        csonObject.put("$.array.b.c[0][1].a", "2000");
        csonObject.put("$.array.b.c[1][0].a", "3000");
        csonObject.put("$.array.b.c[1][1].a", "4000");

        csonObject.put("$.array.b.c[0][0].b.obj.k", "dsafasdf");
        csonObject.put("$.array.b.c[0][0].b.obj.inner.k", "dsafasdf");
        csonObject.put("$.array.b.c[1][1].b.obj.k", "dsafasdf");
        csonObject.put("$.array.b.c[1][1].b.obj.inner.k", "dsafasdf");

        GetterSetterTestClass getterSetterTestClass = new GetterSetterTestClass();
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.toObject(csonObject.toString(), getterSetterTestClass);


        assertEquals("1000", getterSetterTestClass.a);
        assertEquals(1000, getterSetterTestClass.intA);

        assertNotNull(getterSetterTestClass.collectionB);
        assertEquals(3, getterSetterTestClass.collectionB.size());
        assertTrue(getterSetterTestClass.collectionB.contains(1000));
        assertTrue(getterSetterTestClass.collectionB.contains(2000));
        assertTrue(getterSetterTestClass.collectionB.contains(33333));

        assertNotNull(getterSetterTestClass.map);
        assertNotNull(getterSetterTestClass.list);
    }

    public static class ComplexMapTestClass {
        Map<String, Map<String, String>> map;
    }


    @Test
    @DisplayName("복합 맵 테스트")
    public void complexMapTest() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("$.map.a.a", "1000");
        csonObject.put("$.map.a.b", "2000");
        csonObject.put("$.map.b.a", "3000");
        csonObject.put("$.map.b.b", "4000");

        ComplexMapTestClass complexMapTestClass = new ComplexMapTestClass();
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.toObject(csonObject.toString(), complexMapTestClass);

        assertNotNull(complexMapTestClass.map);
        assertEquals(2, complexMapTestClass.map.size());
        assertNotNull(complexMapTestClass.map.get("a"));
        assertNotNull(complexMapTestClass.map.get("b"));
        Object obj =  complexMapTestClass.map.get("a");
        assertEquals(2, complexMapTestClass.map.get("a").size());
        assertEquals(2, complexMapTestClass.map.get("b").size());
        assertEquals("1000", complexMapTestClass.map.get("a").get("a"));
        assertEquals("2000", complexMapTestClass.map.get("a").get("b"));
        assertEquals("3000", complexMapTestClass.map.get("b").get("a"));
        assertEquals("4000", complexMapTestClass.map.get("b").get("b"));
    }








    // todo : 타입이 다른 경우 테스트 추가



}