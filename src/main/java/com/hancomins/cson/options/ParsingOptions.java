package com.hancomins.cson.options;


public interface ParsingOptions<T> extends IMutableINumberConversionOption<T> {





    static void setDefaultParsingOptions(ParsingOptions<?> options) {
        DefaultOptions.DEFAULT_PARSING_OPTIONS = options;
    }

    static ParsingOptions<?> getDefaultParsingOptions() {
        return DefaultOptions.DEFAULT_PARSING_OPTIONS;
    }


    StringFormatType getFormatType();

    static JsonParsingOptions json5() {
        return JsonParsingOptions.json5();
    }


}
