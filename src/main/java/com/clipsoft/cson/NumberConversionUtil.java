package com.clipsoft.cson;

import com.clipsoft.cson.util.MockBigInteger;

import java.math.BigDecimal;
import java.math.BigInteger;

class NumberConversionUtil {


    static Number stringToNumber(char[] input, int offset, int len) throws NumberFormatException {

        int lastIndex = offset + len - 1;
        // trim
        while (offset < len && Character.isSpaceChar(input[offset])) {
            offset++;
            if (offset >= lastIndex) {
                throw new NumberFormatException("Zero lengthRefBigInteger");
            }
        }

        while(len > offset && Character.isSpaceChar(input[lastIndex])) {
            lastIndex--;
            if (lastIndex <= offset) {
                throw new NumberFormatException("Zero lengthRefBigInteger");
            }
        }
        len = lastIndex - offset + 1;
        int end = offset + len;

        if( (input[offset] == 'i' || input[offset] == 'I') || (len > 1 && (input[offset + 1] == 'i' || input[offset + 1] == 'I'))) {
            String value = new String(input, offset, len);
            if(value.equalsIgnoreCase("infinity")) {
                return Double.POSITIVE_INFINITY;
            } else if(value.equalsIgnoreCase("-infinity")) {
                return Double.NEGATIVE_INFINITY;
            }
        }


        boolean isHex  = false;
        if (input[0] == '.'){
            input = new char[input.length + 1];
            input[0] = '0';
            System.arraycopy(input, 0, input, 1, input.length - 1);
            offset = 0;
            len = input.length;
        }
        if (input.length > 2 && input[0] == '-' && input[1] == '.')  {
            input = new char[input.length + 1];
            input[0] = '-';
            input[1] = '0';
            System.arraycopy(input, 0, input, 2, input.length - 2);
            offset = 0;
            len = input.length;
        }
        if(input.length > 2 && input[0] == '0' && input[1] == 'x') {
            String val = new String(input, 2, input.length - 2);
            MockBigInteger bi = new MockBigInteger(val, 16);
            if(bi.bitLength() <= 31){
                return Integer.valueOf(bi.intValue());
            }
            if(bi.bitLength() <= 63){
                return Long.valueOf(bi.longValue());
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
            if (len == 1 && input[0] == '-') { return Double.valueOf(-0.0); }
            boolean negativeFirstChar = input[0] == '-';
            int counter = offset + (negativeFirstChar ? 1:0);

            while (counter < end) {
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
            if(counter == end) {
                input[offset] = '-';
                input[offset + 1] = '0';
                len = 2;
            }
            end = offset + len;

            initial = input[0];
            if(initial == '0' && end > 1) {
                char at1 = input[1];
                if(isNumericChar(at1)) {
                    throw new NumberFormatException("val ["+input+"] is not a valid number.");
                }
            } else if (initial == '-' && end > 2) {
                char at1 = input[1];
                char at2 = input[2];
                if(at1 == '0' && isNumericChar(at2)) {
                    throw new NumberFormatException("val ["+input+"] is not a valid number.");
                }
            }



            MockBigInteger bi = new MockBigInteger(input, offset, len);
            if(bi.bitLength() <= 31){
                return Integer.valueOf(bi.intValue());
            }
            if(bi.bitLength() <= 63){
                return Long.valueOf(bi.longValue());
            }
            return bi;
        }
        throw new NumberFormatException("val ["+input+"] is not a valid number.");
    }

    private static boolean isNumericChar(char c) {
        return (c <= '9' && c >= '0');
    }

    static boolean potentialNumber(String value){
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

}
