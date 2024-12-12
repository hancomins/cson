package com.hancomins.cson.serializer;

import com.hancomins.cson.serializer.mapper.CSON;
import com.hancomins.cson.serializer.mapper.CSONValue;
import org.junit.jupiter.api.Test;

class ObjectTypeAnalyzerTest {


     @CSON(explicit = true)
    public static class User {
        @CSONValue
        private String name;
        @CSONValue
        private int age;
        @CSONValue("detail.isAdult")
        private boolean isAdult;
        @CSONValue("detail.isAdult")
        private double height;
        @CSONValue("detail.weight")
        private float weight;
    }

    @Test
    public void test() {
            User user = new User();
        user.name = "John";
        user.age = 30;
        user.isAdult = true;
        user.height = 180.5;
        user.weight = 70.5f;

        Class<?> clazz = User.class;

        CSONSerializer.toCSONObject(user);




    }

}