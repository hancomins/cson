package com.hancomins.cson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONOptionsTest {

    @Test
    @DisplayName("Pure JSON 확인")
    void json() {
        JSONOptions jsonOptions = JSONOptions.json();
        assertFalse(jsonOptions.isPretty());
        assertTrue(JSONOptions.isPureJSONOption(jsonOptions));
        jsonOptions.setPretty(true);
        assertTrue(JSONOptions.isPureJSONOption(jsonOptions));
    }

}