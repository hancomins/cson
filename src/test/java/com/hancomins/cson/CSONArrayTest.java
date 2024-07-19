package com.hancomins.cson;


import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;


import static org.junit.Assert.*;

public class CSONArrayTest {

    @Test
    public void set() {
        CSONArray csonArray = new CSONArray();
        csonArray.set(100, 123);
        for(int i = 0; i < 100; i++) {
            assertNull(csonArray.get(i));
        }
        assertEquals(123, csonArray.get(100));
        assertEquals(101, csonArray.size());

        csonArray.set(50, "hahaha");
        assertEquals("hahaha", csonArray.get(50));


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("1", 123);
        jsonObject.getString("1");



    }
}