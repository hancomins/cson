package com.hancomins.cson.options;

public class WritingOptions<T> extends MutableINumberConversionOption<T> {

    protected WritingOptions() {
    }


    public static JsonWritingOptions json5() {
        return JsonWritingOptions.json5();
    }

    public static JsonWritingOptions json() {
        return JsonWritingOptions.json();
    }

    public static JsonWritingOptions jsonPretty() {
        return JsonWritingOptions.prettyJson();
    }

}
