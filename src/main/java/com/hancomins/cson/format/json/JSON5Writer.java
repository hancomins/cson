package com.hancomins.cson.format.json;

import com.hancomins.cson.format.BaseDataContainer;
import com.hancomins.cson.format.DataIterator;
import com.hancomins.cson.format.KeyValueDataContainer;
import com.hancomins.cson.format.WriterBorn;
import com.hancomins.cson.options.JsonWritingOptions;
import com.hancomins.cson.util.CharacterBuffer;

import java.util.Map;

public class JSON5Writer extends WriterBorn {

    private static final int DEFAULT_BUFFER_SIZE = 512;
    private final CharacterBuffer stringBuilder = new CharacterBuffer(DEFAULT_BUFFER_SIZE);
    private String keyQuote = "\"";
    private String space = " ";
    private int depth = 0;
    private boolean prettyArray = true;
    private boolean pretty = false;
    private boolean skipComments = false;

    public JSON5Writer(JsonWritingOptions JsonWritingOptions) {
        super();
        keyQuote = JsonWritingOptions.getKeyQuote();
        space =  JsonWritingOptions.isPretty() ? JsonWritingOptions.getDepthString() : "";
        pretty = JsonWritingOptions.isPretty();
        prettyArray = pretty & !JsonWritingOptions.isUnprettyArray();
        skipComments = JsonWritingOptions.isSkipComments();
    }




    @Override
    protected void writeHeaderComment(String comment) {
        if(!skipComments && comment != null && !comment.isEmpty()) {
            stringBuilder.append("/*");
            stringBuilder.append(comment);
            stringBuilder.append("*/");
            if(pretty) {
                stringBuilder.append("\n");
            }
        }
    }

    @Override
    protected void writeFooterComment(String comment) {
        if(!skipComments && comment != null && !comment.isEmpty()) {
            if(pretty) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("/*");
            stringBuilder.append(comment);
            stringBuilder.append("*/");
        }
    }

    @Override
    protected void writePrefix() {
        if(isArrayRootContainer()) {
            stringBuilder.append("[");
            if(prettyArray) {
                stringBuilder.append("\n");
                depth++;
            }
        } else {
            stringBuilder.append("{");
            if(pretty) {
                stringBuilder.append("\n");
                depth++;
            }
        }

    }

    @Override
    protected void writeSuffix() {
        if(isArrayRootContainer()) {
            if(prettyArray) {
                stringBuilder.append("\n");
                depth--;
                stringBuilder.append("]");
            } else {
                stringBuilder.append("]");
            }
        } else {
            if(pretty) {
                stringBuilder.append("\n");
                depth--;
            }
            stringBuilder.append("}");
        }

    }

    @Override
    protected void writeArrayPrefix(BaseDataContainer parents,DataIterator<?> iterator) {
        if (pretty && parents instanceof KeyValueDataContainer) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);

        }
        if(prettyArray) {
            stringBuilder.append("\n");
            depth++;
        }
    }

    @Override
    protected void writeObjectPrefix(BaseDataContainer parents, DataIterator<Map.Entry<String, Object>> iterator) {

    }

    @Override
    protected void writeObjectSuffix() {

    }

    @Override
    protected void writeArraySuffix() {

    }

    @Override
    protected void writeKey(String key) {

    }

    @Override
    protected void writeValue(Object value) {

    }
}
