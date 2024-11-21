package com.hancomins.cson;

import java.util.Locale;

public enum ErrorMessage {

    CONFLICT_KEY_VALUE_TYPE("Conflict detected for key '%s': Value is defined as both an object and a primitive value.", "키 'a'에 대한 충돌이 감지되었습니다: 값이 객체와 기본 값으로 동시에 정의되었습니다.");

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


    final String[] messages;



    ErrorMessage(String ...messages) {
        this.messages = messages;

    }



    public String formatMessage(Object... args) {
        int index = localeIndex;
        if(messages.length <= index) {
            index = 0;
        }
        String localeMessage = messages[index];
        return String.format(localeMessage, args);
    }
}
