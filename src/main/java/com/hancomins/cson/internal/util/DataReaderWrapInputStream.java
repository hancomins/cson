package com.hancomins.cson.internal.util;

import java.io.DataInputStream;
import java.io.IOException;

public class DataReaderWrapInputStream implements DataReader {

    private DataInputStream delegate;

    public DataReaderWrapInputStream(DataInputStream dataInputStream) {
        this.delegate = dataInputStream;
    }


    @Override
    public byte get() {
        try {
            return delegate.readByte();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }

    @Override
    public void get(byte[] dst, int offset, int length) {
        try {
            delegate.readFully(dst, offset, length);
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }

    }

    @Override
    public int getInt() {
        try {
            return delegate.readInt();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }

    @Override
    public char getChar() {
        try {
            return delegate.readChar();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }

    @Override
    public short getShort() {
        try {
            return delegate.readShort();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }

    @Override
    public float getFloat() {
        try {
            return delegate.readFloat();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }

    @Override
    public double getDouble() {
        try {
            return delegate.readDouble();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }

    @Override
    public long getLong() {
        try {
            return delegate.readLong();
        } catch (IOException e) {
            throw new DataReadFailException(e);
        }
    }


}
