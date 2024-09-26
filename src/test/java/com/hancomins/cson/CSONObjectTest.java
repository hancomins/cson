package com.hancomins.cson;


import com.hancomins.cson.options.JsonParsingOptions;
import com.hancomins.cson.options.JsonWritingOptions;
import com.hancomins.cson.options.ParsingOptions;
import com.hancomins.cson.options.WritingOptions;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("CSONObjectTest (성공)")
public class CSONObjectTest {

    private CSONObject makeCSOObject() {
        Random random = new Random(System.currentTimeMillis());
        byte[] randomBuffer = new byte[random.nextInt(64) +2];
        random.nextBytes(randomBuffer);
        CSONObject csonObject = new CSONObject();
        csonObject.put("1", 1);
        csonObject.put("1.1", 1.1f);
        csonObject.put("2.2", 2.2);
        csonObject.put("333333L", 333333L);
        csonObject.put("boolean", true);
        csonObject.put("char", 'c');
        csonObject.put("short", (short)32000);
        csonObject.put("byte", (byte)128);
        csonObject.put("null", null);
        csonObject.put("string", "stri \" \n\rng");
        csonObject.put("this", csonObject);
        csonObject.put("byte[]", randomBuffer);
        CSONArray csonArray = new CSONArray();
        csonArray.add(1);
        csonArray.add(1.1f);
        csonArray.add((double)2.2);
        csonArray.put(333333L);
        csonArray.put(true);
        csonArray.put('c');
        csonArray.add((short)32000);
        csonArray.add((byte)128);
        csonArray.add(null);
        csonArray.add("stri \" \n\rng");
        csonArray.add(csonArray);
        csonArray.add(csonObject.clone());
        csonArray.add(randomBuffer);
        csonObject.put("array", csonArray);
        csonObject.put("array2", new CSONArray().put(new CSONArray().put(1).put(2)).put(new CSONArray().put(3).put(4)).put(new CSONArray()).put(new CSONObject()));
        csonObject.put("array3", new CSONArray().put("").put(new CSONArray().put(3).put(4)).put(new CSONArray()).put(new CSONObject()));
        csonObject.put("array4", new CSONArray().put(new CSONObject()).put(new CSONObject()).put(new CSONArray()).put(new CSONObject().put("inArray",new CSONArray())));
        csonObject.put("key111", new CSONObject().put("1", new CSONObject()));
        csonObject.put("key112", new CSONArray().put(new CSONObject()));



        return csonObject;
    }

    @Test
    public void cloneAndEqualsTest() throws  Exception {


        JSONObject jsonObjectX = new JSONObject("{char:'c'}");




        JSONObject jsonObject = new JSONObject("{\"key\": \"va \\\" \\n \\r lue\"}");

        CSONObject csonObjectA = new CSONObject("{\"key\": \"va \\\" \\n \\r lue\"}");
        System.out.println(csonObjectA.toString());
        JSONObject jsonObjectA = new JSONObject(csonObjectA.toString(JsonWritingOptions.json()));
        new CSONObject(csonObjectA.toString(), JsonWritingOptions.json());

        System.out.println("--------------------------------------------------");

        CSONObject csonObject = makeCSOObject();
        CSONObject csonObject2 = csonObject.clone();
        assertEquals(csonObject, csonObject2);
        assertEquals(csonObject.toString(), csonObject2.toString());

        System.out.println(csonObject.toString());
        JSONObject jsonObject1 = new JSONObject(csonObject.toString(JsonWritingOptions.json()));
        assertEquals(csonObject2,new CSONObject(csonObject.toString(JsonWritingOptions.json())));

        JsonWritingOptions JsonWritingOptions = com.hancomins.cson.options.JsonWritingOptions.json().setPretty(true);


        assertEquals(csonObject2.toString(JsonWritingOptions),new CSONObject(csonObject.toString(JsonWritingOptions)).toString(JsonWritingOptions));
        assertEquals(csonObject2,new CSONObject(csonObject.toBytes()));
    }

