package com.clipsoft.cson.util;

public class RefString implements CharSequence {

    final char[] value;
    final int offset;
    final int count;


    RefString(char[] value, int offset, int len) {
        this.value = value;
        this.offset = offset;
        this.count = len;
    }

    @Override
    public int length() {
        return count;
    }

    @Override
    public char charAt(int index) {
        return value[index + offset];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new RefString(value, offset + start, end - start);
    }
}
