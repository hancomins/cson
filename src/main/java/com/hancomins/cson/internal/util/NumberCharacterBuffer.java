package com.hancomins.cson.internal.util;

public class NumberCharacterBuffer {
    private byte[] chars;

    private int length = 0;
    private int capacity = 32;


    public NumberCharacterBuffer() {
        chars = new byte[capacity];
    }

    public NumberCharacterBuffer(int capacity) {
        this.capacity = capacity;
        chars = new byte[capacity];
    }

    @Override
    public String toString() {
        return new String(chars, 0, length);
    }


    public NumberCharacterBuffer append(char c) {
        ensureCapacity(length + 1);
        chars[length++] = (byte)c;
        return this;
    }

    public NumberCharacterBuffer append(byte c) {
        ensureCapacity(length + 1);
        chars[length++] = c;
        return this;
    }



    public void prev() {
        length--;
    }



    public NumberCharacterBuffer append(char[] c) {
        ensureCapacity(length + c.length);
        System.arraycopy(c, 0, chars, length, c.length);
        length += c.length;
        return this;
    }

    public void reset() {
        length = 0;
    }

    public byte[] getBytes() {
        return chars;
    }

    public int length() {
        return length;
    }




    public void shiftLeft(int start) {
        System.arraycopy(chars, start, chars, 0, length - start);
        length -= start;

    }

    public void setLength(int length) {
        this.length = length;
    }

    public void decreaseLength(int length) {
        this.length -= length;
    }


    public byte at(int index) {
        return chars[index];
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
            byte[] newChars = new byte[newCapacity];
            System.arraycopy(chars, 0, newChars, 0, length);
            chars = newChars;
            capacity = newCapacity;
        }
    }

    public boolean isEmpty() {
        return length == 0;
    }





}
