package com.snoworca.cson.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class CompatibleForPreconditions {

    public static <X extends RuntimeException>
    int checkFromIndexSize(int fromIndex, int size, int length,
                           BiFunction<String, List<Integer>, X> oobef) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex)
            throw outOfBoundsCheckFromIndexSize(oobef, fromIndex, size, length);
        return fromIndex;
    }

    private static RuntimeException outOfBoundsCheckFromIndexSize(
            BiFunction<String, List<Integer>, ? extends RuntimeException> oobe,
            int fromIndex, int size, int length) {
        return outOfBounds(oobe, "checkFromIndexSize", fromIndex, size, length);
    }

    private static RuntimeException outOfBounds(
            BiFunction<String, List<Integer>, ? extends RuntimeException> oobef,
            String checkKind,
            Integer... args) {
        List<Integer> largs = Arrays.asList(args);
        RuntimeException e = oobef == null
                ? null : oobef.apply(checkKind, largs);
        return e == null
                ? new IndexOutOfBoundsException(outOfBoundsMessage(checkKind, largs)) : e;
    }



    private static String outOfBoundsMessage(String checkKind, List<Integer> args) {
        if (checkKind == null && args == null) {
            return String.format("Range check failed");
        } else if (checkKind == null) {
            return String.format("Range check failed: %s", args);
        } else if (args == null) {
            return String.format("Range check failed: %s", checkKind);
        }

        int argSize = 0;
        switch (checkKind) {
            case "checkIndex":
                argSize = 2;
                break;
            case "checkFromToIndex":
            case "checkFromIndexSize":
                argSize = 3;
                break;
            default:
        }

        switch ((args.size() != argSize) ? "" : checkKind) {
            case "checkIndex":
                return String.format("Index %s out of bounds for length %s",
                        args.get(0), args.get(1));
            case "checkFromToIndex":
                return String.format("Range [%s, %s) out of bounds for length %s",
                        args.get(0), args.get(1), args.get(2));
            case "checkFromIndexSize":
                return String.format("Range [%s, %<s + %s) out of bounds for length %s",
                        args.get(0), args.get(1), args.get(2));
            default:
                return String.format("Range check failed: %s %s", checkKind, args);
        }
    }
}
