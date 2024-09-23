package com.hancomins.cson.options;

class DefaultOptions {
    static ParsingOptions<?> DEFAULT_PARSING_OPTIONS = ParsingOptions.json5();
    static WritingOptions<?> DEFAULT_WRITING_OPTIONS = WritingOptions.json();
}
