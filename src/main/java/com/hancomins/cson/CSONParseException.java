package com.hancomins.cson;

public class CSONParseException  extends RuntimeException {
    public CSONParseException(String message) {
        super(message);
    }

    public CSONParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSONParseException() {
        super();
    }

}
