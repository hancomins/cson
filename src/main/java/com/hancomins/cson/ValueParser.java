package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.NumberConversionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ValueParser {


    public static Object parse(CharacterBuffer characterBuffer, NumberConversionUtil.NumberConversionOption numberConversionOption, boolean onlyNumber) {

         int length = characterBuffer.length();


         char first = characterBuffer.charAt(0);


         if(isNumber(first)) {
             if(first == 0 && length > 1) {
                char second = characterBuffer.charAt(1);
                 if(second == 'x' || second == 'X') {
                     return parseHex(characterBuffer);
                 } else if(second == '.') {
                     // todo 실수형 전용 파싱
                     return parseRealNumber(-1,characterBuffer, onlyNumber);
                 } else if(numberConversionOption.isLeadingZeroOmission()) {
                     int start = 1;
                     for(start = 1; start < length; start++) {
                         char at = characterBuffer.charAt(start);
                         if(!isNumber(at)) {
                             return characterBuffer.toString();
                         } else if(at != '0') {
                             break;
                         }
                     }
                     // todo start 부터 length 까지의 문자열을 숫자로 변환
                    return parseNumber(start, characterBuffer, onlyNumber);
                 }
                 return characterBuffer.toString();
             }
            return parseNumber(0,characterBuffer, onlyNumber);
         } else if(length > 1) {
            char second = characterBuffer.charAt(1);
            if(isNumber(second)) {
                if(first == '+') {
                    return parseNumber(1, characterBuffer, onlyNumber);
                } else if(first == '-') {
                    return parseNumber(0, characterBuffer, onlyNumber);
                } else if(first == '.') {
                    // todo 실수형 전용 파싱
                    return parseNumber(-1,  characterBuffer, onlyNumber);
                }
            }
            String value = characterBuffer.toString();
            if(value.equalsIgnoreCase("true")) {
                return Boolean.TRUE;
            } else if(value.equalsIgnoreCase("false")) {
                return Boolean.FALSE;
            } else if(value.equalsIgnoreCase("null") || value.equalsIgnoreCase("undefined")) {
                return null;
            } else if(numberConversionOption.isAllowNaN() && value.equalsIgnoreCase("NaN")) {
                return Double.NaN;
            } else if(numberConversionOption.isAllowInfinity()) {
                if(value.equalsIgnoreCase("Infinity")) {
                    return Double.POSITIVE_INFINITY;
                } else if(value.equalsIgnoreCase("-Infinity")) {
                   return Double.NEGATIVE_INFINITY;
                } else if(numberConversionOption.isAllowPositiveSing() && value.equalsIgnoreCase("+Infinity")) {
                    return Float.POSITIVE_INFINITY;
                }
            }
            return value;
         }
         return characterBuffer.toString();
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }




    private static Object parseRealNumber(int start, CharacterBuffer characterBuffer, boolean onlyNumber) {
        if(start == -1) {
            String value = "0" + characterBuffer.toString();
            return new BigDecimal(value);
        }
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        return new BigDecimal(chars,start, length);
    }



    private static Object parseNumber(int start, CharacterBuffer characterBuffer, boolean onlyNumber) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        for(int i = start; i < length; i++) {
            char c = chars[i];
            if(c == '.' || c == 'e' || c == 'E') {
                return new BigDecimal(chars, start, characterBuffer.length());
            } else if(!isNumber(c)) {
                return characterBuffer.toString();
            }
        }

        String resultString = new String(chars, start, length);
        BigInteger bigInteger = new BigInteger(resultString);
        if(bigInteger.bitLength() <= 31){
            return bigInteger.intValue();
        } else if(bigInteger.bitLength() <= 63){
            return bigInteger.longValue();
        }
        return bigInteger;
    }

    private static Object parseHex(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        int start = 2;
        int end = length;
        if(length == 2) {
            return 0;
        }
        if(chars[length - 1] == 'L' || chars[length - 1] == 'l') {
            end--;
        }
        String hexString = new String(chars, start, end - start);
        BigInteger bigInteger = new BigInteger(hexString, 16);
        if(bigInteger.bitLength() <= 31){
            return bigInteger.intValue();
        } else if(bigInteger.bitLength() <= 63){
            return bigInteger.longValue();
        }
        return bigInteger;
    }






}
