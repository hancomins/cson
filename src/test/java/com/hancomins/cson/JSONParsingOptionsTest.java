package com.hancomins.cson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONParsingOptionsTest {

    @Test
    @DisplayName("Pure JSON 확인")
    void json() {
        JSONParsingOptions jsonParsingOptions = JSONParsingOptions.json();
        assertFalse(jsonParsingOptions.isPretty());
        assertTrue(JSONParsingOptions.isPureJSONOption(jsonParsingOptions));
        jsonParsingOptions.setPretty(true);
        assertTrue(JSONParsingOptions.isPureJSONOption(jsonParsingOptions));
    }

}