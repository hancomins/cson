package com.clipsoft.cson;

public interface StringFormatOption {


    final static StringFormatOption PURE_JSON = () -> StringFormatType.PureJSON;
    StringFormatType getFormatType();


    public static JSONOptions json() {
        return JSONOptions.json();
    }

    public static JSONOptions json5() {
        return JSONOptions.json5();
    }

    public static StringFormatOption jsonPure() {
        return PURE_JSON;
    }




}
