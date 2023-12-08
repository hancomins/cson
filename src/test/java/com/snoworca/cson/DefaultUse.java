package com.snoworca.cson;


import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class DefaultUse {

    @Test
    public void toBinaryArrayTest() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("number", 1000).put("bigNumber", new BigDecimal(10000000000L));

        byte[] bytes = csonObject.toBytes();

        CSONObject parsedCsonObject = new CSONObject(bytes);
        assertEquals(csonObject.get("number"), parsedCsonObject.get("number"));
        assertEquals(1000, parsedCsonObject.get("number"));

        assertEquals(10000000000L, parsedCsonObject.getLong("bigNumber"));





    }
}
