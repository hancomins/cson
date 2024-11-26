package com.hancomins.cson.container;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;

import java.util.Set;

public class KeyValueDataContainerWrapper implements KeyValueDataContainer {

    private KeyValueDataContainer container;

    public KeyValueDataContainerWrapper(KeyValueDataContainer container) {
        this.container = container;
    }

    public KeyValueDataContainerWrapper() {
    }


    public boolean hasContainer() {
        return container != null;
    }

    @Override
    public void put(String key, Object value) {
        if(container == null) {
            return;
        }
        container.put(key, value);
    }

    @Override
    public Object get(String key) {
        if(container == null) {
            return null;
        }
        return container.get(key);
    }

    @Override
    public void remove(String key) {
        if(container == null) {
            return;
        }
        container.remove(key);
    }

    @Override
    public void setComment(String key, String comment, CommentPosition type) {
        if(container == null) {
            return;
        }
        container.setComment(key, comment, type);
    }

    @Override
    public String getComment(String key, CommentPosition type) {
        if(container == null) {
            return null;
        }
        return container.getComment(key, type);
    }

    @Override
    public CommentObject<String> getCommentObject(String key) {
        if(container == null) {
            return null;
        }
        return container.getCommentObject(key);
    }

    @Override
    public Set<String> keySet() {
        if(container == null) {
            return null;
        }
        return container.keySet();
    }

    @Override
    public String getLastAccessedKey() {
        if(container == null) {
            return null;
        }
        return container.getLastAccessedKey();
    }

    @Override
    public int size() {
        if(container == null) {
            return 0;
        }
        return container.size();
    }

    @Override
    public void setSourceFormat(FormatType formatType) {
        if(container == null) {
            return;
        }
        container.setSourceFormat(formatType);
    }

    @Override
    public void setComment(String comment, CommentPosition commentPosition) {
        if(container == null) {
            return;
        }
        container.setComment(comment, commentPosition);
    }

    @Override
    public void setComment(CommentObject<?> commentObject) {
        if(container == null) {
            return;
        }
        container.setComment(commentObject);
    }

    @Override
    public String getComment(CommentPosition commentPosition) {
        if(container == null) {
            return null;
        }
        return container.getComment(commentPosition);
    }

    @Override
    public DataIterator<?> iterator() {
        if(container == null) {
            return null;
        }
        return container.iterator();
    }

    public void setContainer(KeyValueDataContainer container) {
        this.container = container;
    }


    public static KeyValueDataContainerFactory newFactory(KeyValueDataContainer container) {
        return () -> new KeyValueDataContainerWrapper(container);
    }



}