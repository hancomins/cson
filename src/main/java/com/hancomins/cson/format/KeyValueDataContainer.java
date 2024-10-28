package com.hancomins.cson.format;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;



public interface KeyValueDataContainer extends BaseDataContainer {
    void put(String key, Object value);
    Object get(String key);
    void remove(String key);
    void setComment(String key, String comment, CommentPosition type);
    String getComment(String key, CommentPosition type);
    CommentObject getCommentObject(String key);
    String getLastAccessedKey();

    default void setComment(String comment, CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                setComment(null, comment, commentPosition);
        }
    }

    default String getComment(CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                return getComment(null, commentPosition);
        }
        return null;
    }

}
