package com.clipsoft.cson;

public interface StringFormatOption {



    StringFormatOption PURE_JSON = () -> StringFormatType.PureJSON;

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

    static StringFormatOption jsonPure() {
        return PURE_JSON;
    }




}
