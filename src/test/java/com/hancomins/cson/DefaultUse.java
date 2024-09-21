package com.hancomins.cson;


import com.hancomins.cson.options.ParsingOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;


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

    @Test
    public void escapeSequenceTest() {
        ParsingOption<?> parsingOption =  CSONObject.getDefaultStringFormatOption();
        CSONObject.setDefaultStringFormatOption(ParsingOption.json());
        CSONObject csonObject = new CSONObject(ParsingOption.json());
        csonObject.put("string", "Hello\\nWorld");
        csonObject.put("string2", "Hello\\World");
        CSONArray csonArray = new CSONArray();
        csonArray.add("Hello\\nWorld");
        csonArray.add("Hello\\Wor\"ld");
        csonObject.put("array", csonArray);
        String jsonString = csonObject.toString();
        System.out.println(jsonString);
        CSONObject csonObjetPure = new CSONObject(jsonString);
        CSONObject csonObjectJson = new CSONObject(jsonString, ParsingOption.json());
        assertEquals("Hello\\World",csonObjectJson.get("string2"));
        assertEquals(jsonString, csonObjetPure.toString());
        assertEquals(jsonString, csonObjectJson.toString(ParsingOption.json().setUnprettyArray(true)));
        csonObjectJson.put("string3", "Hello/World");
        csonObjectJson = new CSONObject(csonObjectJson.toString());
        assertEquals("Hello/World", csonObjectJson.get("string3"));


        csonObject = new CSONObject();
        csonObject.put("st\"ring'4", "Hello\"World");
        System.out.println(csonObject.toString(ParsingOption.json5().setValueQuote("\"")));

        CSONObject json5 = new CSONObject(csonObject.toString(ParsingOption.json5()), ParsingOption.json5());
        assertEquals("Hello\"World", json5.get("st\"ring'4"));

        CSONObject.setDefaultStringFormatOption(parsingOption);



    }
}
