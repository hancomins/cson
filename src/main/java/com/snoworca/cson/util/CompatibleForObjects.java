package com.snoworca.cson.util;


public class CompatibleForObjects {

    public static int checkFromIndexSize(int fromIndex, int size, int length) {

        return CompatibleForPreconditions.checkFromIndexSize(fromIndex, size, length, null);
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }
}
