package com.hancomins.cson.format;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;

public interface ArrayDataContainer extends BaseDataContainer {
    void add(Object value);
    Object get(int index);
    void setComment(int index, String comment, CommentPosition position);
    String getComment(int index, CommentPosition position);
    void remove(int index);
    int size();
    CommentObject getCommentObject(int index);

    default void setComment(String comment, CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                setComment(-1, comment, commentPosition);
        }
    }

    default String getComment(CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                getComment(-1, commentPosition);
        }
        return null;
    }
}
