package com.hancomins.cson.util;


import java.math.BigDecimal;
import java.util.Arrays;

public class NumberConversionUtil {

    public static Number stringToNumber(String value, NumberConversionOption numberConversionOption) throws NumberFormatException {
        char[] input = value.toCharArray();
        return stringToNumber(input, 0, input.length,numberConversionOption);
    }

    public static Number stringToNumber(char[] input, int offset, int len, NumberConversionOption numberConversionOption) throws NumberFormatException {

        int lastIndex = offset + len - 1;
        // trim
        while (offset < len && Character.isWhitespace(input[offset])) {
            offset++;
            if (offset >= lastIndex) {
                throw new NumberFormatException("Zero lengthRefBigInteger");
            }
        }

        while(len > offset && Character.isWhitespace(input[lastIndex])) {
            lastIndex--;
            if (lastIndex <= offset) {
                throw new NumberFormatException("Zero lengthRefBigInteger");
            }
        }
        len = lastIndex - offset + 1;

        if(numberConversionOption.isAllowNaN() && len > 2 && (input[offset] == 'N' || input[offset] == 'n') &&
                (input[offset + 1] == 'A' || input[offset + 1] == 'a') &&
                (input[offset + 2] == 'N' || input[offset + 2] == 'n')) {
            return Double.NaN;
        }


        if(numberConversionOption.isAllowInfinity() && (input[offset] == 'i' || input[offset] == 'I') || (len > 1 && (input[offset + 1] == 'i' || input[offset + 1] == 'I'))) {
            String value = new String(input, offset, len);
            if(value.equalsIgnoreCase("infinity")) {
                return Double.POSITIVE_INFINITY;
            } else if(value.equalsIgnoreCase("-infinity")) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        boolean isLeadingZeroOmission = numberConversionOption.isLeadingZeroOmission();
        boolean isAllowPositiveSing = numberConversionOption.isAllowPositiveSing();
        boolean isAllowHexadecimal = numberConversionOption.isAllowHexadecimal();
        if (numberConversionOption.isLeadingZeroOmission() && input[offset] == '.'){
            char[] newInput = new char[len + 1];
            newInput[0] = '0';
            System.arraycopy(input, offset, newInput, 1, len);
            input = newInput;
            offset = 0;
            len = input.length;
        }
        else if (isLeadingZeroOmission && isAllowPositiveSing && len > 1 && input[offset] == '+' && input[offset + 1] == '.')  {
            input[offset] = '0';
        }
        else if (isLeadingZeroOmission && len > 1 && input[offset] == '-' && input[offset + 1] == '.')  {
            char[] newInput = new char[len + 1];
            newInput[0] = '-';
            newInput[1] = '0';
            System.arraycopy(input, offset + 1, newInput, 2, len - 1);
            input = newInput;
            offset = 0;
            len = input.length;
        }
        else if(isAllowHexadecimal && len > 2 && input[offset] == '0' && input[offset + 1] == 'x') {
            String val = new String(input, offset + 2, len - 2);
            MockBigInteger bi = new MockBigInteger(val, 16);
            if(bi.bitLength() <= 31){
                return bi.intValue();
            }
            if(bi.bitLength() <= 63){
                return bi.longValue();
            }
        }
        else if (isAllowPositiveSing && input[offset] == '+'){
            if(len == 1) {
                input[offset] = '0';
            } else {
                char[] newInput = new char[len - 1];
                System.arraycopy(input, offset + 1, newInput, 0, len - 1);
                input = newInput;
                offset = 0;
                len = input.length;
            }
        }


        char initial = input[offset];
        if ( isNumericChar(initial) || initial == '-' ) {
            // decimal representation
            if (isDecimalNotation(input, offset, len)) {
                // Use a BigDecimal all the time so we keep the original
                // representation. BigDecimal doesn't support -0.0, ensure we
                // keep that by forcing a decimal.
                try {
                    BigDecimal bd = new BigDecimal(input, offset, len);
                    if(initial == '-' && BigDecimal.ZERO.compareTo(bd)==0) {
                        return Double.valueOf(-0.0);
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    // this is to support "Hex Floats" like this: 0x1.0P-1074
                    try {
                        Double d = Double.valueOf(new String(input, offset, len));
                        if(d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val ["+input+"] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val ["+input+"] is not a valid number.");
                    }
                }
            }
            // remove Leading Zeros Of Number
            if (len == 1 && input[offset] == '-') { //noinspection UnnecessaryBoxing
                return Double.valueOf(-0.0); }
            boolean negativeFirstChar = input[offset] == '-';
            int counter = offset + (negativeFirstChar ? 1:0);

            while (counter < len) {
                if (input[counter] != '0'){
                    offset = counter;
                    if(negativeFirstChar) {
                        offset--;
                        input[offset] = '-';
                    }
                    break;
                }
                counter++;
            }


            if(counter == len && len != 1) {
                input[offset] = '-';
                input[offset + 1] = '0';
                len = 2;
            }

            initial = input[offset];
            if(initial == '0' && len > 1) {
                char at1 = input[offset + 1];
                if(isNumericChar(at1)) {
                    throw new NumberFormatException("val ["+ Arrays.toString(input) +"] is not a valid number.");
                }
            } else if (initial == '-' && len > 2) {
                char at1 = input[offset +1];
                char at2 = input[offset +2];
                if(at1 == '0' && isNumericChar(at2)) {
                    throw new NumberFormatException("val ["+ Arrays.toString(input) +"] is not a valid number.");
                }
            }



            MockBigInteger bi = new MockBigInteger(input, offset, len);
            if(bi.bitLength() <= 31){
                return bi.intValue();
            }
            if(bi.bitLength() <= 63){
                return bi.longValue();
            }
            return bi.toBigInteger();
        } else if(numberConversionOption.isIgnoreNonNumeric()) {
            return null;
        }
        throw new NumberFormatException("val ["+ Arrays.toString(input) +"] is not a valid number.");
    }

    private static boolean isNumericChar(char c) {
        return (c <= '9' && c >= '0');
    }

    @SuppressWarnings("unused")
    public static boolean potentialNumber(String value){
        if (value == null || value.isEmpty()){
            return false;
        }
        return potentialPositiveNumberStartingAtIndex(value, (value.charAt(0)=='-'?1:0));
    }

    private static boolean isDecimalNotation(final char[] val, int offset, int len) {
        return  contains(val,offset, len, '.') || contains(val,offset, len, 'e')
                || contains(val,offset, len, 'E')|| (len == 2 && val[offset] == '-' && val[offset + 1] == '0');
    }

    private static boolean contains(char[] input, int offset,int len,  char c) {
        int lastIndex = offset + len;
        for (int i = offset; i < lastIndex; i++) {
            if (input[i] == c) {
                return true;
            }
        }
        return false;
    }

    private static boolean potentialPositiveNumberStartingAtIndex(String value,int index){
        if (index >= value.length()){
            return false;
        }
        return digitAtIndex(value, (value.charAt(index)=='.'?index+1:index));
    }

    private static boolean digitAtIndex(String value, int index){
        if (index >= value.length()){
            return false;
        }
        return value.charAt(index) >= '0' && value.charAt(index) <= '9';
    }

    public static final NumberConversionOption DEFAULT_NUMBER_CONVERSION_OPTION = new NumberConversionOption() {
        @Override
        public boolean isAllowNaN() {
            return true;
        }

        @Override
        public boolean isAllowInfinity() {
            return true;
        }

        @Override
        public boolean isAllowHexadecimal() {
            return true;
        }

        @Override
        public boolean isLeadingZeroOmission() {
            return true;
        }

        @Override
        public boolean isAllowPositiveSing() {
            return true;
        }

        @Override
        public boolean isIgnoreNonNumeric() {
            return true;
        }
    };

    public static interface NumberConversionOption {
        public boolean isAllowNaN();
        public boolean isAllowInfinity();
        public boolean isAllowHexadecimal();
        public boolean isLeadingZeroOmission();
        public boolean isAllowPositiveSing();
        public boolean isIgnoreNonNumeric();

    }

    public static interface MutableNumberConversionOption<T> extends NumberConversionOption {


            public  T setAllowNaN(boolean enable);
            public  T  setAllowInfinity(boolean enable);
            public  T  setAllowHexadecimal(boolean enable);
            public  T  setLeadingZeroOmission(boolean enable);
            public  T  setAllowPositiveSing(boolean enable);
            public  T  setIgnoreNonNumeric(boolean enable);
    }

}
