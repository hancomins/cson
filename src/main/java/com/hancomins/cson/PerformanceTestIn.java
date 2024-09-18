package com.hancomins.cson;

import java.util.Random;

public class PerformanceTestIn {
    // 방법 1: if-else 방식
    public static boolean ifElseTest(char c) {
        if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
            return true;
        } else {
            return false;
        }
    }

    // 방법 2: switch-case 방식
    public static boolean switchTest(char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                return true;
            default:
                return false;
        }
    }

    // 방법 3: indexOf 방식
    public static boolean indexOfTest(char c) {
        return "\n\r\t ".indexOf(c) > -1;
    }

    // 시간 측정 함수
    public static long measureTime(Runnable testFunc, int iterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            testFunc.run();
        }
        return System.nanoTime() - startTime;
    }

    public static void main(String[] args) {
        int iterations = 1000000;
        Random random = new Random();
        char[] testChars = { '\n', '\r', '\t', ' ', 'a', 'b', 'z', '1' };

        // 랜덤한 문자 선택
        char testChar = testChars[random.nextInt(testChars.length)];
        System.out.println("테스트 문자: " + testChar);

        // if-else 성능 측정
        measureTime(() -> ifElseTest(testChar), iterations);


        // switch-case 성능 측정
        measureTime(() -> switchTest(testChar), iterations);


        // indexOf 성능 측정
        measureTime(() -> indexOfTest(testChar), iterations);



        // if-else 성능 측정
        long ifElseTime = measureTime(() -> ifElseTest(testChar), iterations);
        System.out.println("if-else 걸린 시간: " + ifElseTime / 1_000_000.0 + " ms");

        // switch-case 성능 측정
        long switchTime = measureTime(() -> switchTest(testChar), iterations);
        System.out.println("switch-case 걸린 시간: " + switchTime / 1_000_000.0 + " ms");

        // indexOf 성능 측정
        long indexOfTime = measureTime(() -> indexOfTest(testChar), iterations);
        System.out.println("indexOf 걸린 시간: " + indexOfTime / 1_000_000.0 + " ms");
    }
}
