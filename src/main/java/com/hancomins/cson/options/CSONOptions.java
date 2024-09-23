package com.hancomins.cson.options;

public interface CSONOptions {


    <T extends ParsingOptions<T>> T getParsingOptions();
    <T extends WritingOptions<T>> T getWritingOptions();






}
