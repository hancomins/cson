package com.clipsoft.cson;

import com.clipsoft.cson.util.NumberConversionUtil;

public interface StringFormatOption<T> extends NumberConversionUtil.MutableNumberConversionOption<T> {


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

    static StringFormatOption<?> jsonPure() {
        return new PureJSONOption();
    }




}
