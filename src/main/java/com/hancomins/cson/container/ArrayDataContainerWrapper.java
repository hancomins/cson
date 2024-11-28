package com.hancomins.cson.container;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;

public class ArrayDataContainerWrapper implements ArrayDataContainer {
    private ArrayDataContainer container;

    public ArrayDataContainerWrapper(ArrayDataContainer container) {
        this.container = container;
    }

    public ArrayDataContainerWrapper() {
    }

    public void setContainer(ArrayDataContainer container) {
        this.container = container;
    }

    public ArrayDataContainer getContainer() {
        return container;
    }

    @Override
    public void add(Object value) {
        if (container == null) {
            return;
        }
        container.add(value);
    }

    @Override
    public Object get(int index) {
        if (container == null) {
            return null;
        }
        return container.get(index);
    }

    @Override
    public void set(int index, Object value) {
        if (container == null) {
            return;
        }
        container.set(index, value);
    }

    @Override
    public void setComment(int index, String comment, CommentPosition position) {
        if (container == null) {
            return;
        }
        container.setComment(index, comment, position);
    }

    @Override
    public String getComment(int index, CommentPosition position) {
        if (container == null) {
            return null;
        }
        return container.getComment(index, position);
    }

    @Override
    public void remove(int index) {
        if (container == null) {
            return;
        }
        container.remove(index);
    }

    @Override
    public int size() {
        if (container == null) {
            return 0;
        }
        return container.size();
    }

    @Override
    public void setSourceFormat(FormatType formatType) {
        if (container == null) {
            return;
        }
        container.setSourceFormat(formatType);
    }

    @Override
    public CommentObject<Integer> getCommentObject(int index) {
        if (container == null) {
            return null;
        }
        return container.getCommentObject(index);
    }

    @Override
    public void setComment(String comment, CommentPosition commentPosition) {
        if (container == null) {
            return;
        }
        container.setComment(comment, commentPosition);
    }

    @Override
    public void setComment(CommentObject<?> commentObject) {

    }

    @Override
    public String getComment(CommentPosition commentPosition) {
        if (container == null) {
            return null;
        }
        return container.getComment(commentPosition);
    }

    @Override
    public DataIterator<?> iterator() {
        if (container == null) {
            return null;
        }
        return container.iterator();
    }

    public static ArrayDataContainerFactory newFactory(ArrayDataContainer container) {
        return () -> new ArrayDataContainerWrapper(container);
    }
}