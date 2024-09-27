package com.hancomins.cson.util;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public interface DataReader {

    static DataReader wrapInputStream(InputStream inputStream) {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        return new DataReaderWrapInputStream(dataInputStream);
    }

    static DataReader wrapByteBuffer(ByteBuffer byteBuffer) {
        return new DataReaderWrapByteBuffer(byteBuffer);
    }

    byte get();

    void get(byte[] dst, int offset, int length);

    int getInt();

    char getChar();

    short getShort();

    float getFloat();

    double getDouble();

    long getLong();

}
