package com.hancomins.cson.options;

public interface WritingOptions<T> extends IMutableINumberConversionOption<T> {


    static void setDefaultWritingOptions(WritingOptions<?> options) {
        DefaultOptions.DEFAULT_WRITING_OPTIONS = options;
    }

    static WritingOptions<?> getDefaultWritingOptions() {
        return DefaultOptions.DEFAULT_WRITING_OPTIONS;
    }


    static JSON5WriterOption json5() {
        return JSON5WriterOption.json5();
    }

    static JSON5WriterOption json5Pretty() {
        return JSON5WriterOption.prettyJson5();
    }

    static JSON5WriterOption json() {
        return JSON5WriterOption.json();
    }

    static JSON5WriterOption jsonPretty() {
        return JSON5WriterOption.prettyJson();
    }

}
