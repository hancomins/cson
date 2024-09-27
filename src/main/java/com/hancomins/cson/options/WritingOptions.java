package com.hancomins.cson.options;

public interface WritingOptions<T> extends IMutableINumberConversionOption<T> {


    static void setDefaultWritingOptions(WritingOptions<?> options) {
        DefaultOptions.DEFAULT_WRITING_OPTIONS = options;
    }

    static WritingOptions<?> getDefaultWritingOptions() {
        return DefaultOptions.DEFAULT_WRITING_OPTIONS;
    }


    static JsonWritingOptions json5() {
        return JsonWritingOptions.json5();
    }

    static JsonWritingOptions json5Pretty() {
        return JsonWritingOptions.prettyJson5();
    }

    static JsonWritingOptions json() {
        return JsonWritingOptions.json();
    }

    static JsonWritingOptions jsonPretty() {
        return JsonWritingOptions.prettyJson();
    }

}
