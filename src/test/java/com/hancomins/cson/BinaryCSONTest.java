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
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);




    }
}
