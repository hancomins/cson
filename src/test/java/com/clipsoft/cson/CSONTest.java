package com.clipsoft.cson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class CSONTest {

    @Test
    public void jsonStringParsingTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("11111",1)
        .put("22222",1L).put("33333","3").put("00000", true).put("44444", 4.4f).put("55555", 5.5).put("66666", '6');
        JSONArray numberJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        jsonObject.put("AN",numberJsonArray);

        JSONArray objectJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSONObject().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()));
        }
        jsonObject.put("AO",objectJsonArray);


        jsonObject = new JSONObject(jsonObject.toString());

        CSONObject csonObject = new CSONObject(new CSONObject(jsonObject.toString()).toString(JSONOptions.json()), JSONOptions.json());
        Set<String> originalKeySet = jsonObject.keySet();
        Set<String> csonKeySet =csonObject.keySet();
        assertEquals(originalKeySet, csonKeySet);
        for(String oriKey : originalKeySet) {
            if(oriKey.startsWith("A")) continue;
            assertEquals(jsonObject.get(oriKey),csonObject.get(oriKey) );
        }

        numberJsonArray = jsonObject.getJSONArray("AN");
        CSONArray numberCsonArray = csonObject.getCSONArray("AN");
        assertEquals(numberJsonArray.length(), numberCsonArray.size());
        for(int i = 0, n = numberJsonArray.length(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberCsonArray.optString(i));
        }

        objectJsonArray = jsonObject.getJSONArray("AO");
        CSONArray objectCsonArray = csonObject.getCSONArray("AO");
        assertEquals(objectJsonArray.length(), objectCsonArray.size());
        for(int i = 0, n = objectJsonArray.length(); i < n; ++i) {
            JSONObject jao = objectJsonArray.getJSONObject(i);
            CSONObject cao = objectCsonArray.getCSONObject(i);
            assertEquals(jao.getString("str"), cao.getString("str"));
            assertEquals(jao.optString("true"), cao.getString("true"));
            assertEquals(jao.getBoolean("true"), cao.getBoolean("true"));
            assertEquals(jao.getBoolean("false"), cao.getBoolean("false"));
            assertEquals(jao.getInt("random"), cao.getInteger("random"));
        }
    }

    @Test
    public void jsonAndCSonParsingTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("11111",1)
                .put("22222",1L).put("33333","3").put("00000", true).put("44444", 4.4f).put("55555", 5.5).put("66666", '6');
        JSONArray numberJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        jsonObject.put("AN",numberJsonArray);

        JSONArray objectJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSONObject().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()).put("float", ThreadLocalRandom.current().nextDouble()));
        }
        jsonObject.put("AO",objectJsonArray);


        jsonObject = new JSONObject(jsonObject.toString());

        CSONObject csonObject = new CSONObject(new CSONObject(jsonObject.toString()).toBytes());
        Set<String> originalKeySet = jsonObject.keySet();
        Set<String> csonKeySet =csonObject.keySet();
        assertEquals(originalKeySet, csonKeySet);
        for(String oriKey : originalKeySet) {
            if(oriKey.startsWith("A")) continue;
            assertEquals(jsonObject.get(oriKey),csonObject.get(oriKey) );
        }

        numberJsonArray = jsonObject.getJSONArray("AN");
        CSONArray numberCsonArray = csonObject.getCSONArray("AN");
        assertEquals(numberJsonArray.length(), numberCsonArray.size());
        for(int i = 0, n = numberJsonArray.length(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberCsonArray.optString(i));
        }

        objectJsonArray = jsonObject.getJSONArray("AO");
        CSONArray objectCsonArray = csonObject.getCSONArray("AO");
        assertEquals(objectJsonArray.length(), objectCsonArray.size());
        for(int i = 0, n = objectJsonArray.length(); i < n; ++i) {
            JSONObject jao = objectJsonArray.getJSONObject(i);
            CSONObject cao = objectCsonArray.getCSONObject(i);
            assertEquals(jao.getString("str"), cao.getString("str"));
            assertEquals(jao.optString("true"), cao.getString("true"));
            assertEquals(jao.getBoolean("true"), cao.getBoolean("true"));
            assertEquals(jao.getBoolean("false"), cao.getBoolean("false"));
            assertEquals(jao.getInt("random"), cao.getInteger("random"));
            assertEquals(Double.valueOf(jao.getDouble("float")),Double.valueOf(cao.getDouble("float")));
            assertEquals(Float.valueOf(jao.getFloat("float")),Float.valueOf(cao.getFloat("float")));
            assertEquals(jao.getInt("float"), cao.getInteger("float"));
        }
    }

    @Test
    public void overBufferTest() {
        CSONObject csonObject = new CSONObject().put("sdfasdf",213123).put("sdf2w123", 21311).put("key", "name");
        byte[] buffer = csonObject.toBytes();
        ArrayList<Byte> list = new ArrayList<>();
        for(byte b : buffer) {
            list.add(b);
        }
        Random rand = new Random(System.currentTimeMillis());
        for(int i = 0; i < 1000; ++i) {
            list.add((byte)rand.nextInt(255));
        }
        byte[] overBuffer = new byte[list.size()];
        for(int i = 0, n = list.size(); i < n; ++i) {
            overBuffer[i] = list.get(i);
        }
        CSONObject fromOverBuffer = new CSONObject(overBuffer);
        assertArrayEquals(csonObject.toBytes(), fromOverBuffer.toBytes());

    }


    @Test
    public void jsonArrayAndCSonParsingTest() {
        JSONArray numberJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        JSONArray objectJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSONObject().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()).put("float", ThreadLocalRandom.current().nextDouble()));
        }

        numberJsonArray = new JSONArray(numberJsonArray.toString());
        CSONArray numberCsonArray = new CSONArray(new CSONArray(numberJsonArray.toString()).toByteArray());
        assertEquals(numberJsonArray.length(), numberCsonArray.size());
        for(int i = 0, n = numberJsonArray.length(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberCsonArray.optString(i));
        }

        objectJsonArray = new JSONArray(objectJsonArray.toString());
        System.out.println(objectJsonArray.toString());
        CSONArray objectCsonArray = new CSONArray(new CSONArray(objectJsonArray.toString()).toByteArray());


        assertEquals(objectJsonArray.length(), objectCsonArray.size());
        for(int i = 0, n = objectJsonArray.length(); i < n; ++i) {
            System.out.println(i);
            JSONObject jao = objectJsonArray.getJSONObject(i);
            CSONObject cao = objectCsonArray.getCSONObject(i);
            System.out.println(jao.toString());
            System.out.println(cao.toString());

            assertEquals(jao.getString("str"), cao.getString("str"));
            assertEquals(jao.optString("true"), cao.getString("true"));
            assertEquals(jao.getBoolean("true"), cao.getBoolean("true"));
            assertEquals(jao.getBoolean("false"), cao.getBoolean("false"));
            assertEquals(jao.getLong("random"), cao.getLong("random"));
            assertEquals(Double.valueOf(jao.getDouble("float")),Double.valueOf(cao.getDouble("float")));
            assertEquals(Float.valueOf(jao.getFloat("float")),Float.valueOf(cao.getFloat("float")));
            assertEquals(jao.getInt("float"), cao.getInteger("float"));
        }
    }


    @Test
    public void booleanInArray() {
        CSONArray csonArray = new CSONArray();
        csonArray.add(true);
        csonArray.add(false);
        csonArray.add(true);
        assertEquals(csonArray.toString(), "[true,false,true]");

        CSONObject csonObject = new CSONObject();
        csonObject.put("true", true);
        assertEquals(csonObject.toString(), "{\"true\":true}");




    }






}