package com.hancomins.cson.serializer.mapper;

public class CSONObjectException extends RuntimeException {

    public CSONObjectException(String message) {
        super(message);
    }

    public CSONObjectException(String message, Exception cause) {
        super(message,cause);
    }
}
