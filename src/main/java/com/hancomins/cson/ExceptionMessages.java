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



}
