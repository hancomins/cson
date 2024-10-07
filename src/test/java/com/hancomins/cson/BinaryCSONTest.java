package com.hancomins.cson;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryCSONTest {
    @Test
    @DisplayName("CSONOBject")
    public void testSimpleKeyValue() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("key", "value");
        csonObject.put("key2", 123L);
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);
    }

    @Test
    public void testObjectInEmptyObject() {
        CSONObject csonObject = new CSONObject();
        CSONObject innerObject = new CSONObject();
        csonObject.put("inner", innerObject);
        CSONObject originCsonObject = csonObject.clone();
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(originCsonObject, readObject);
        assertEquals(csonObject, readObject);
    }

    @Test
    public void testObjectInObject() {
        CSONObject csonObject = new CSONObject();
        CSONObject innerObject = new CSONObject();
        innerObject.put("key", "value");
        csonObject.put("inner", innerObject);
        innerObject.put("key2", 123L);
        innerObject.put("key3", new CSONObject());
        innerObject.put("key4", new CSONObject().put("key", "value").put("key2", 123L));
        csonObject.put("key2", 123L);

        CSONObject originCsonObject = csonObject.clone();
        System.out.println(csonObject);
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(originCsonObject, readObject);
        assertEquals(csonObject, readObject);
    }

}
