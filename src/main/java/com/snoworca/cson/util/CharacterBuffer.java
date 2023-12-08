package com.snoworca.cson.util;

public class CharacterBuffer {
    private char[] chars = new char[32];

    private int length = 0;
    private int capacity = 32;


    public CharacterBuffer() {
    }

    public String toString() {
        return new String(chars, 0, length);
    }

    public void append(char c) {
        ensureCapacity(length + 1);
        chars[length++] = c;
    }

    public void append(char[] c) {
        ensureCapacity(length + c.length);
        System.arraycopy(c, 0, chars, length, c.length);
        length += c.length;
    }

    public void reset() {
        length = 0;
    }

    public char[] getChars() {
        return chars;
    }

    public int getLength() {
        return length;
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
