package com.hancomins.cson.internal.util;

public class NullValue {
    public static final NullValue Instance = new NullValue();



    private NullValue() {
    }

    @Override
    public String toString() {
        return "null";
    }

}
