package com.hancomins.cson.util;

public class EscapeUtil {

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static String escapeJSONString(String str, boolean allowLineBreak, char quote) {
        if(str == null) return  null;
        boolean isSpacialQuote = quote != 0 && quote != '"';
        boolean isEmptyQuote = quote == 0;
        char[] charArray = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        char lastCh = 0;
        boolean isLastLF = false;
        for(int i = 0; i < charArray.length; ++i) {
            char ch = charArray[i];
            if (ch == '\n') {
                if(allowLineBreak && lastCh == '\\') {
                    builder.append('\n');
                    isLastLF = true;
                } else {
                    builder.append("\\n");
                }
            } else if (ch == '\r') {
                if(allowLineBreak && isLastLF) {
                    builder.append('\r');
                    isLastLF = false;
                } else {
                    builder.append("\\r");
                }
            } else if (ch == '\f') {
                isLastLF = false;
                builder.append("\\f");
            } else if (ch == '\t') {
                isLastLF = false;
                builder.append("\\t");
            } else if (ch == '\b') {
                isLastLF = false;
                builder.append("\\b");
            } else if (ch == '"' && !isSpacialQuote) {
                isLastLF = false;
                builder.append("\\\"");
            } else if (ch == '\\') {
                isLastLF = false;
                builder.append("\\\\");
            } else if(!isEmptyQuote && ch == quote) {
                isLastLF = false;
                builder.append("\\").append(ch);
            } else if(isEmptyQuote && ch == '\'') {
                isLastLF = false;
                builder.append("\\'");
            }
            else {
                isLastLF = false;
                builder.append(ch);
            }
            lastCh = ch;
        }
        return builder.toString();

    }

}
