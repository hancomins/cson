package com.hancomins.cson.util;

public class ReferenceWrapper<T> {

    private T value;

    public ReferenceWrapper() {
    }

    public ReferenceWrapper(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void clear() {
        this.value = null;
    }

    public boolean isNull() {
        return value == null;
    }


}
