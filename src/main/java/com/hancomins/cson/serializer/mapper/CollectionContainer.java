package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.ArrayDataContainer;
import com.hancomins.cson.format.DataIterator;
import com.hancomins.cson.format.FormatType;

public class CollectionContainer implements ArrayDataContainer {
    @Override
    public void add(Object value) {

    }

    @Override
    public Object get(int index) {
        return null;
    }

    @Override
    public void set(int index, Object value) {

    }

    @Override
    public void setComment(int index, String comment, CommentPosition position) {

    }

    @Override
    public String getComment(int index, CommentPosition position) {
        return "";
    }

    @Override
    public void remove(int index) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setSourceFormat(FormatType formatType) {

    }

    @Override
    public void setComment(CommentObject<?> commentObject) {

    }

    @Override
    public DataIterator<?> iterator() {
        return null;
    }

    @Override
    public CommentObject getCommentObject(int index) {
        return null;
    }
}
