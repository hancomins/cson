package com.clipsoft.cson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

public class CSONElements {
    private CSONElements() {
    }

    public static boolean equals(CSONElement csonElement, CSONElement csonElement2) {
        if(csonElement instanceof CSONObject && csonElement2 instanceof CSONObject) {
            return equals((CSONObject)csonElement, (CSONObject)csonElement2);
        }
        return false;
    }


    private static boolean equals(CSONObject obj1, CSONObject obj2) {
        if(obj1.size() != obj2.size()) {
            return false;
        }
        for(String key : obj1.keySet()) {
            Object value = obj1.opt(key);
            Object value2 = obj2.opt(key);
        }
        return true;
    }

    private static boolean equals(Object value, Object value2) {
        if(value2 == value) {
            return true;
        }
        if(value2 == null) {
            return false;
        }
        if(value instanceof CSONElement) {
            return equals((CSONElement)value, (CSONElement)value2);
        }
        boolean isNumberValue1 = value instanceof Number;
        boolean isNumberValue2 = value2 instanceof Number;
        if(isNumberValue1 && isNumberValue2) {

        }


        return value.equals(value2);
    }

    private static boolean equals(Object value, Object value2) {
        if (value2 == value) {
            return true;
        }
        if (value2 == null || value == null) {
            return false;
        }
        if (value instanceof CSONElement) {
            return equals((CSONElement) value, (CSONElement) value2);
        }

        if (value instanceof Number && value2 instanceof Number) {
            return equals((Number) value, (Number) value2);
        }

        return value.equals(value2);
    }


    private static boolean equals(Number value, Number value2) {

        // 같은 타입이면 직접 비교
        if (value.getClass() == value2.getClass()) {
            return value.equals( value2);
        }

        // 정수 타입끼리 비교: longValue()로 빠르게 비교
        if (isIntegerType( value) && isIntegerType( value2)) {
            return ( value).longValue() == ( value2).longValue();
        }

        // 부동소수점 타입끼리 비교: doubleValue()로 빠르게 비교
        if (isFloatingPointType( value) && isFloatingPointType( value2)) {
            return Double.compare(( value).doubleValue(), ( value2).doubleValue()) == 0;
        }

        // 정밀도 중요한 경우에만 BigDecimal로 비교
        return toBigDecimal( value).compareTo(toBigDecimal( value2)) == 0;
    }

    private static boolean isIntegerType(Number number) {
        return number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte;
    }

    private static boolean isFloatingPointType(Number number) {
        return number instanceof Double || number instanceof Float;
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        } else {
            return BigDecimal.valueOf(number.doubleValue());
        }
    }

}
