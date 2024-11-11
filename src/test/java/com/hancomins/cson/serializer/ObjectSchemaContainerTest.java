package com.hancomins.cson.serializer;

import com.hancomins.cson.CSONObject;
import com.hancomins.cson.format.KeyValueDataContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectSchemaContainerTest {

     @CSON
    public static class TestClass {
        String a = "a";

        TestClass2 b = new TestClass2();
    }

     @CSON
    public static class TestClass2 {
        public int a = 200;

        @CSONValue("a")
        public String ab = "";

        /*@CSONValue("obj.k")
        public String k = "";*/
    }

    @Test
    void test() {
        ObjectSchemaContainer container = new ObjectSchemaContainer(TestClass.class);

        Object valule = container.get("a");

        assertEquals(valule, "a");


        

        Object valule2 = container.get("b");


    }

    @Test
    void putTest() {


        CSONObject csonObject = new CSONObject();
        csonObject.put("a", "aaa");
        //csonObject.put("$.a.b", 10000);
        csonObject.put("$.b.a", 10000);
        //csonObject.put("$.b.obj.k", "k");

        System.out.println(csonObject.toString());

        TestClass testClass = new TestClass();
        CSONM csonm = new CSONM();

        csonm.toObject(csonObject.toString(), testClass);


        assertEquals(testClass.a, "aaa");
        assertEquals(testClass.b.a, 10000);
        assertEquals(testClass.b.ab, "10000");

        //assertEquals(testClass.b.k, "k");








    }

}