package com.clipsoft.cson;

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

    private static boolean equalNumbers(Number number1, Number number2) {
        if(number1.getClass() == number2.getClass()) {
            return number1.equals(number2);
        }


        boolean isFloatNumber1 = number1 instanceof Float || number1 instanceof Double;
        boolean isFloatNumber2 = number2 instanceof Float || number2 instanceof Double;
        boolean isFloat = isFloatNumber1 || isFloatNumber2;
        boolean isIntegerNumber1 = number1 instanceof Integer || number1 instanceof Long;
        boolean isIntegerNumber2 = number2 instanceof Integer || number2 instanceof Long;
        boolean isInteger = isIntegerNumber1 || isIntegerNumber2;

        if(isFloat) {
            return number1.doubleValue() == number2.doubleValue();
        }
        else if(isInteger) return number1.longValue() == number2.longValue();






        if(number1 instanceof Double || number1 instanceof Float || number2 instanceof Double || number2 instanceof Float) {
            return number1.doubleValue() == number2.doubleValue();
        }
        return number1.longValue() == number2.longValue();
    }

}