    @Test
    public void toCsonAndParseTest() {

        CSONObject csonObject = makeCSOObject();

        byte[] buffer = csonObject.getByteArray("byte[]");
        byte[] cson = csonObject.toCSONBinary();



        CSONObject compareCSONObject = new CSONObject(cson);


        assertEquals(1, compareCSONObject.get("1"));
        assertEquals(1.1f, (float)compareCSONObject.get("1.1"), 0.0001f);
        assertEquals(2.2, (double)compareCSONObject.get("2.2"), 0.0001);
        assertEquals(333333L, compareCSONObject.get("333333L"));
        assertEquals(true, compareCSONObject.get("boolean"));
        assertEquals('c', compareCSONObject.get("char"));
        assertEquals(null, compareCSONObject.get("null"));

        assertEquals((short)32000, compareCSONObject.getShort("short"));
        assertEquals((byte)128, compareCSONObject.getByte("byte"));
        assertEquals("stri \" \n\rng", compareCSONObject.getString("string"));


        String aa = compareCSONObject.getString("byte[]");

        assertArrayEquals(buffer, compareCSONObject.getByteArray("byte[]"));

        CSONArray csonArray = compareCSONObject.getCSONArray("array");
        assertEquals(1, csonArray.get(0));
        assertEquals(1.1f, (float) csonArray.get(1), 0.00001f);
        assertEquals(2.2, (double) csonArray.get(2), 0.00001);
        assertEquals(333333L, csonArray.get(3));
        assertEquals(true, csonArray.get(4));
        assertEquals('c', csonArray.get(5));
        assertEquals((short)32000, csonArray.get(6));
        assertEquals((byte)128, csonArray.get(7));
        assertEquals(null, csonArray.get(8));
        assertEquals("stri \" \n\rng", csonArray.get(9));
        assertArrayEquals("stri \" \n\rng".getBytes(StandardCharsets.UTF_8), csonArray.optByteArray(9));
        assertTrue(csonArray.get(10) instanceof CSONArray);
        assertTrue(csonArray.get(11) instanceof CSONObject);
        assertArrayEquals(buffer, (byte[])csonArray.get(12));


    }


    @Test
    @DisplayName("비어있는 CSONObject 와 CSONArray 파싱 테스트")
    public void emptyCSONObjectAndArrayTest() {

        CSONObject csonObject = new CSONObject("{}", JsonWritingOptions.json());
        CSONArray csonArray = new CSONArray("[]", JsonWritingOptions.json());

        assertEquals(0, csonObject.size());
        assertEquals(0, csonArray.size());

        CSONObject complexCSONObject = new CSONObject("{\"emptyObject\":{},\"emptyArray\":[]}");
        assertEquals(2, complexCSONObject.size());
        assertEquals(0, complexCSONObject.getCSONObject("emptyObject").size());
        assertEquals(0, complexCSONObject.getCSONArray("emptyArray").size());

        System.out.println(complexCSONObject);

        assertEquals("{\"emptyObject\":{},\"emptyArray\":[]}", complexCSONObject.toString());






    }


    @Test
    public void toJsonAndParseTest() {


        CSONObject csonObject = makeCSOObject();

        byte[] buffer = csonObject.getByteArray("byte[]");
        byte[] bufferOrigin = buffer;
        String jsonString = csonObject.toString(JsonWritingOptions.json());



        System.out.println(csonObject.get("string"));


        System.out.println(jsonString);
        String bufferBase64 = "base64," + Base64.getEncoder().encodeToString(buffer);
        CSONObject compareCSONObject = new CSONObject(jsonString, JsonWritingOptions.json());


        assertEquals(1, compareCSONObject.getInt("1"));
        assertEquals(1.1f, compareCSONObject.getFloat("1.1"), 0.0001f);
        assertEquals(2.2, compareCSONObject.getDouble("2.2"), 0.0001);
        assertEquals(333333L, compareCSONObject.getLong("333333L"));
        assertEquals(true, compareCSONObject.getBoolean("boolean"));
        assertEquals('c', compareCSONObject.getChar("char"));
        assertEquals((short)32000, compareCSONObject.getShort("short"));
        assertEquals((byte)128, compareCSONObject.getByte("byte"));
        assertEquals("stri \" \n\rng", compareCSONObject.getString("string"));


        assertEquals(bufferBase64, compareCSONObject.getString("byte[]"));
        assertArrayEquals(bufferOrigin, compareCSONObject.getByteArray("byte[]"));

        CSONArray csonArray = compareCSONObject.getCSONArray("array");
        assertEquals(1, csonArray.get(0));
        assertEquals(1.1f, csonArray.getFloat(1), 0.00001f);
        assertEquals(2.2, csonArray.getDouble(2), 0.00001);
        assertEquals(333333L, csonArray.getLong(3));
        assertEquals(true, csonArray.getBoolean(4));
        assertEquals('c', csonArray.getChar(5));
        assertEquals(32000, csonArray.getShort(6));
        assertEquals((byte)128, csonArray.getByte(7));
        assertEquals(null, csonArray.getString(8));
        assertEquals("stri \" \n\rng", csonArray.getString(9));
        assertArrayEquals("stri \" \n\rng".getBytes(StandardCharsets.UTF_8), csonArray.optByteArray(9));
        assertTrue(csonArray.get(10) instanceof CSONArray);
        assertTrue(csonArray.get(11) instanceof CSONObject);
        assertArrayEquals(buffer, csonArray.getByteArray(12));

        System.out.println("--------------------------------------------------");
        System.out.println(jsonString);

        CSONObject csonObject2 = new CSONObject(jsonString);

    }

    @Test
    public void csonArrayToStringTest() {

    }
}
