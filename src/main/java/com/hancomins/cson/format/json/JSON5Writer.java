package com.hancomins.cson.format.json;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.*;
import com.hancomins.cson.options.JSON5WriterOption;
import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.NullValue;

import java.util.Base64;
import java.util.Map;

public class JSON5Writer extends WriterBorn {

    private static final int DEFAULT_BUFFER_SIZE = 512;
    private final CharacterBuffer stringBuilder = new CharacterBuffer(DEFAULT_BUFFER_SIZE);
    private final String keyQuote;

    private String space = " ";
    private int depth = 0;
    private boolean prettyArray = true;
    private boolean pretty = false;
    private boolean skipComments = false;

    public JSON5Writer(JSON5WriterOption JSON5WriterOption) {
        super(JSON5WriterOption.isSkipComments());
        keyQuote = JSON5WriterOption.getKeyQuote();

        space = JSON5WriterOption.isPretty() ? JSON5WriterOption.getDepthString() : "";
        pretty = JSON5WriterOption.isPretty();
        prettyArray = pretty & !JSON5WriterOption.isUnprettyArray();
        skipComments = JSON5WriterOption.isSkipComments();
    }


    @Override
    protected void writeHeaderComment(String comment) {
        if (!skipComments && comment != null && !comment.isEmpty()) {
            stringBuilder.append("/*");
            stringBuilder.append(comment);
            stringBuilder.append("*/");
            if (pretty) {
                stringBuilder.append("\n");
            }
        }
    }

    @Override
    protected void writeFooterComment(String comment) {
        if (!skipComments && comment != null && !comment.isEmpty()) {
            if (pretty) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("/*");
            stringBuilder.append(comment);
            stringBuilder.append("*/");
        }
    }

    @Override
    protected void writePrefix() {

    }

    @Override
    protected void writeSuffix() {
        if (stringBuilder.last() == ',') {
            stringBuilder.prev();
        }
    }

    @Override
    protected void writeArrayPrefix(BaseDataContainer parents, DataIterator<?> iterator) {
        writeComment(CommentPosition.BEFORE_VALUE, pretty, true);
        stringBuilder.append("[");
        depth++;
    }

    @Override
    protected void writeObjectPrefix(BaseDataContainer parents, DataIterator<Map.Entry<String, Object>> iterator) {
        writeComment(CommentPosition.BEFORE_VALUE, pretty, true);
        stringBuilder.append("{");
        depth++;
    }

    @Override
    protected void writeObjectSuffix(DataIterator<Map.Entry<String, Object>> iterator) {
        if (stringBuilder.last() == ',') {
            stringBuilder.prev();
        }
        depth--;
        if (pretty && iterator.size() != 0) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        stringBuilder.append("}");
        writeComment(CommentPosition.AFTER_VALUE, pretty, true);
        stringBuilder.append(",");
    }

    @Override
    protected void writeArraySuffix(DataIterator<Object> iterator) {
        if (stringBuilder.last() == ',') {
            stringBuilder.prev();
        }
        depth--;
        if (prettyArray && iterator.size() != 0) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        stringBuilder.append("]");
        writeComment(CommentPosition.AFTER_VALUE, pretty, true);
        stringBuilder.append(",");

    }

    @Override
    protected void writeKey(String key) {
        writeComment(CommentPosition.BEFORE_KEY, pretty);
        if (pretty) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        stringBuilder.append(keyQuote);
        stringBuilder.append(key);
        stringBuilder.append(keyQuote);
        writeComment(CommentPosition.AFTER_KEY, false);
        stringBuilder.append(":");
    }

    private void writeComment(CommentPosition commentPosition, boolean pretty) {
        writeComment(commentPosition, pretty, false);
    }

    private void writeComment(CommentPosition commentPosition, boolean pretty, boolean breakLineIfPretty) {
        CommentObject<?> commentObject = getCurrentCommentObject();
        if(commentObject == null) return;
        String comment = commentObject.getComment(commentPosition);
        if(comment == null || comment.isEmpty()) return;
        if(pretty) {
            String[] comments = comment.split("\n");
            for(int i = 0; i < comments.length; i++) {
                String c = comments[i];
                if(c.isEmpty()) continue;
                if(stringBuilder.last() != ' ') {
                    stringBuilder.append("\n");
                    stringBuilder.repeat(space, depth);
                }
                stringBuilder.append("//");
                stringBuilder.append(c);
                if(breakLineIfPretty || i != comments.length - 1) {
                    stringBuilder.append("\n");
                    stringBuilder.repeat(space, depth);
                }

            }
        }
        else {
            stringBuilder.append("/*");
            stringBuilder.append(comment);
            stringBuilder.append("*/");
        }
    }

    @Override
    protected void writeObjectValue(Object value) {
        writeValue(value);
    }

    @Override
    protected void writeArrayValue(Object value) {
        if(prettyArray) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        writeValue(value);
    }


    private void writeString(String value) {
        stringBuilder.append("\"");
        for(int i = 0, n = value.length(); i < n; i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\b':
                    stringBuilder.append("\\b");
                    break;
                case '\f':
                    stringBuilder.append("\\f");
                    break;
                case '\n':
                    stringBuilder.append("\\n");
                    break;
                case '\r':
                    stringBuilder.append("\\r");
                    break;
                case '\t':
                    stringBuilder.append("\\t");
                    break;
                case '\\':
                    stringBuilder.append("\\\\");
                    break;
                case '\"':
                    stringBuilder.append("\\\"");
                    break;
                default:
                    stringBuilder.append(c);
            }
        }
        stringBuilder.append("\"");
    }


    protected void writeValue(Object value) {
        writeComment(CommentPosition.BEFORE_VALUE, prettyArray, true);
        if(value instanceof String) {
            writeString((String) value);
        } else if(value instanceof byte[]) {
            stringBuilder.append('"');
            stringBuilder.append("base64," + Base64.getEncoder().encodeToString((byte[]) value));
            stringBuilder.append('"');
        }
        // todo MAP  처리.
        else if(value == NullValue.Instance) {
            stringBuilder.append("null");
        } else if(value instanceof Character) {
            stringBuilder.append('"');
            stringBuilder.append((Character) value);
            stringBuilder.append('"');
        }
        else {
            stringBuilder.append(String.valueOf(value));
        }
        writeComment(CommentPosition.AFTER_VALUE, prettyArray, true);
        stringBuilder.append(',');
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
