package com.clipsoft.cson;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class CSONPathTest {
    CSONObject json;
    CSONPath jsonPath;

    @Before
    public void setUp() {
        json = new CSONObject();
        json.put("a", 1);
        CSONObject b = new CSONObject();
        json.put("b",b);
        b.put("c", "true");
        b.put("d", Math.PI);
        CSONObject e = new CSONObject();
        b.put("e", e);
        e.put("f", "4");
        e.put("g", 5.123132);
        CSONArray h = new CSONArray();
        e.put("h", h);
        h.put( 6);
        h.put( 7);
        h.put( 8);
        h.put( 9.1234);
        CSONObject i = new CSONObject();
        h.put(i);
        i.put("j", "10");
        i.put("k", "11");
        i.put("l", "12");

        jsonPath = new CSONPath(json);


    }

    @Test
    public void optBoolean() {
        assertEquals(true, jsonPath.optBoolean("a"));
        assertEquals(true, jsonPath.optBoolean("b.c"));
    }

    @Test
    public  void optDouble() {

        assertEquals(Double.valueOf(9.1234),jsonPath.optDouble("b.e.h[3]"));
        assertEquals(Double.valueOf(5.123132),  jsonPath.optDouble("b.e.g"));
        assertEquals(Double.valueOf(4.0), jsonPath.optDouble("b.e.f"));
        assertEquals(Double.valueOf(1.0), jsonPath.optDouble("a"));
        assertEquals(Double.valueOf(Math.PI), jsonPath.optDouble("b.d"));

    }

    @Test
    public  void optFloat() {



        assertEquals(Float.valueOf(9.1234f),jsonPath.optFloat("b.e.h[3]"));
        assertEquals(Float.valueOf(5.123132f), jsonPath.optFloat("b.e.g"));
        assertEquals(Float.valueOf(4.0f), jsonPath.optFloat("b.e.f"));
        assertEquals(Float.valueOf(1.0f), jsonPath.optFloat("a"));
        assertEquals(Float.valueOf((float)Math.PI), jsonPath.optFloat("b.d"));
        assertEquals(Float.valueOf(10.0f), jsonPath.optFloat("b.e.h[4].j"));
        assertEquals(Float.valueOf(11.0f), jsonPath.optFloat("b.e.h[4].k"));


        assertEquals(Float.valueOf(9.1234f),(Float)json.optFloat("$.b.e.h[3]"));
        assertEquals(Float.valueOf(5.123132f), (Float)json.optFloat("$.b.e.g"));
        assertEquals(Float.valueOf(4.0f), (Float)json.optFloat("$.b.e.f"));
        assertEquals(Float.valueOf(1.0f),(Float) json.optFloat("$.a"));
        assertEquals(Float.valueOf((float) Math.PI), (Float)json.optFloat("$.b.d"));
        assertEquals(Float.valueOf(10.0f), (Float)json.optFloat("$.b.e.h[4].j"));
        assertEquals(Float.valueOf(11.0f), (Float)json.optFloat("$.b.e.h[4].k"));

        assertTrue(json.remove("$.b.e.h[3]"));
        assertEquals((Float)Float.NaN,(Float)json.optFloat("$.b.e.h[3]"));
        assertTrue(json.remove("$.b.e.g"));
        assertEquals((Float)Float.NaN,(Float)json.optFloat("$.b.e.g"));


    }

    @Test
    public  void optLong() {
        assertEquals(9L, (long) jsonPath.optLong("b.e.h[3]"));
        assertEquals(5L, (long)jsonPath.optLong("b.e.g"));
        assertEquals(4L, (long)jsonPath.optLong("b.e.f"));
        assertEquals(1L, (long)jsonPath.optLong("a"));
        assertEquals((long)Math.PI,(long) jsonPath.optLong("b.d"));
        assertEquals(10L, (long)jsonPath.optLong("b.e.h[4].j"));
        assertEquals(11L, (long)jsonPath.optLong("b.e.h[4].k"));

    }

    @Test
    public void optShort() {
        assertEquals((short)9,(short) jsonPath.optShort("b.e.h[3]"));
        assertEquals((short)5,(short) jsonPath.optShort("b.e.g"));
        assertEquals((short)4, (short)jsonPath.optShort("b.e.f"));
        assertEquals((short)1, (short)jsonPath.optShort("a"));
        assertEquals((short)Math.PI,(short) jsonPath.optShort("b.d"));
        assertEquals((short)10, (short)jsonPath.optShort("b.e.h[4].j"));
        assertEquals((short)11,(short) jsonPath.optShort("b.e.h[4].k"));


    }

    @Test
    public  void optByte() {
        assertEquals((byte)9, (byte)jsonPath.optByte("b.e.h[3]"));
        assertEquals((byte)5,(byte) jsonPath.optByte("b.e.g"));
        assertEquals((byte)4, (byte)jsonPath.optByte("b.e.f"));
        assertEquals((byte)1,(byte) jsonPath.optByte("a"));
        assertEquals((byte)Math.PI, (byte)jsonPath.optByte("b.d"));
        assertEquals((byte)10,(byte) jsonPath.optByte("b.e.h[4].j"));
        assertEquals((byte)11, (byte)jsonPath.optByte("b.e.h[4].k"));

    }

    @Test
    public void arrayPathTest() {
        String jsonArrayString = "[0,1,2,[{\"a\":true, \"b\": [10000,10001] }],4,5,[1,2,3,4,5,[6]],7,8,9,10]";
        CSONArray jsonArray = new CSONArray(jsonArrayString);
        CSONPath jsonPath = new CSONPath(jsonArray);
        assertEquals(10001, (int)jsonPath.optInteger("[3].[0].b[1]"));
        assertEquals(0, (int)jsonPath.optInteger("[0]"));
        assertEquals(10000, (int)jsonPath.optInteger("[3].[0].b[0]"));
        assertEquals(10000, (int)jsonPath.optInteger("[3].[0].b[0]"));
        assertEquals(true, jsonPath.get("[3].[0].a"));
        assertEquals(5, jsonPath.get("[6].[4]"));
        assertEquals(6, jsonPath.get("[6].[5].[0]"));
    }


    @Test
    public  void optInteger() {
        assertEquals(10, (int)jsonPath.optInteger("b.e.h[4].j"));



        assertEquals(9,(int) jsonPath.optInteger("b.e.h[3]"));
        assertEquals(5, (int)jsonPath.optInteger("b.e.g"));
        assertEquals(4, (int)jsonPath.optInteger("b.e.f"));
        assertEquals(1, (int)jsonPath.optInteger("a"));
        assertEquals((int)Math.PI,(int) jsonPath.optInteger("b.d"));
        assertEquals(11, (int)jsonPath.optInteger("b.e.h[4].k"));

    }

    @Test
    public void optString() {
        assertEquals("9.1234", jsonPath.optString("b.e.h[3]"));
        assertEquals("5.123132", jsonPath.optString("b.e.g"));
        assertEquals("4", jsonPath.optString("b.e.f"));
        assertEquals("1", jsonPath.optString("a"));
        assertEquals(String.valueOf(Math.PI), jsonPath.optString("b.d"));
        assertEquals("10", jsonPath.optString("b.e.h[4].j"));
        assertEquals("11", jsonPath.optString("b.e.h[4].k"));
    }

    @Test
    public void optCSONObject() {
        assertEquals(new CSONObject().put("j", "10").put("k", "11").put("l", "12").toString(), jsonPath.optCSONObject("b.e.h[4]").toString());
    }

    @Test
    public void optCSONArray() {
        assertEquals(new CSONArray().put(6).put(7).put(8).put(9.1234).put(new CSONObject().put("j", "10").put("k", "11").put("l", "12")).toString(), jsonPath.optCSONArray("b.e.h").toString());
    }

    @Test
    public void getTest() {
        assertEquals(1, jsonPath.get("a"));
        assertEquals("true", jsonPath.get("b.c"));
        assertEquals(9.1234, jsonPath.get("b.e.h[3]"));
        assertEquals(5.123132, jsonPath.get("b.e.g"));
        assertEquals("4", jsonPath.get("b.e.f"));
        assertEquals(1, jsonPath.get("a"));
        assertEquals(Math.PI, jsonPath.get("b.d"));
        assertEquals("10", jsonPath.get("b.e.h[4].j"));
        assertEquals("11", jsonPath.get("b.e.h[4].k"));
        assertEquals("12", jsonPath.get("b.e.h[4].l"));
        assertEquals(new CSONObject().put("j", "10").put("k", "11").put("l", "12").toString(), jsonPath.get("b.e.h[4]").toString());
        assertEquals(new CSONArray().put(6).put(7).put(8).put(9.1234).put(new CSONObject().put("j", "10").put("k", "11").put("l", "12")).toString(), jsonPath.get("b.e.h").toString());

    }

    @Test
    public void putObjectTest() {
        CSONObject csonObject = new CSONObject();
        CSONPath csonPath =  csonObject.getCsonPath();
        csonPath.put("path1.path2.path3", "100");
        assertEquals(100, csonObject.getCSONObject("path1").getCSONObject("path2").getInt("path3"));
        assertEquals("100", csonObject.getCSONObject("path1").getCSONObject("path2").getString("path3"));
    }

    @Test
    public void putArrayOrObjectMixedTest() {
        CSONObject csonObject = new CSONObject();
        CSONPath csonPath =  csonObject.getCsonPath();
        csonPath.put("path1[0].path2[1][2]path3[3]", "100");
        assertEquals(100, csonObject.getCSONArray("path1").getCSONObject(0).getCSONArray("path2").getCSONArray(1).getCSONObject(2).getCSONArray("path3").getInteger(3));
    }


    @Test
    public void putArrayOrObjectMixedTest2() {
        CSONObject csonObject = new CSONObject();
        CSONPath csonPath =  csonObject.getCsonPath();
        csonPath.put("path1[0].path2[1][2]path3", "100");
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));
        assertEquals(100, csonObject.getCSONArray("path1").getCSONObject(0).getCSONArray("path2").getCSONArray(1).getCSONObject(2).getInt("path3"));
    }

    @Test
    public void putArrayOrObjectMixedTest3() {
        CSONObject csonObject = new CSONObject();
        CSONPath csonPath =  csonObject.getCsonPath();
        csonPath.put("path1[0].path2[1][2].path3.path4", "100");
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));
        assertEquals(100, csonObject.getCSONArray("path1").getCSONObject(0).getCSONArray("path2").getCSONArray(1).getCSONObject(2).getCSONObject("path3").getInt("path4"));

        csonPath.put("path1[0].path2[1][2]path3.path4", 200);
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));
        assertEquals(200, csonObject.getCSONArray("path1").getCSONObject(0).getCSONArray("path2").getCSONArray(1).getCSONObject(2).getCSONObject("path3").getInt("path4"));

        csonPath.put("path1[0].path2[1][2].path3.path4[20][0]", 400);
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));
        assertEquals(400, csonObject.getCSONArray("path1").getCSONObject(0).getCSONArray("path2").getCSONArray(1).getCSONObject(2).getCSONObject("path3").getCSONArray("path4").getCSONArray(20).getInt(0));
    }

    @Test
    public void putArrayTest() {
        CSONArray csonArray = new CSONArray();
        CSONPath csonPath =  csonArray.getCsonPath();
        csonPath.put("[0][1][2][3]", "100");
        assertEquals(100, csonArray.getCSONArray(0).getCSONArray(1).getCSONArray(2).getInteger(3));
        assertEquals("100", csonArray.getCSONArray(0).getCSONArray(1).getCSONArray(2).getString(3));
    }

}