package com.hancomins.cson.internal.util;

import java.nio.ByteBuffer;

public class DataReaderWrapByteBuffer implements DataReader {

    private ByteBuffer delegate;

    DataReaderWrapByteBuffer(ByteBuffer byteBuffer) {
        this.delegate = byteBuffer;
    }

    @Override
    public byte get() {
        return delegate.get();
    }

    @Override
    public void get(byte[] dst, int offset, int length) {
        delegate.get(dst, offset, length);
    }

    @Override
    public int getInt() {
        return delegate.getInt();
    }

    @Override
    public char getChar() {
        return delegate.getChar();
    }

    @Override
    public short getShort() {
        return delegate.getShort();
    }

    @Override
    public float getFloat() {
        return delegate.getFloat();
    }

    @Override
    public double getDouble() {
        return delegate.getDouble();
    }

    @Override
    public long getLong() {
        return delegate.getLong();
    }
}
