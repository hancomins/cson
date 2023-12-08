package com.clipsoft.cson.serializer;

import com.clipsoft.cson.CSONException;

public class CSONSerializerException extends CSONException {
    public CSONSerializerException(String message) {
        super(message);
    }

    public CSONSerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSONSerializerException(Throwable cause) {
        super(cause);
    }
}
