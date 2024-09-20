package com.hancomins.cson.options;

import com.hancomins.cson.JSONOptions;


public interface StringFormatOption<T> extends MutableNumberConversionOption<T> {


    StringFormatType getFormatType();


    static JSONOptions json() {
        return JSONOptions.json();
    }

    static JSONOptions jsonPretty() {
        return JSONOptions.json().setPretty(true);
    }

    static JSONOptions json5() {
        return JSONOptions.json5();
    }




}
