package com.hancomins.cson.util;

public class NullValue implements Cloneable {
    public static final NullValue Instance = new NullValue();



    private NullValue() {
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public NullValue clone() {
        return Instance;
    }
}
