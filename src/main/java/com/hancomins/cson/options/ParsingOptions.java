package com.hancomins.cson.options;


public interface ParsingOptions<T> extends IMutableINumberConversionOption<T> {


    StringFormatType getFormatType();



    static JsonParsingOptions json5() {
        return JsonParsingOptions.json5();
    }






}
