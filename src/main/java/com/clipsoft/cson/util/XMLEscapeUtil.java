package com.clipsoft.cson.util;

public class XMLEscapeUtil {

    public static String escape(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            switch (ch) {
                case '<':
                    stringBuilder.append("&lt;");
                    break;
                case '>':
                    stringBuilder.append("&gt;");
                    break;
                case '&':
                    stringBuilder.append("&amp;");
                    break;
                case '"':
                    stringBuilder.append("&quot;");
                    break;
                case '\'':
                    stringBuilder.append("&apos;");
                    break;
                default:
                    stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }

    public static String unescape(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            switch (ch) {
                case '&':
                    if (str.startsWith("&lt;", i)) {
                        stringBuilder.append("<");
                        i += 3;
                    } else if (str.startsWith("&gt;", i)) {
                        stringBuilder.append(">");
                        i += 3;
                    } else if (str.startsWith("&amp;", i)) {
                        stringBuilder.append("&");
                        i += 4;
                    } else if (str.startsWith("&quot;", i)) {
                        stringBuilder.append("\"");
                        i += 5;
                    } else if (str.startsWith("&apos;", i)) {
                        stringBuilder.append("'");
                        i += 5;
                    } else {
                        stringBuilder.append(ch);
                    }
                    break;
                default:
                    stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }
}
