package com.hancomins.cson.util;

public class NullValue {
    public static final NullValue Instance = new NullValue();



    private NullValue() {
    }

    @Override
    public String toString() {
        return "null";
    }

}
