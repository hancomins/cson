package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONException;

public class CSONMapperException extends CSONException {
    public CSONMapperException(String message) {
        super(message);
    }
    public CSONMapperException(String message, Throwable cause) {
        super(message, cause);
    }
    @SuppressWarnings("unused")
    public CSONMapperException(Throwable cause) {
        super(cause);
    }
}
