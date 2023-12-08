package com.clipsoft.cson;

class NullValue {
    static final NullValue Instance = new NullValue();



    private NullValue() {
    }

    @Override
    public String toString() {
        return "null";
    }

}
