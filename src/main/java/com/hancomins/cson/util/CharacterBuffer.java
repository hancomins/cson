package com.hancomins.cson.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class CharacterBuffer {
    private char[] chars;

    private int length = 0;
    private int capacity = 32;


    public CharacterBuffer() {
        chars = new char[capacity];
    }

    public CharacterBuffer(int capacity) {
        this.capacity = capacity;
        chars = new char[capacity];
    }

    @Override
    public String toString() {
        return new String(chars, 0, length);
    }

    public String toTrimString() {
        int start = 0;
        int end= 0;
        for(int i = 0; i < length; i++) {
            if(!Character.isWhitespace(chars[i])) {
                start = i;
                break;
            }
        }
        for(int i = length - 1; i >= 0; i--) {
            if(!Character.isWhitespace(chars[i])) {
                end = i + 1;
                break;
            }
        }
        if(end <= start) {
            return "";
        }

        return new String(chars, start, length - (length - end) - start);
    }

    public CharacterBuffer append(char c) {
        ensureCapacity(length + 1);
        chars[length++] = c;
        return this;
    }


    public CharacterBuffer append(int v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(double v) {
        String value = String.valueOf(v);
        return append(value);

    }

    public CharacterBuffer append(long v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(float v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(boolean v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(byte v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(short v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(BigInteger v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public CharacterBuffer append(BigDecimal v) {
        String value = String.valueOf(v);
        return append(value);
    }

    public void prev() {
        length--;
    }

    public CharacterBuffer append(String s) {
        ensureCapacity(length + s.length());
        s.getChars(0, s.length(), chars, length);
        length += s.length();
        return this;
    }

    public CharacterBuffer append(char[] c) {
        ensureCapacity(length + c.length);
        System.arraycopy(c, 0, chars, length, c.length);
        length += c.length;
        return this;
    }

    public void reset() {
        length = 0;
    }

    public char[] getChars() {
        return chars;
    }

    public int length() {
        return length;
    }





    public void setLength(int length) {
        this.length = length;
    }

    public void decreaseLength(int length) {
        this.length -= length;
    }


    public char charAt(int index) {
        return chars[index];
    }

    public void insert(int index, String str) {
        ensureCapacity(length + str.length());
        System.arraycopy(chars, index, chars, index + str.length(), length - index);
        str.getChars(0, str.length(), chars, index);
        length += str.length();
    }

    public String subSequence(int start, int end) {
        return new String(chars, start, end - start);
    }


    private void ensureCapacity(int minCapacity) {
        if (minCapacity > capacity) {
            int newCapacity = capacity * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            char[] newChars = new char[newCapacity];
            System.arraycopy(chars, 0, newChars, 0, length);
            chars = newChars;
            capacity = newCapacity;
        }
    }

    public boolean isEmpty() {
        return length == 0;
    }





}
