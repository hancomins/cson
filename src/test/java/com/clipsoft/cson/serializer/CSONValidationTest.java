package com.clipsoft.cson.serializer;

import com.clipsoft.cson.CSONArray;
import com.clipsoft.cson.CSONObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public class CSONValidationTest {
    public static class Data {
        @CSONValidation(min = -1, max = 10, pattern = "^[a-zA-Z].*", required = false)
        private String name;
        @CSONValidation(notNull = true)
        private int age;
        private String email;
        private String phone;
        @CSONValidation(required = true)
        private String address;
        private String address2;

        @CSONValidation(min = 2)
        private List<String> collection = null;
    }

    @Test
    public void testCSONValidation() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("name", "John");
        csonObject.put("age", 20);
        csonObject.put("email", "test@gmail.com");
        csonObject.put("phone", "010-1234-5678");
        csonObject.put("address", "Seoul, Korea");
        csonObject.put("address2", "Gangnam-gu");

        Data data = csonObject.toObject(Data.class);
        System.out.println(data);

        try {
            csonObject.put("name", "");
            data = csonObject.toObject(Data.class);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }

        csonObject.put("name", "John");
        csonObject.put("address", null);
        try {
            data = csonObject.toObject(Data.class);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }
        csonObject.put("address", "Seoul, Korea");
        csonObject.put("age", null);
        try {
            data = csonObject.toObject(Data.class);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }

        csonObject.put("age", 20);
        csonObject.put("collection", new CSONArray().put("1", "2", "3"));
        data = csonObject.toObject(Data.class);
        System.out.println(data.collection);

        csonObject.put("collection", new CSONArray().put("1"));
        try {
            data = csonObject.toObject(Data.class);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            fail("Should throw IllegalArgumentException");
        }


    }

}
