package com.hancomins.cson.util;

public class CharacterBuffer {
    private char[] chars = new char[32];

    private int length = 0;
    private int capacity = 32;


    public CharacterBuffer() {
    }

    public CharacterBuffer(int capacity) {
        this.capacity = capacity;
        chars = new char[capacity];
    }

    @Override
    public String toString() {
        return new String(chars, 0, length);
    }

    public CharacterBuffer append(char c) {
        ensureCapacity(length + 1);
        chars[length++] = c;
        return this;
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





}
