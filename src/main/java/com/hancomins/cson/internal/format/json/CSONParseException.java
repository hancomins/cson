package com.hancomins.cson.internal.format.json;

import java.util.Locale;

public class CSONParseException  extends RuntimeException {


    private static boolean iskorean = Locale.getDefault().getLanguage().equals("ko");


    private int index = -1;
    private int line = -1;

    public CSONParseException() {

    }

    public CSONParseException(String message) {
        super(message);

    }

    CSONParseException(String message, int line, int index) {
        super(makeMessage(message, line, index));
        this.index = index;
        this.line = line;
    }

    public CSONParseException(String message, int line, int index, Throwable cause) {
        super(makeMessage(message, line, index), cause);
        this.index = index;
        this.line = line;
    }

    private static String makeMessage(String message, int line, int index) {
        if(iskorean) {
            return message +  "  (" + line + "번째 줄, " + (index - 1) + "번째 위치)";
        }
        return message + " (at line " + line + ", position " + (index - 1) + ")";
    }

}
