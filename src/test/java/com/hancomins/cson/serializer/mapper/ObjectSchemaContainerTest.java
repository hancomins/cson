package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONObject;
import com.hancomins.cson.serializer.CSON;
import com.hancomins.cson.serializer.CSONValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectSchemaContainerTest {

    public static class TestClass {
        String a = "a";

        TestClass2 b;

        @CSONValue("b.obj")
        TestClassC c;
    }

    public static class TestClassC {

        @CSONValue("k")
        String a = "a";

        @CSONValue("inner.k")
        String k = "aaa";

    }


    public static class TestClass2 {
         //@CSONValue("a")
         //public int a = 200;

        //@CSONValue("a")
        //public String ab = "";

        @CSONValue("obj.k")
        public String k = "";
    }




    @Test
    void test() {
        ObjectSchemaContainer container = new ObjectSchemaContainer(TestClass.class);

        Object valule = container.get("a");

        assertEquals(valule, "a");


        

        Object valule2 = container.get("b");


    }

    @Test
    void putTestDefault() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("k", "aaa");
        csonObject.put("$.inner.k", "bbb");
        CSONM csonm = new CSONM();
        TestClassC testClassC = csonm.toObject(csonObject.toString(),new TestClassC());
        assertEquals(testClassC.a, "aaa");
        assertEquals(testClassC.k, "bbb");




    }

    @Test
    void putTest() {


        CSONObject csonObject = new CSONObject();
        csonObject.put("a", "aaa");
        //csonObject.put("$.a.b", 10000);
        csonObject.put("$.b.a", 10000);
        csonObject.put("$.b.obj.k", "k");

        System.out.println(csonObject.toString());

        TestClass testClass = new TestClass();
        CSONM csonm = new CSONM();

        csonm.toObject(csonObject.toString(), testClass);


        assertEquals(testClass.a, "aaa");
        //assertEquals(testClass.b.a, 10000);
        //assertEquals(testClass.b.ab, "10000");


        assertEquals(testClass.b.k, "k");
        assertNotNull(testClass.c);
        assertEquals("k", testClass.c.a);







    }

}