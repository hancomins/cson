package com.hancomins.cson.serializer;

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
    }

    @Test
    void test() {
        ObjectSchemaContainer container = new ObjectSchemaContainer(TestClass.class);

        Object valule = container.get("a");
        assertEquals(valule, "a");


    }

}