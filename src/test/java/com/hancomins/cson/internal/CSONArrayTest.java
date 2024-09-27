package com.hancomins.cson.internal;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CSONArray 테스트 (성공)")
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



        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put("1", 123);
        //jsonObject.getString("1");



    }
}