package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.container.DataIterator;
import com.hancomins.cson.container.FormatType;
import com.hancomins.cson.container.KeyValueDataContainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ContainerOfStringMapKeyValue implements KeyValueDataContainer {


    private final Map<String, Object> stringKeyValueMap = new HashMap<>();

    Map<String, Object> getMap() {
        return stringKeyValueMap;
    }

    @Override
    public void put(String key, Object value) {
        stringKeyValueMap.put(key, value);
    }

    @Override
    public Object get(String key) {
        return stringKeyValueMap.get(key);
    }

    @Override
    public void remove(String key) {
        stringKeyValueMap.remove(key);
    }

    @Override
    public void setComment(String key, String comment, CommentPosition type) {

    }

    @Override
    public String getComment(String key, CommentPosition type) {
        return "";
    }

    @Override
    public CommentObject<String> getCommentObject(String key) {
        return null;
    }

    @Override
    public Set<String> keySet() {
        return stringKeyValueMap.keySet();
    }

    @Override
    public String getLastAccessedKey() {
        return "";
    }

    @Override
    public int size() {
        return stringKeyValueMap.size();
    }

    @Override
    public void setSourceFormat(FormatType formatType) {

    }

    @Override
    public void setComment(CommentObject<?> commentObject) {

    }

    @Override
    public DataIterator<?> iterator() {
        Iterator<Map.Entry<String, Object>> iterator = stringKeyValueMap.entrySet().iterator();
        return new DataIterator<Map.Entry<String, Object>>(iterator, stringKeyValueMap.size(), true) {
            @Override
            public Map.Entry<String, Object> next() {
                return iterator.next();
            }
        };
    }
}
