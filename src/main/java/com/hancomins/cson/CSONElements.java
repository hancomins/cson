package com.hancomins.cson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class CSONElements {

    private final static String BASE64_PREFIX = "base64,";

    private CSONElements() {
    }

    public static boolean equalsIgnoreTypes(CSONElement csonElement, CSONElement csonElement2) {
        return equals(csonElement, csonElement2, true);
    }

    public static boolean equals(CSONElement csonElement, CSONElement csonElement2) {
        return equals(csonElement, csonElement2, false);
    }

    private static boolean equals(CSONElement csonElement, CSONElement csonElement2, boolean ignoreTypes) {
        if(csonElement == csonElement2) {
            return true;
        }
        if(csonElement instanceof CSONObject && csonElement2 instanceof CSONObject) {
            return equals((CSONObject)csonElement, (CSONObject)csonElement2, ignoreTypes);
        }  else if(csonElement instanceof CSONArray && csonElement2 instanceof CSONArray) {
            return equals((CSONArray)csonElement, (CSONArray)csonElement2, ignoreTypes);
        }
        return false;
    }

    public static boolean equals(CSONObject obj1, CSONObject obj2) {
        return equals(obj1, obj2, false);
    }

    public static boolean equalsIgnoreTypes(CSONObject obj1, CSONObject obj2) {
        return equals(obj1, obj2, true);
    }

     private static boolean equals(CSONObject obj1, CSONObject obj2, boolean ignoreTypes) {
        if(obj1.size() != obj2.size()) {
            return false;
        }
        for(String key : obj1.keySet()) {
            Object value = obj1.opt(key);
            Object value2 = obj2.opt(key);
           if(!equalsObject(value, value2, ignoreTypes)) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(CSONArray array1, CSONArray array2) {
        return equals(array1, array2, false);
    }

    public static boolean equalsIgnoreTypes(CSONArray array1, CSONArray array2) {
        return equals(array1, array2, true);
    }


    private static boolean equals(CSONArray array1, CSONArray array2, boolean ignoreTypes) {
        if(array1.size() != array2.size()) {
            return false;
        }
        for(int i = 0; i < array1.size(); i++) {
            Object value = array1.opt(i);
            Object value2 = array2.opt(i);
            if(!equalsObject(value, value2,ignoreTypes)) {
                return false;
            }
        }
        return true;
    }





    private static boolean equalsObject(Object value1, Object value2, boolean ignoreType) {
        if (value2 == value1) {
            return true;
        }
        if (value2 == null || value1 == null) {
            if(ignoreType) {
                //noinspection DuplicateExpressions
                return String.valueOf(value1).equals(String.valueOf(value2));
            }
            return false;
        }
        if (value1 instanceof CSONElement && value2 instanceof CSONElement) {
            return equals((CSONElement) value1, (CSONElement) value2, ignoreType);
        }



        boolean isNumberValue1 = value1 instanceof Number;
        boolean isNumberValue2 = value2 instanceof Number;
        boolean isByteArrayValue1 = value1 instanceof byte[];
        boolean isByteArrayValue2 = value2 instanceof byte[];
        boolean isStringValue1 = value1 instanceof String;
        boolean isStringValue2 = value2 instanceof String;

        if(value1 instanceof Character || value2 instanceof Character) {
            if(isNumberValue1) {
                value2 = (int)(char)value2;
                isNumberValue2 = true;
            }
            else if(isNumberValue2) {
                value1 = (int)(char)value1;
                isNumberValue1 = true;
            }
            else if(isStringValue1) {
                value2 = String.valueOf(value2);
            }
            else if(isStringValue2) {
                value1 = String.valueOf(value1);
            }
        }


        else if (isNumberValue1 && isNumberValue2) {
            return equalsNumber((Number) value1, (Number) value2);
        }
        else if(isByteArrayValue1 && isByteArrayValue2) {
            return Arrays.equals((byte[])value1, (byte[])value2);
        }
        else if(isByteArrayValue1 && isStringValue2 && ((String) value2).startsWith(BASE64_PREFIX)) {
            String base64OfValue1 = Base64.getEncoder().encodeToString((byte[]) value1);
            return base64OfValue1.equals(((String) value2).substring(BASE64_PREFIX.length()));
        } else if(isByteArrayValue2 && isStringValue1 && ((String) value1).startsWith(BASE64_PREFIX)) {
            String base64OfValue2 = Base64.getEncoder().encodeToString((byte[]) value2);
            return base64OfValue2.equals(((String) value1).substring(BASE64_PREFIX.length()));
        }
        else if(ignoreType) {
            if (isNumberValue1 || isNumberValue2) {
                return String.valueOf(value1).equals(String.valueOf(value2));
            } else if (isByteArrayValue1 && value2 instanceof String) {
                String base64 = Base64.getEncoder().encodeToString((byte[]) value1);
                return base64.equals(value2);
            } else if (isByteArrayValue2 && value1 instanceof String) {
                String base64 = Base64.getEncoder().encodeToString((byte[]) value2);
                return base64.equals(value1);
            }
        }
        return Objects.equals(value1, value2);
    }

    enum NumberType {
        INTEGER, FLOATING_POINT, BIG_INTEGER, BIG_DECIMAL, UNKNOWN;

        static NumberType of(Number value) {
            if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
                return INTEGER;
            }
            if (value instanceof Float || value instanceof Double) {
                return FLOATING_POINT;
            }
            if (value instanceof BigInteger) {
                return BIG_INTEGER;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            return UNKNOWN;
        }
    }


    static boolean equalsNumber(Number value, Number value2) {
        // 같은 타입이면 직접 비교
        if (value.getClass() == value2.getClass()) {
            return value.equals( value2);
        }

        NumberType typeOfValue1 = NumberType.of( value);
        NumberType typeOfValue2 = NumberType.of( value2);

        if(typeOfValue1 == NumberType.BIG_INTEGER || typeOfValue2 == NumberType.BIG_INTEGER) {
            if(typeOfValue1 == NumberType.BIG_DECIMAL || typeOfValue2 == NumberType.BIG_DECIMAL ||
                    typeOfValue1 == NumberType.FLOATING_POINT || typeOfValue2 == NumberType.FLOATING_POINT) {
                return toBigDecimal(value).compareTo(toBigDecimal( value2)) == 0;
            } else {
                return toBigInteger(value).equals(toBigInteger( value2));
            }
        } else if(typeOfValue1 == NumberType.BIG_DECIMAL || typeOfValue2 == NumberType.BIG_DECIMAL) {
            return toBigDecimal(value).compareTo(toBigDecimal( value2)) == 0;
        }
        if (typeOfValue1 == NumberType.INTEGER && typeOfValue2 == NumberType.INTEGER) {
            return ( value).longValue() == ( value2).longValue();
        }
        if (typeOfValue1 == NumberType.FLOATING_POINT || typeOfValue2 == NumberType.FLOATING_POINT) {
            return Double.compare(( value).doubleValue(), ( value2).doubleValue()) == 0;
        }


        // 정밀도 중요한 경우에만 BigDecimal로 비교
        return toBigDecimal(value).compareTo(toBigDecimal( value2)) == 0;
    }

    private static boolean isIntegerType(Number number) {
        return number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte;
    }

    private static boolean isFloatingPointType(Number number) {
        return number instanceof Double || number instanceof Float;
    }

    private static BigInteger toBigInteger(Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof BigDecimal) {
            return ((BigDecimal) number).toBigInteger();
        } else if(number instanceof Long) {
            return BigInteger.valueOf(number.longValue());
        } else if(number instanceof Integer) {
            return BigInteger.valueOf(number.intValue());
        } else if(number instanceof Short) {
            return BigInteger.valueOf(number.shortValue());
        } else if(number instanceof Byte) {
            return BigInteger.valueOf(number.byteValue());
        } else if(number instanceof Double) {
            return BigDecimal.valueOf(number.doubleValue()).toBigInteger();
        } else if(number instanceof Float) {
            return BigDecimal.valueOf(number.floatValue()).toBigInteger();
        } else {
            return BigDecimal.valueOf(number.doubleValue()).toBigInteger();
        }
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        } else if(number instanceof Long) {
            return BigDecimal.valueOf(number.longValue());
        } else if(number instanceof Integer) {
            return BigDecimal.valueOf(number.intValue());
        } else if(number instanceof Short) {
            return BigDecimal.valueOf(number.shortValue());
        } else if(number instanceof Byte) {
            return BigDecimal.valueOf(number.byteValue());
        } else {
            return new BigDecimal(number.toString());
        }
    }

}
