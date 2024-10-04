package com.hancomins.cson.format;

import java.util.Iterator;

public class DataIterator <T> implements Iterator<T> {
    private final Iterator<T> iterator;
    private boolean isEntryValue = false;
    private final int size;
    private int position = 0;
    private boolean isBegin = true;
    private Object key;


    public DataIterator(Iterator<T> iterator,int size, boolean isEntryValue) {
        this.iterator = iterator;
        this.isEntryValue = isEntryValue;
        this.size = size;
    }



    @Override
    public boolean hasNext() {
        return position < size;
    }

    @Override
    public T next() {
        ++position;
        isBegin = false;
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    public boolean isEntryValue() {
        return isEntryValue;
    }

    public int size() {
        return size;
    }

    public int getPosition() {
        return position;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public boolean isBegin() {
        return isBegin;
    }
}
