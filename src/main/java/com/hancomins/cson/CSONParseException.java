package com.hancomins.cson;

public class CSONParseException  extends RuntimeException {

    private int index = -1;
    private int line = -1;

    CSONParseException() {

    }

    CSONParseException(String message) {
        super(message);

    }

    CSONParseException(String message, int line, int index) {
        super(message);
        this.index = index;
        this.line = line;
    }

    public CSONParseException(String message, int line, int index, Throwable cause) {
        super(message, cause);
        this.index = index;
        this.line = line;
    }

}
