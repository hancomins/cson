package com.hancomins.cson.format.json;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.*;
import com.hancomins.cson.options.JSON5WriterOption;
import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.NullValue;

import java.util.Map;

public class JSON5Writer extends WriterBorn  {

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

        space =  JSON5WriterOption.isPretty() ? JSON5WriterOption.getDepthString() : "";
        pretty = JSON5WriterOption.isPretty();
        prettyArray = pretty & !JSON5WriterOption.isUnprettyArray();
        skipComments = JSON5WriterOption.isSkipComments();
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
        /*if(isArrayRootContainer()) {
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
        }*/

    }

    @Override
    protected void writeSuffix() {
        if(stringBuilder.last() == ',') {
            stringBuilder.prev();
        }
        /*if(isArrayRootContainer()) {
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
        }*/

    }

    @Override
    protected void writeArrayPrefix(BaseDataContainer parents,DataIterator<?> iterator) {
        stringBuilder.append("[");
        depth++;
    }

    @Override
    protected void writeObjectPrefix(BaseDataContainer parents, DataIterator<Map.Entry<String, Object>> iterator) {
        writeCommentForKeyValueContainer(CommentPosition.BEFORE_VALUE);
        stringBuilder.append("{");
        depth++;
    }

    @Override
    protected void writeObjectSuffix(DataIterator<Map.Entry<String, Object>> iterator) {
        if(stringBuilder.last() == ',') {
            stringBuilder.prev();
        }
        depth--;
        if(pretty && iterator.size() != 0) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        stringBuilder.append("}");
        writeCommentForKeyValueContainer(CommentPosition.AFTER_VALUE);
        stringBuilder.append(",");
    }

    @Override
    protected void writeArraySuffix(DataIterator<Object> iterator) {
        if(stringBuilder.last() == ',') {
            stringBuilder.prev();
        }
        depth--;
        if(prettyArray && iterator.size() != 0) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        stringBuilder.append("],");

    }

    @Override
    protected void writeKey(String key) {
        writeCommentForKeyValueContainer(CommentPosition.BEFORE_KEY);
        if(pretty) {
            stringBuilder.append("\n");
            stringBuilder.repeat(space, depth);
        }
        stringBuilder.append(keyQuote);
        stringBuilder.append(key);
        stringBuilder.append(keyQuote);
        writeCommentForKeyValueContainer(CommentPosition.AFTER_KEY);
        stringBuilder.append(":");
    }

    private void writeCommentForKeyValueContainer(CommentPosition commentPosition) {
        CommentObject commentObject = getCurrentCommentObject();
        if(commentObject == null) return;
        String comment = commentObject.getComment(commentPosition);
        if(comment == null || comment.isEmpty()) return;
        if(pretty && commentPosition == CommentPosition.BEFORE_KEY) {
            String[] comments = comment.split("\n");
            for(String c : comments) {
                if(c.isEmpty()) continue;
                stringBuilder.append("\n");
                stringBuilder.repeat(space, depth);
                stringBuilder.append("//");
                stringBuilder.append(c);
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


    protected void writeValue(Object value) {
        writeCommentForKeyValueContainer(CommentPosition.BEFORE_VALUE);
        if(value instanceof String) {
            stringBuilder.append("\"");
            stringBuilder.append(value.toString());
            stringBuilder.append("\"");
        } else if(value == NullValue.Instance) {
            stringBuilder.append("null");
        }
        else {
            stringBuilder.append(String.valueOf(value));
        }
        writeCommentForKeyValueContainer(CommentPosition.AFTER_VALUE);
        stringBuilder.append(',');
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
