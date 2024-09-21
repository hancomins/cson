package com.hancomins.cson.options;

import com.hancomins.cson.JSONParsingOptions;


public interface ParsingOption<T> extends MutableNumberConversionOption<T> {


    StringFormatType getFormatType();


    static JSONParsingOptions json() {
        return JSONParsingOptions.json();
    }

    static JSONParsingOptions jsonPretty() {
        return JSONParsingOptions.json().setPretty(true);
    }

    static JSONParsingOptions json5() {
        return JSONParsingOptions.json5();
    }






}
