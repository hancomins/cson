package com.hancomins.cson.options;


public interface ParsingOptions<T> extends IMutableINumberConversionOption<T> {




    //ParsingOptions<?> //DEFAULT_PARSING_OPTIONS = ParsingOptions.json5();

    static void setDefaultParsingOptions(ParsingOptions<?> options) {
        DEFAULT_PARSING_OPTIONS = options;
    }


    StringFormatType getFormatType();

    static JsonParsingOptions json5() {
        return JsonParsingOptions.json5();
    }


}
