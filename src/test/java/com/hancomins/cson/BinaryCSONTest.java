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
    public void testObjectInObject1() {
        CSONObject csonObject = new CSONObject();
        CSONObject innerObject = new CSONObject();
        csonObject.put("inner", innerObject);
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);
    }

}
