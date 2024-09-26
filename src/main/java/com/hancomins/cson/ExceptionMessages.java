package com.hancomins.cson;

import java.util.Locale;

public class ExceptionMessages {
    private static final int LOCALE_INDEX_EN = 0;
    private static final int LOCALE_INDEX_KO = 1;

    private static int localeIndex = 0;

    static {
        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("ko")) {
            localeIndex = LOCALE_INDEX_KO;
        } else {
            localeIndex = LOCALE_INDEX_EN;
        }
    }


    private static final String[] OBJECT_VALUE_CONVERT_ERROR = {"CSONObject['%s'] value is '%s' and cannot be converted to a %s type.", "CSONObject['%s'] 값이 '%s'이며 %s 타입으로 변환할 수 없습니다."};
    private static final String[] ARRAY_VALUE_CONVERT_ERROR = {"CSONArray[%d] value is '%s' and cannot be converted to a %s type.", "CSONArray[%d] 값이 '%s'이며 %s 타입으로 변환할 수 없습니다."};
    private static final String[] ARRAY_INDEX_OUT_OF_BOUNDS = {"CSONArray[%d] is out of bounds. The size of the CSONArray is %d.", "CSONArray[%d]가 범위를 벗어났습니다. CSONArray의 크기는 %d입니다."};
    private static final String[] OBJECT_KEY_NOT_FOUND = {"CSONObject['%s'] is not found.", "CSONObject['%s']가 없습니다."};
    private static final String[] CTRL_CHAR_NOT_ALLOWED = {"Control character(%s) is not allowed.", "제어 문자(%s)가 허용되지 않습니다."};


    private static final String[] KEY_NOT_FOUND = {"Key not found in JSON string at line %d, position %d.", "문자열의 %d번째 줄 %d번째 위치에서 키를 찾을 수 없습니다."};


    public static final String[] STRING_READ_ERROR = {"An error occurred while reading the string.", "문자열을 읽는중 에러가 발생하였습니다."};

    // { 또는 [ 를 찾을 수 없습니다.
    public static final String[] JSON5_BRACKET_NOT_FOUND = {"Cannot find '{' or '[' in JSON string.", " '{' 또는 '['를 찾을 수 없습니다."};

    public static final String[] END_OF_STREAM = {"Unexpected end of stream", "예상치 못한 스트림의 끝입니다."};

    public static final String[] UNEXPECTED_TOKEN_LONG = {"Unexpected token.('%c') One of \"%s\" is expected.", "예상치 못한 토큰. ('%c') 다음 \"%s\" 중에 하나가 와야합니다."};

    public static final String[] UNEXPECTED_TOKEN = {"Unexpected token '%c'.", "예상치 못한 토큰 '%c'." };

    static String formatMessage(String[] message, Object... args) {
        String localeMessage = message[localeIndex];
        return String.format(localeMessage, args);
    }


    static String getCSONObjectValueConvertError(String key, Object value, String type) {
        return String.format(OBJECT_VALUE_CONVERT_ERROR[localeIndex], key, value, type);
    }

    static String getCSONArrayValueConvertError(int index, Object value, String type) {
        return String.format(ARRAY_VALUE_CONVERT_ERROR[localeIndex], index, value, type);
    }

    static String getCSONArrayIndexOutOfBounds(int index, int size) {
        return String.format(ARRAY_INDEX_OUT_OF_BOUNDS[localeIndex], index, size);
    }

    static String getCSONObjectKeyNotFound(String key) {
        return String.format(OBJECT_KEY_NOT_FOUND[localeIndex], key);
    }

    static String getKeyNotFound(int line, int position) {
        return String.format(KEY_NOT_FOUND[localeIndex], line, position);
    }

    static String getCtrlCharNotAllowed(char c) {
        // 컨트롤 문자를 String 표현으로 처리. 예를 들어 \n 은 \\n 으로 표시
        String ctrl = c == '\n' ? "\\n" : c == '\r' ? "\\r" : c == '\t' ? "\\t" : c == '\b' ? "\\b" : c == '\f' ? "\\f" : c == '\u000B' ? "\\v" : "\\x" + Integer.toHexString(c);
        return String.format(CTRL_CHAR_NOT_ALLOWED[localeIndex], ctrl);
    }



}
