package com.snoworca.cson.util;


import java.io.*;
import java.util.Arrays;

public class MockBigInteger extends Number implements Comparable<MockBigInteger> {

    static final long INFLATED = Long.MIN_VALUE;
    final int signum;
    final int[] mag;


    private int bitLengthPlusOne;

    private int lowestSetBitPlusTwo;
    private int firstNonzeroIntNumPlusTwo;


    public static final int     EXP_BIAS        = 127;

    public static final int     SIGN_BIT_MASK   = 0x80000000;

    public static final int     SIGNIF_BIT_MASK = 0x007FFFFF;
    static final long LONG_MASK = 0xffffffffL;
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1; // (1 << 26)
    private static final  int PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000;
    private static final int KARATSUBA_THRESHOLD = 80;
    private static final int TOOM_COOK_THRESHOLD = 240;
    private static final int KARATSUBA_SQUARE_THRESHOLD = 128;
    private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;
    static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;
    static final int BURNIKEL_ZIEGLER_OFFSET = 40;
    private static final int SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20;
    private static final int MULTIPLY_SQUARE_THRESHOLD = 20;
    private static final int MONTGOMERY_INTRINSIC_THRESHOLD = 512;
    public static final int SIGNIFICAND_WIDTH   = 24;


    private MockBigInteger(int signum, int[] magnitude) {
        this.mag = stripLeadingZeroInts(magnitude);

        if (signum < -1 || signum > 1)
            throw(new NumberFormatException("Invalid signum value"));

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw(new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    public MockBigInteger(String val, int radix) {
        int cursor = 0, numDigits;
        final int len = val.length();

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new NumberFormatException("Radix out of range");
        if (len == 0)
            throw new NumberFormatException("Zero lengthRefBigInteger");

        int sign = 1;
        int index1 = val.lastIndexOf('-');
        int index2 = val.lastIndexOf('+');
        if (index1 >= 0) {
            if (index1 != 0 || index2 >= 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            sign = -1;
            cursor = 1;
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            cursor = 1;
        }
        if (cursor == len)
            throw new NumberFormatException("Zero lengthRefBigInteger");

        while (cursor < len &&
                Character.digit(val.charAt(cursor), radix) == 0) {
            cursor++;
        }

        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }

        numDigits = len - cursor;
        signum = sign;

        long numBits = ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
        if (numBits + 31 >= (1L << 32)) {
            reportOverflow();
        }
        int numWords = (int) (numBits + 31) >>> 5;
        int[] magnitude = new int[numWords];

        int firstGroupLen = numDigits % digitsPerInt[radix];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[radix];
        String group = val.substring(cursor, cursor += firstGroupLen);
        magnitude[numWords - 1] = Integer.parseInt(group, radix);
        if (magnitude[numWords - 1] < 0)
            throw new NumberFormatException("Illegal digit");

        int superRadix = intRadix[radix];
        int groupVal = 0;
        while (cursor < len) {
            group = val.substring(cursor, cursor += digitsPerInt[radix]);
            groupVal = Integer.parseInt(group, radix);
            if (groupVal < 0)
                throw new NumberFormatException("Illegal digit");
            destructiveMulAdd(magnitude, superRadix, groupVal);
        }

        mag = trustedStripLeadingZeroInts(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }


    static int lastIndexOf(char[] val, int offset, int len, int ch) {
        int i = offset + len - 1;
        while(i >= offset && val[i] != ch) {
            i--;
        }
        return i;
    }

    public MockBigInteger(char[] val, int offset, int len, int radix) {
        int cursor = 0, numDigits;
        int lastIndex = offset + len - 1;

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new NumberFormatException("Radix out of range");
        if (len == 0)
            throw new NumberFormatException("Zero lengthRefBigInteger");

        while (offset < len && Character.isSpaceChar(val[offset])) {
            offset++;
            if (offset >= lastIndex) {
                throw new NumberFormatException("Zero lengthRefBigInteger");
            }
        }

        while(len > offset && Character.isSpaceChar(val[lastIndex])) {
            lastIndex--;
            if (lastIndex <= offset) {
                throw new NumberFormatException("Zero lengthRefBigInteger");
            }
        }

        len = lastIndex - offset + 1;





        int sign = 1;

        int index1 = lastIndexOf(val, offset, len, '-');
        int index2 = lastIndexOf(val, offset, len, '+');
        if (index1 >= 0) {
            if (index1 != 0 || index2 >= 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            sign = -1;
            cursor = 1;
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            cursor = 1;
        }
        if (cursor == len)
            throw new NumberFormatException("Zero lengthRefBigInteger");

        while (cursor < len && Character.digit(val[cursor], radix) == 0) {
            cursor++;
        }

        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }

        numDigits = len - cursor;
        signum = sign;

        long numBits = ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
        if (numBits + 31 >= (1L << 32)) {
            reportOverflow();
        }
        int numWords = (int) (numBits + 31) >>> 5;
        int[] magnitude = new int[numWords];

        int firstGroupLen = numDigits % digitsPerInt[radix];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[radix];

        magnitude[numWords - 1] = parseInt(val, cursor, firstGroupLen, radix);
        cursor += digitsPerInt[radix];
        if (magnitude[numWords - 1] < 0)
            throw new NumberFormatException("Illegal digit");

        int superRadix = intRadix[radix];
        int groupVal = 0;
        while (cursor < len) {
            groupVal = parseInt(val, cursor, firstGroupLen, radix); //Integer.parseInt(group, radix);
            cursor += digitsPerInt[radix];
            if (groupVal < 0)
                throw new NumberFormatException("Illegal digit");
            destructiveMulAdd(magnitude, superRadix, groupVal);
        }
        mag = trustedStripLeadingZeroInts(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    public static int parseInt(char[] s, int beginIndex, int len, int radix)
            throws NumberFormatException {
        s = java.util.Objects.requireNonNull(s);

        int endIndex = beginIndex + len;

        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length) {
            throw new IndexOutOfBoundsException();
        }
        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        boolean negative = false;
        int i = beginIndex;
        int limit = -Integer.MAX_VALUE;

        if (i < endIndex) {
            char firstChar = s[i];
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw forCharSequence(s, beginIndex, endIndex, i);
                }
                i++;
                if (i == endIndex) { // Cannot have lone "+" or "-"
                    throw forCharSequence(s, beginIndex, endIndex, i);
                }
            }
            int multmin = limit / radix;
            int result = 0;
            while (i < endIndex) {

                int digit = Character.digit(s[i], radix);
                if (digit < 0 || result < multmin) {
                    throw forCharSequence(s, beginIndex, endIndex, i);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw forCharSequence(s, beginIndex, endIndex, i);
                }
                i++;
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            throw forInputString("");
        }
    }

    static NumberFormatException forCharSequence(char[] charArray,
                                                 int beginIndex, int endIndex, int errorIndex) {
        String s = new String(charArray, beginIndex, endIndex - beginIndex);
        return new NumberFormatException("Error at index "
                + (errorIndex - beginIndex) + " in: \""
                + s.subSequence(beginIndex, endIndex) + "\"");
    }






    static NumberFormatException forInputString(String s) {
        return new NumberFormatException("For input string: \"" + s + "\"");
    }


    private static long bitsPerDigit[] = { 0, 0,
            1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672,
            3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633,
            4696, 4756, 4814, 4870, 4923, 4975, 5025, 5074, 5120, 5166, 5210,
            5253, 5295};

    private static void destructiveMulAdd(int[] x, int y, int z) {

        long ylong = y & LONG_MASK;
        long zlong = z & LONG_MASK;
        int len = x.length;

        long product = 0;
        long carry = 0;
        for (int i = len-1; i >= 0; i--) {
            product = ylong * (x[i] & LONG_MASK) + carry;
            x[i] = (int)product;
            carry = product >>> 32;
        }

        long sum = (x[len-1] & LONG_MASK) + zlong;
        x[len-1] = (int)sum;
        carry = sum >>> 32;
        for (int i = len-2; i >= 0; i--) {
            sum = (x[i] & LONG_MASK) + carry;
            x[i] = (int)sum;
            carry = sum >>> 32;
        }
    }



    private static final int SMALL_PRIME_THRESHOLD = 95;

    private static final int DEFAULT_PRIME_CERTAINTY = 100;

    private static final MockBigInteger SMALL_PRIME_PRODUCT
            = valueOf(3L*5*7*11*13*17*19*23*29*31*37*41);







   MockBigInteger(int[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = magnitude;
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }



    private void checkRange() {
        if (mag.length > MAX_MAG_LENGTH || mag.length == MAX_MAG_LENGTH && mag[0] < 0) {
            reportOverflow();
        }
    }

    private static void reportOverflow() {
        throw new ArithmeticException("MockBigInteger would overflow supported range");
    }

    @SuppressWarnings("DataFlowIssue")
    public static MockBigInteger valueOf(long val) {

        if (val == 0)
            return ZERO;
        if (val > 0 && val <= MAX_CONSTANT)
            return posConst[(int) val];
        else if (val < 0 && val >= -MAX_CONSTANT)
            return negConst[(int) -val];

        return new MockBigInteger(val);
    }

    private MockBigInteger(long val) {
        if (val < 0) {
            val = -val;
            signum = -1;
        } else {
            signum = 1;
        }

        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            mag = new int[1];
            mag[0] = (int)val;
        } else {
            mag = new int[2];
            mag[0] = highWord;
            mag[1] = (int)val;
        }
    }

    private static final int MAX_CONSTANT = 16;
    private static final MockBigInteger[] posConst = new MockBigInteger[MAX_CONSTANT+1];
    private static final MockBigInteger[] negConst = new MockBigInteger[MAX_CONSTANT+1];

    private static volatile MockBigInteger[][] powerCache;

    private static final double[] logCache;

    private static final double LOG_TWO = Math.log(2.0);

    static {
        assert 0 < KARATSUBA_THRESHOLD
                && KARATSUBA_THRESHOLD < TOOM_COOK_THRESHOLD
                && TOOM_COOK_THRESHOLD < Integer.MAX_VALUE
                && 0 < KARATSUBA_SQUARE_THRESHOLD
                && KARATSUBA_SQUARE_THRESHOLD < TOOM_COOK_SQUARE_THRESHOLD
                && TOOM_COOK_SQUARE_THRESHOLD < Integer.MAX_VALUE :
                "Algorithm thresholds are inconsistent";

        for (int i = 1; i <= MAX_CONSTANT; i++) {
            int[] magnitude = new int[1];
            magnitude[0] = i;
            posConst[i] = new MockBigInteger(magnitude,  1);
            negConst[i] = new MockBigInteger(magnitude, -1);
        }

        powerCache = new MockBigInteger[Character.MAX_RADIX+1][];
        logCache = new double[Character.MAX_RADIX+1];

        for (int i=Character.MIN_RADIX; i <= Character.MAX_RADIX; i++) {
            powerCache[i] = new MockBigInteger[] {com.snoworca.cson.util.MockBigInteger.valueOf(i) };
            logCache[i] = Math.log(i);
        }
    }

    public static final MockBigInteger ZERO = new MockBigInteger(new int[0], 0);
    public static final MockBigInteger ONE = valueOf(1);

    private static final MockBigInteger NEGATIVE_ONE = valueOf(-1);


    public MockBigInteger add(MockBigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val;
        if (val.signum == signum)
            return new MockBigInteger(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);

        return new MockBigInteger(resultMag, cmp == signum ? 1 : -1);
    }


    private static int[] add(int[] x, int[] y) {
        if (x.length < y.length) {
            int[] tmp = x;
            x = y;
            y = tmp;
        }

        int xIndex = x.length;
        int yIndex = y.length;
        int result[] = new int[xIndex];
        long sum = 0;
        if (yIndex == 1) {
            sum = (x[--xIndex] & LONG_MASK) + (y[0] & LONG_MASK) ;
            result[xIndex] = (int)sum;
        } else {
            while (yIndex > 0) {
                sum = (x[--xIndex] & LONG_MASK) +
                        (y[--yIndex] & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int)sum;
            }
        }
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);

        while (xIndex > 0)
            result[--xIndex] = x[xIndex];

        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    private MockBigInteger subtract(MockBigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val.negate();
        if (val.signum != signum)
            return new MockBigInteger(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new MockBigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    private static int[] subtract(int[] big, int[] little) {
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        int littleIndex = little.length;
        long difference = 0;

        while (littleIndex > 0) {
            difference = (big[--bigIndex] & LONG_MASK) -
                    (little[--littleIndex] & LONG_MASK) +
                    (difference >> 32);
            result[bigIndex] = (int)difference;
        }

        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    public MockBigInteger multiply(MockBigInteger val) {
        return multiply(val, false);
    }

    private MockBigInteger multiply(MockBigInteger val, boolean isRecursion) {
        if (val.signum == 0 || signum == 0)
            return ZERO;

        int xlen = mag.length;

        if (val == this && xlen > MULTIPLY_SQUARE_THRESHOLD) {
            return square();
        }

        int ylen = val.mag.length;

        if ((xlen < KARATSUBA_THRESHOLD) || (ylen < KARATSUBA_THRESHOLD)) {
            int resultSign = signum == val.signum ? 1 : -1;
            if (val.mag.length == 1) {
                return multiplyByInt(mag,val.mag[0], resultSign);
            }
            if (mag.length == 1) {
                return multiplyByInt(val.mag,mag[0], resultSign);
            }
            int[] result = multiplyToLen(mag, xlen,
                    val.mag, ylen, null);
            result = trustedStripLeadingZeroInts(result);
            return new MockBigInteger(result, resultSign);
        } else {
            if ((xlen < TOOM_COOK_THRESHOLD) && (ylen < TOOM_COOK_THRESHOLD)) {
                return multiplyKaratsuba(this, val);
            } else {

                if (!isRecursion) {
               
                    if ((long)bitLength(mag, mag.length) +
                            (long)bitLength(val.mag, val.mag.length) >
                            32L*MAX_MAG_LENGTH) {
                        reportOverflow();
                    }
                }

                return multiplyToomCook3(this, val);
            }
        }
    }

    private static MockBigInteger multiplyByInt(int[] x, int y, int sign) {
        if (Integer.bitCount(y) == 1) {
            return new MockBigInteger(shiftLeft(x,Integer.numberOfTrailingZeros(y)), sign);
        }
        int xlen = x.length;
        int[] rmag =  new int[xlen + 1];
        long carry = 0;
        long yl = y & LONG_MASK;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (x[i] & LONG_MASK) * yl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        if (carry == 0L) {
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        } else {
            rmag[rstart] = (int)carry;
        }
        return new MockBigInteger(rmag, sign);
    }




   MockBigInteger multiply(long v) {
        if (v == 0 || signum == 0)
            return ZERO;
        if (v == INFLATED)
            return multiply(com.snoworca.cson.util.MockBigInteger.valueOf(v));
        int rsign = (v > 0 ? signum : -signum);
        if (v < 0)
            v = -v;
        long dh = v >>> 32;      // higher order bits
        long dl = v & LONG_MASK; // lower order bits

        int xlen = mag.length;
        int[] value = mag;
        int[] rmag = (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
        long carry = 0;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (value[i] & LONG_MASK) * dl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        rmag[rstart] = (int)carry;
        if (dh != 0L) {
            carry = 0;
            rstart = rmag.length - 2;
            for (int i = xlen - 1; i >= 0; i--) {
                long product = (value[i] & LONG_MASK) * dh +
                        (rmag[rstart] & LONG_MASK) + carry;
                rmag[rstart--] = (int)product;
                carry = product >>> 32;
            }
            rmag[0] = (int)carry;
        }
        if (carry == 0L)
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        return new MockBigInteger(rmag, rsign);
    }




    private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        multiplyToLenCheck(x, xlen);
        multiplyToLenCheck(y, ylen);
        return implMultiplyToLen(x, xlen, y, ylen, z);
    }

    private static int[] implMultiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;

        if (z == null || z.length < (xlen+ ylen))
            z = new int[xlen+ylen];

        long carry = 0;
        for (int j=ystart, k=ystart+1+xstart; j >= 0; j--, k--) {
            long product = (y[j] & LONG_MASK) *
                    (x[xstart] & LONG_MASK) + carry;
            z[k] = (int)product;
            carry = product >>> 32;
        }
        z[xstart] = (int)carry;

        for (int i = xstart-1; i >= 0; i--) {
            carry = 0;
            for (int j=ystart, k=ystart+1+i; j >= 0; j--, k--) {
                long product = (y[j] & LONG_MASK) *
                        (x[i] & LONG_MASK) +
                        (z[k] & LONG_MASK) + carry;
                z[k] = (int)product;
                carry = product >>> 32;
            }
            z[i] = (int)carry;
        }
        return z;
    }

    private static void multiplyToLenCheck(int[] array, int length) {
        if (length <= 0) {
            return;  // not an error because multiplyToLen won't execute if len <= 0
        }



        CompatibleForObjects.requireNonNull(array);

        if (length > array.length) {
            throw new ArrayIndexOutOfBoundsException(length - 1);
        }
    }















    private static MockBigInteger multiplyKaratsuba(MockBigInteger x, MockBigInteger y) {
        int xlen = x.mag.length;
        int ylen = y.mag.length;

        int half = (Math.max(xlen, ylen)+1) / 2;


       MockBigInteger xl = x.getLower(half);
       MockBigInteger xh = x.getUpper(half);
       MockBigInteger yl = y.getLower(half);
       MockBigInteger yh = y.getUpper(half);

       MockBigInteger p1 = xh.multiply(yh);  // p1 = xh*yh
       MockBigInteger p2 = xl.multiply(yl);  // p2 = xl*yl

       MockBigInteger p3 = xh.add(xl).multiply(yh.add(yl));

       MockBigInteger result = p1.shiftLeft(32*half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32*half).add(p2);

        if (x.signum != y.signum) {
            return result.negate();
        } else {
            return result;
        }
    }




























    private static MockBigInteger multiplyToomCook3(MockBigInteger a, MockBigInteger b) {
        int alen = a.mag.length;
        int blen = b.mag.length;

        int largest = Math.max(alen, blen);

        int k = (largest+2)/3;   // Equal to ceil(largest/3)

        int r = largest - 2*k;


       MockBigInteger a0, a1, a2, b0, b1, b2;
        a2 = a.getToomSlice(k, r, 0, largest);
        a1 = a.getToomSlice(k, r, 1, largest);
        a0 = a.getToomSlice(k, r, 2, largest);
        b2 = b.getToomSlice(k, r, 0, largest);
        b1 = b.getToomSlice(k, r, 1, largest);
        b0 = b.getToomSlice(k, r, 2, largest);

       MockBigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;

        v0 = a0.multiply(b0, true);
        da1 = a2.add(a0);
        db1 = b2.add(b0);
        vm1 = da1.subtract(a1).multiply(db1.subtract(b1), true);
        da1 = da1.add(a1);
        db1 = db1.add(b1);
        v1 = da1.multiply(db1, true);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(
                db1.add(b2).shiftLeft(1).subtract(b0), true);
        vinf = a2.multiply(b2, true);






        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        int ss = k*32;

       MockBigInteger result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);

        if (a.signum != b.signum) {
            return result.negate();
        } else {
            return result;
        }
    }













    private MockBigInteger getToomSlice(int lowerSize, int upperSize, int slice,
                                        int fullsize) {
        int start, end, sliceSize, len, offset;

        len = mag.length;
        offset = fullsize - len;

        if (slice == 0) {
            start = 0 - offset;
            end = upperSize - 1 - offset;
        } else {
            start = upperSize + (slice-1)*lowerSize - offset;
            end = start + lowerSize - 1;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            return ZERO;
        }

        sliceSize = (end-start) + 1;

        if (sliceSize <= 0) {
            return ZERO;
        }


        if (start == 0 && sliceSize >= len) {
            return this.abs();
        }

        int intSlice[] = new int[sliceSize];
        System.arraycopy(mag, start, intSlice, 0, sliceSize);

        return new MockBigInteger(trustedStripLeadingZeroInts(intSlice), 1);
    }








    private MockBigInteger exactDivideBy3() {
        int len = mag.length;
        int[] result = new int[len];
        long x, w, q, borrow;
        borrow = 0L;
        for (int i=len-1; i >= 0; i--) {
            x = (mag[i] & LONG_MASK);
            w = x - borrow;
            if (borrow > x) {      // Did we make the number go negative?
                borrow = 1L;
            } else {
                borrow = 0L;
            }



            q = (w * 0xAAAAAAABL) & LONG_MASK;
            result[i] = (int) q;


            if (q >= 0x55555556L) {
                borrow++;
                if (q >= 0xAAAAAAABL)
                    borrow++;
            }
        }
        result = trustedStripLeadingZeroInts(result);
        return new MockBigInteger(result, signum);
    }




    private MockBigInteger getLower(int n) {
        int len = mag.length;

        if (len <= n) {
            return abs();
        }

        int lowerInts[] = new int[n];
        System.arraycopy(mag, len-n, lowerInts, 0, n);

        return new MockBigInteger(trustedStripLeadingZeroInts(lowerInts), 1);
    }





    private MockBigInteger getUpper(int n) {
        int len = mag.length;

        if (len <= n) {
            return ZERO;
        }

        int upperLen = len - n;
        int upperInts[] = new int[upperLen];
        System.arraycopy(mag, 0, upperInts, 0, upperLen);

        return new MockBigInteger(trustedStripLeadingZeroInts(upperInts), 1);
    }






    private MockBigInteger square() {
        return square(false);
    }







    private MockBigInteger square(boolean isRecursion) {
        if (signum == 0) {
            return ZERO;
        }
        int len = mag.length;

        if (len < KARATSUBA_SQUARE_THRESHOLD) {
            int[] z = squareToLen(mag, len, null);
            return new MockBigInteger(trustedStripLeadingZeroInts(z), 1);
        } else {
            if (len < TOOM_COOK_SQUARE_THRESHOLD) {
                return squareKaratsuba();
            } else {



                if (!isRecursion) {
                    if (bitLength(mag, mag.length) > 16L*MAX_MAG_LENGTH) {
                        reportOverflow();
                    }
                }

                return squareToomCook3();
            }
        }
    }




    private static int[] squareToLen(int[] x, int len, int[] z) {
        int zlen = len << 1;
        if (z == null || z.length < zlen)
            z = new int[zlen];

        implSquareToLenChecks(x, len, z, zlen);
        return implSquareToLen(x, len, z, zlen);
    }



    private static void implSquareToLenChecks(int[] x, int len, int[] z, int zlen) throws RuntimeException {
        if (len < 1) {
            throw new IllegalArgumentException("invalid input length: " + len);
        }
        if (len > x.length) {
            throw new IllegalArgumentException("input length out of bound: " +
                    len + " > " + x.length);
        }
        if (len * 2 > z.length) {
            throw new IllegalArgumentException("input length out of bound: " +
                    (len * 2) + " > " + z.length);
        }
        if (zlen < 1) {
            throw new IllegalArgumentException("invalid input length: " + zlen);
        }
        if (zlen > z.length) {
            throw new IllegalArgumentException("input length out of bound: " +
                    len + " > " + z.length);
        }
    }

    private static int[] implSquareToLen(int[] x, int len, int[] z, int zlen) {

        int lastProductLowWord = 0;
        for (int j=0, i=0; j < len; j++) {
            long piece = (x[j] & LONG_MASK);
            long product = piece * piece;
            z[i++] = (lastProductLowWord << 31) | (int)(product >>> 33);
            z[i++] = (int)(product >>> 1);
            lastProductLowWord = (int)product;
        }

        for (int i=len, offset=1; i > 0; i--, offset+=2) {
            int t = x[i-1];
            t = mulAdd(z, x, offset, i-1, t);
            addOne(z, offset-1, i, t);
        }

        primitiveLeftShift(z, zlen, 1);
        z[zlen-1] |= x[len-1] & 1;

        return z;
    }







    private MockBigInteger squareKaratsuba() {
        int half = (mag.length+1) / 2;

       MockBigInteger xl = getLower(half);
       MockBigInteger xh = getUpper(half);

       MockBigInteger xhs = xh.square();  // xhs = xh^2
       MockBigInteger xls = xl.square();  // xls = xl^2

        return xhs.shiftLeft(half*32).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(half*32).add(xls);
    }







    private MockBigInteger squareToomCook3() {
        int len = mag.length;

        int k = (len+2)/3;   // Equal to ceil(largest/3)

        int r = len - 2*k;


       MockBigInteger a0, a1, a2;
        a2 = getToomSlice(k, r, 0, len);
        a1 = getToomSlice(k, r, 1, len);
        a0 = getToomSlice(k, r, 2, len);
       MockBigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

        v0 = a0.square(true);
        da1 = a2.add(a0);
        vm1 = da1.subtract(a1).square(true);
        da1 = da1.add(a1);
        v1 = da1.square(true);
        vinf = a2.square(true);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).square(true);






        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        int ss = k*32;

        return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
    }








    public MockBigInteger divide(MockBigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return divideKnuth(val);
        } else {
            return divideBurnikelZiegler(val);
        }
    }








    private MockBigInteger divideKnuth(MockBigInteger val) {
        MutableBigIntegerMock q = new MutableBigIntegerMock(),
                a = new MutableBigIntegerMock(this.mag),
                b = new MutableBigIntegerMock(val.mag);

        a.divideKnuth(b, q, false);
        return q.toBigInteger(this.signum * val.signum);
    }











    public MockBigInteger[] divideAndRemainder(MockBigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return divideAndRemainderKnuth(val);
        } else {
            return divideAndRemainderBurnikelZiegler(val);
        }
    }

    private MockBigInteger[] divideAndRemainderKnuth(MockBigInteger val) {
       MockBigInteger[] result = new MockBigInteger[2];
        MutableBigIntegerMock q = new MutableBigIntegerMock(),
                a = new MutableBigIntegerMock(this.mag),
                b = new MutableBigIntegerMock(val.mag);
        MutableBigIntegerMock r = a.divideKnuth(b, q);
        result[0] = q.toBigInteger(this.signum == val.signum ? 1 : -1);
        result[1] = r.toBigInteger(this.signum);
        return result;
    }








    public MockBigInteger remainder(MockBigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return remainderKnuth(val);
        } else {
            return remainderBurnikelZiegler(val);
        }
    }

    private MockBigInteger remainderKnuth(MockBigInteger val) {
        MutableBigIntegerMock q = new MutableBigIntegerMock(),
                a = new MutableBigIntegerMock(this.mag),
                b = new MutableBigIntegerMock(val.mag);

        return a.divideKnuth(b, q).toBigInteger(this.signum);
    }





    private MockBigInteger divideBurnikelZiegler(MockBigInteger val) {
        return divideAndRemainderBurnikelZiegler(val)[0];
    }





    private MockBigInteger remainderBurnikelZiegler(MockBigInteger val) {
        return divideAndRemainderBurnikelZiegler(val)[1];
    }






    private MockBigInteger[] divideAndRemainderBurnikelZiegler(MockBigInteger val) {
        MutableBigIntegerMock q = new MutableBigIntegerMock();
        MutableBigIntegerMock r = new MutableBigIntegerMock(this).divideAndRemainderBurnikelZiegler(new MutableBigIntegerMock(val), q);
       MockBigInteger qBigInt = q.isZero() ? ZERO : q.toBigInteger(signum*val.signum);
       MockBigInteger rBigInt = r.isZero() ? ZERO : r.toBigInteger(signum);
        return new MockBigInteger[] {qBigInt, rBigInt};
    }









    public MockBigInteger pow(int exponent) {
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent");
        }
        if (signum == 0) {
            return (exponent == 0 ? ONE : this);
        }

       MockBigInteger partToSquare = this.abs();




        int powersOfTwo = partToSquare.getLowestSetBit();
        long bitsToShiftLong = (long)powersOfTwo * exponent;
        if (bitsToShiftLong > Integer.MAX_VALUE) {
            reportOverflow();
        }
        int bitsToShift = (int)bitsToShiftLong;

        int remainingBits;

        if (powersOfTwo > 0) {
            partToSquare = partToSquare.shiftRight(powersOfTwo);
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) {  // Nothing left but +/- 1?
                if (signum < 0 && (exponent&1) == 1) {
                    return NEGATIVE_ONE.shiftLeft(bitsToShift);
                } else {
                    return ONE.shiftLeft(bitsToShift);
                }
            }
        } else {
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) { // Nothing left but +/- 1?
                if (signum < 0  && (exponent&1) == 1) {
                    return NEGATIVE_ONE;
                } else {
                    return ONE;
                }
            }
        }



        long scaleFactor = (long)remainingBits * exponent;


        if (partToSquare.mag.length == 1 && scaleFactor <= 62) {

            int newSign = (signum <0  && (exponent&1) == 1 ? -1 : 1);
            long result = 1;
            long baseToPow2 = partToSquare.mag[0] & LONG_MASK;

            int workingExponent = exponent;

            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    result = result * baseToPow2;
                }

                if ((workingExponent >>>= 1) != 0) {
                    baseToPow2 = baseToPow2 * baseToPow2;
                }
            }

            if (powersOfTwo > 0) {
                if (bitsToShift + scaleFactor <= 62) { // Fits in long?
                    return valueOf((result << bitsToShift) * newSign);
                } else {
                    return valueOf(result*newSign).shiftLeft(bitsToShift);
                }
            } else {
                return valueOf(result*newSign);
            }
        } else {
            if ((long)bitLength() * exponent / Integer.SIZE > MAX_MAG_LENGTH) {
                reportOverflow();
            }



           MockBigInteger answer = ONE;

            int workingExponent = exponent;

            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    answer = answer.multiply(partToSquare);
                }

                if ((workingExponent >>>= 1) != 0) {
                    partToSquare = partToSquare.square();
                }
            }


            if (powersOfTwo > 0) {
                answer = answer.shiftLeft(bitsToShift);
            }

            if (signum < 0 && (exponent&1) == 1) {
                return answer.negate();
            } else {
                return answer;
            }
        }
    }

















    public MockBigInteger sqrt() {
        if (this.signum < 0) {
            throw new ArithmeticException("NegativeRefBigInteger");
        }

        return new MutableBigIntegerMock(this.mag).sqrt().toBigInteger();
    }












    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }




    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n&0x1F;
        int bitsInHighWord = bitLengthForInt(a[0]);

        if (n <= (32-bitsInHighWord)) {
            primitiveLeftShift(a, len, nBits);
            return a;
        } else { // Array must be resized
            if (nBits <= (32-bitsInHighWord)) {
                int result[] = new int[nInts+len];
                System.arraycopy(a, 0, result, 0, len);
                primitiveLeftShift(result, result.length, nBits);
                return result;
            } else {
                int result[] = new int[nInts+len+1];
                System.arraycopy(a, 0, result, 0, len);
                primitiveRightShift(result, result.length, 32 - nBits);
                return result;
            }
        }
    }

    static void primitiveRightShift(int[] a, int len, int n) {
        int n2 = 32 - n;
        for (int i=len-1, c=a[i]; i > 0; i--) {
            int b = c;
            c = a[i-1];
            a[i] = (c << n2) | (b >>> n);
        }
        a[0] >>>= n;
    }

    static void primitiveLeftShift(int[] a, int len, int n) {
        if (len == 0 || n == 0)
            return;

        int n2 = 32 - n;
        for (int i=0, c=a[i], m=i+len-1; i < m; i++) {
            int b = c;
            c = a[i+1];
            a[i] = (b << n) | (c >>> n2);
        }
        a[len-1] <<= n;
    }




    private static int bitLength(int[] val, int len) {
        if (len == 0)
            return 0;
        return ((len - 1) << 5) + bitLengthForInt(val[0]);
    }






    public MockBigInteger abs() {
        return (signum >= 0 ? this : this.negate());
    }





    public MockBigInteger negate() {
        return new MockBigInteger(this.mag, -this.signum);
    }











    public MockBigInteger mod(MockBigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("MockBigInteger: modulus not positive");

       MockBigInteger result = this.remainder(m);
        return (result.signum >= 0 ? result : result.add(m));
    }















    private static int[] montgomeryMultiply(int[] a, int[] b, int[] n, int len, long inv,
                                            int[] product) {
        implMontgomeryMultiplyChecks(a, b, n, len, product);
        if (len > MONTGOMERY_INTRINSIC_THRESHOLD) {

            product = multiplyToLen(a, len, b, len, product);
            return montReduce(product, n, len, (int)inv);
        } else {
            return implMontgomeryMultiply(a, b, n, len, inv, materialize(product, len));
        }
    }
    private static int[] montgomerySquare(int[] a, int[] n, int len, long inv,
                                          int[] product) {
        implMontgomeryMultiplyChecks(a, a, n, len, product);
        if (len > MONTGOMERY_INTRINSIC_THRESHOLD) {

            product = squareToLen(a, len, product);
            return montReduce(product, n, len, (int)inv);
        } else {
            return implMontgomerySquare(a, n, len, inv, materialize(product, len));
        }
    }

    private static void implMontgomeryMultiplyChecks
    (int[] a, int[] b, int[] n, int len, int[] product) throws RuntimeException {
        if (len % 2 != 0) {
            throw new IllegalArgumentException("input array length must be even: " + len);
        }

        if (len < 1) {
            throw new IllegalArgumentException("invalid input length: " + len);
        }

        if (len > a.length ||
                len > b.length ||
                len > n.length ||
                (product != null && len > product.length)) {
            throw new IllegalArgumentException("input array length out of bound: " + len);
        }
    }



    private static int[] materialize(int[] z, int len) {
        if (z == null || z.length < len)
            z = new int[len];
        return z;
    }


    
    private static int[] implMontgomeryMultiply(int[] a, int[] b, int[] n, int len,
                                                long inv, int[] product) {
        product = multiplyToLen(a, len, b, len, product);
        return montReduce(product, n, len, (int)inv);
    }
    
    private static int[] implMontgomerySquare(int[] a, int[] n, int len,
                                              long inv, int[] product) {
        product = squareToLen(a, len, product);
        return montReduce(product, n, len, (int)inv);
    }

    static int[] bnExpModThreshTable = {7, 25, 81, 241, 673, 1793,
            Integer.MAX_VALUE}; // Sentinel






    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
        int c=0;
        int len = mlen;
        int offset=0;

        do {
            int nEnd = n[n.length-1-offset];
            int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
            c += addOne(n, offset, mlen, carry);
            offset++;
        } while (--len > 0);

        while (c > 0)
            c += subN(n, mod, mlen);

        while (intArrayCmpToLen(n, mod, mlen) >= 0)
            subN(n, mod, mlen);

        return n;
    }




    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
        for (int i=0; i < len; i++) {
            long b1 = arg1[i] & LONG_MASK;
            long b2 = arg2[i] & LONG_MASK;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }



    private static int subN(int[] a, int[] b, int len) {
        long sum = 0;

        while (--len >= 0) {
            sum = (a[len] & LONG_MASK) -
                    (b[len] & LONG_MASK) + (sum >> 32);
            a[len] = (int)sum;
        }

        return (int)(sum >> 32);
    }



    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
        implMulAddCheck(out, in, offset, len, k);
        return implMulAdd(out, in, offset, len, k);
    }



    private static void implMulAddCheck(int[] out, int[] in, int offset, int len, int k) {
        if (len > in.length) {
            throw new IllegalArgumentException("input length is out of bound: " + len + " > " + in.length);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("input offset is invalid: " + offset);
        }
        if (offset > (out.length - 1)) {
            throw new IllegalArgumentException("input offset is out of bound: " + offset + " > " + (out.length - 1));
        }
        if (len > (out.length - offset)) {
            throw new IllegalArgumentException("input len is out of bound: " + len + " > " + (out.length - offset));
        }
    }



    
    private static int implMulAdd(int[] out, int[] in, int offset, int len, int k) {
        long kLong = k & LONG_MASK;
        long carry = 0;

        offset = out.length-offset - 1;
        for (int j=len-1; j >= 0; j--) {
            long product = (in[j] & LONG_MASK) * kLong +
                    (out[offset] & LONG_MASK) + carry;
            out[offset--] = (int)product;
            carry = product >>> 32;
        }
        return (int)carry;
    }




    static int addOne(int[] a, int offset, int mlen, int carry) {
        offset = a.length-1-mlen-offset;
        long t = (a[offset] & LONG_MASK) + (carry & LONG_MASK);

        a[offset] = (int)t;
        if ((t >>> 32) == 0)
            return 0;
        while (--mlen >= 0) {
            if (--offset < 0) { // Carry out of number
                return 1;
            } else {
                a[offset]++;
                if (a[offset] != 0)
                    return 0;
            }
        }
        return 1;
    }



    private MockBigInteger modPow2(MockBigInteger exponent, int p) {




       MockBigInteger result = ONE;
       MockBigInteger baseToPow2 = this.mod2(p);
        int expOffset = 0;

        int limit = exponent.bitLength();

        if (this.testBit(0))
            limit = (p-1) < limit ? (p-1) : limit;

        while (expOffset < limit) {
            if (exponent.testBit(expOffset))
                result = result.multiply(baseToPow2).mod2(p);
            expOffset++;
            if (expOffset < limit)
                baseToPow2 = baseToPow2.square().mod2(p);
        }

        return result;
    }




    private MockBigInteger mod2(int p) {
        if (bitLength() <= p)
            return this;

        int numInts = (p + 31) >>> 5;
        int[] mag = new int[numInts];
        System.arraycopy(this.mag, (this.mag.length - numInts), mag, 0, numInts);

        int excessBits = (numInts << 5) - p;
        mag[0] &= (1L << (32-excessBits)) - 1;

        return (mag[0] == 0 ? new MockBigInteger(1, mag) : new MockBigInteger(mag, 1));
    }











    public MockBigInteger shiftLeft(int n) {
        if (signum == 0)
            return ZERO;
        if (n > 0) {
            return new MockBigInteger(shiftLeft(mag, n), signum);
        } else if (n == 0) {
            return this;
        } else {


            return shiftRightImpl(-n);
        }
    }









    private static int[] shiftLeft(int[] mag, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int newMag[] = null;

        if (nBits == 0) {
            newMag = new int[magLen + nInts];
            System.arraycopy(mag, 0, newMag, 0, magLen);
        } else {
            int i = 0;
            int nBits2 = 32 - nBits;
            int highBits = mag[0] >>> nBits2;
            if (highBits != 0) {
                newMag = new int[magLen + nInts + 1];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen + nInts];
            }
            int j=0;
            while (j < magLen-1)
                newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
            newMag[i] = mag[j] << nBits;
        }
        return newMag;
    }










    public MockBigInteger shiftRight(int n) {
        if (signum == 0)
            return ZERO;
        if (n > 0) {
            return shiftRightImpl(n);
        } else if (n == 0) {
            return this;
        } else {


            return new MockBigInteger(shiftLeft(mag, -n), signum);
        }
    }








    private MockBigInteger shiftRightImpl(int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int newMag[] = null;

        if (nInts >= magLen)
            return (signum >= 0 ? ZERO : negConst[1]);

        if (nBits == 0) {
            int newMagLen = magLen - nInts;
            newMag = Arrays.copyOf(mag, newMagLen);
        } else {
            int i = 0;
            int highBits = mag[0] >>> nBits;
            if (highBits != 0) {
                newMag = new int[magLen - nInts];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen - nInts -1];
            }

            int nBits2 = 32 - nBits;
            int j=0;
            while (j < magLen - nInts - 1)
                newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
        }

        if (signum < 0) {

            boolean onesLost = false;
            for (int i=magLen-1, j=magLen-nInts; i >= j && !onesLost; i--)
                onesLost = (mag[i] != 0);
            if (!onesLost && nBits != 0)
                onesLost = (mag[magLen - nInts - 1] << (32 - nBits) != 0);

            if (onesLost)
                newMag = javaIncrement(newMag);
        }

        return new MockBigInteger(newMag, signum);
    }

    int[] javaIncrement(int[] val) {
        int lastSum = 0;
        for (int i=val.length-1;  i >= 0 && lastSum == 0; i--)
            lastSum = (val[i] += 1);
        if (lastSum == 0) {
            val = new int[val.length+1];
            val[0] = 1;
        }
        return val;
    }


    protected boolean testBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
    }

    protected int getLowestSetBit() {
        int lsb = lowestSetBitPlusTwo - 2;
        if (lsb == -2) {
            lsb = 0;
            if (signum == 0) {
                lsb -= 1;
            } else {
                int i,b;
                for (i=0; (b = getInt(i)) == 0; i++)
                    ;
                lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
            }
            lowestSetBitPlusTwo = lsb + 2;
        }
        return lsb;
    }



    public int bitLength() {
        int n = bitLengthPlusOne - 1;
        if (n == -1) {
            int[] m = mag;
            int len = m.length;
            if (len == 0) {
                n = 0;
            }  else {
                int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
                if (signum < 0) {
                    boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                    for (int i=1; i< len && pow2; i++)
                        pow2 = (mag[i] == 0);

                    n = (pow2 ? magBitLength - 1 : magBitLength);
                } else {
                    n = magBitLength;
                }
            }
            bitLengthPlusOne = n + 1;
        }
        return n;
    }



    public int compareTo(MockBigInteger val) {
        if (signum == val.signum) {
            switch (signum) {
                case 1:
                    return compareMagnitude(val);
                case -1:
                    return val.compareMagnitude(this);
                default:
                    return 0;
            }
        }
        return signum > val.signum ? 1 : -1;
    }


    private int compareMagnitude(MockBigInteger val) {
        int[] m1 = mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if (len1 < len2)
            return -1;
        if (len1 > len2)
            return 1;
        for (int i = 0; i < len1; i++) {
            int a = m1[i];
            int b = m2[i];
            if (a != b)
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
        }
        return 0;
    }

    public boolean equals(Object x) {
        if (x == this)
            return true;

        if (!(x instanceof MockBigInteger))
            return false;

       MockBigInteger xInt = (MockBigInteger) x;
        if (xInt.signum != signum)
            return false;

        int[] m = mag;
        int len = m.length;
        int[] xm = xInt.mag;
        if (len != xm.length)
            return false;

        for (int i = 0; i < len; i++)
            if (xm[i] != m[i])
                return false;

        return true;
    }


    public int hashCode() {
        int hashCode = 0;

        for (int i=0; i < mag.length; i++)
            hashCode = (int)(31*hashCode + (mag[i] & LONG_MASK));

        return hashCode * signum;
    }

    public String toString(int radix) {
        if (signum == 0)
            return "0";
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        if (mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD)
            return smallToString(radix);

        StringBuilder sb = new StringBuilder();
        if (signum < 0) {
            toString(this.negate(), sb, radix, 0);
            sb.insert(0, '-');
        }
        else
            toString(this, sb, radix, 0);

        return sb.toString();
    }

    private String smallToString(int radix) {
        if (signum == 0) {
            return "0";
        }

        int maxNumDigitGroups = (4*mag.length + 6)/7;
        String digitGroup[] = new String[maxNumDigitGroups];

       MockBigInteger tmp = this.abs();
        int numGroups = 0;
        while (tmp.signum != 0) {
           MockBigInteger d = longRadix[radix];

            MutableBigIntegerMock q = new MutableBigIntegerMock(),
                    a = new MutableBigIntegerMock(tmp.mag),
                    b = new MutableBigIntegerMock(d.mag);
            MutableBigIntegerMock r = a.divide(b, q);
           MockBigInteger q2 = q.toBigInteger(tmp.signum * d.signum);
           MockBigInteger r2 = r.toBigInteger(tmp.signum * d.signum);

            digitGroup[numGroups++] = Long.toString(r2.longValue(), radix);
            tmp = q2;
        }

        StringBuilder buf = new StringBuilder(numGroups*digitsPerLong[radix]+1);
        if (signum < 0) {
            buf.append('-');
        }
        buf.append(digitGroup[numGroups-1]);

        for (int i=numGroups-2; i >= 0; i--) {

            int numLeadingZeros = digitsPerLong[radix]-digitGroup[i].length();
            if (numLeadingZeros != 0) {
                buf.append(zeros[numLeadingZeros]);
            }
            buf.append(digitGroup[i]);
        }
        return buf.toString();
    }

    private static void toString(MockBigInteger u, StringBuilder sb, int radix,
                                 int digits) {
        if (u.mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
            String s = u.smallToString(radix);

            if ((s.length() < digits) && (sb.length() > 0)) {
                for (int i=s.length(); i < digits; i++) {
                    sb.append('0');
                }
            }

            sb.append(s);
            return;
        }

        int b, n;
        b = u.bitLength();

        n = (int) Math.round(Math.log(b * LOG_TWO / logCache[radix]) / LOG_TWO - 1.0);
       MockBigInteger v = getRadixConversionCache(radix, n);
       MockBigInteger[] results;
        results = u.divideAndRemainder(v);

        int expectedDigits = 1 << n;

        toString(results[0], sb, radix, digits-expectedDigits);
        toString(results[1], sb, radix, expectedDigits);
    }


    private static MockBigInteger getRadixConversionCache(int radix, int exponent) {
       MockBigInteger[] cacheLine = powerCache[radix]; // volatile read
        if (exponent < cacheLine.length) {
            return cacheLine[exponent];
        }

        int oldLength = cacheLine.length;
        cacheLine = Arrays.copyOf(cacheLine, exponent + 1);
        for (int i = oldLength; i <= exponent; i++) {
            cacheLine[i] = cacheLine[i - 1].pow(2);
        }

       MockBigInteger[][] pc = powerCache; // volatile read again
        if (exponent >= pc[radix].length) {
            pc = pc.clone();
            pc[radix] = cacheLine;
            powerCache = pc; // volatile write, publish
        }
        return cacheLine[exponent];
    }

    private static String zeros[] = new String[64];
    static {
        zeros[63] =
                "000000000000000000000000000000000000000000000000000000000000000";
        for (int i=0; i < 63; i++)
            zeros[i] = zeros[63].substring(0, i);
    }

    public String toString() {
        return toString(10);
    }


    public int intValue() {
        int result = 0;
        result = getInt(0);
        return result;
    }

    public long longValue() {
        long result = 0;

        for (int i=1; i >= 0; i--)
            result = (result << 32) + (getInt(i) & LONG_MASK);
        return result;
    }

    public float floatValue() {
        if (signum == 0) {
            return 0.0f;
        }

        int exponent = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]) - 1;

        if (exponent < Long.SIZE - 1) {
            return longValue();
        } else if (exponent > Float.MAX_EXPONENT) {
            return signum > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }

        int shift = exponent - SIGNIFICAND_WIDTH;

        int twiceSignifFloor;

        int nBits = shift & 0x1f;
        int nBits2 = 32 - nBits;

        if (nBits == 0) {
            twiceSignifFloor = mag[0];
        } else {
            twiceSignifFloor = mag[0] >>> nBits;
            if (twiceSignifFloor == 0) {
                twiceSignifFloor = (mag[0] << nBits2) | (mag[1] >>> nBits);
            }
        }

        int signifFloor = twiceSignifFloor >> 1;
        signifFloor &= SIGNIF_BIT_MASK; // remove the implied bit

        boolean increment = (twiceSignifFloor & 1) != 0
                && ((signifFloor & 1) != 0 || abs().getLowestSetBit() < shift);
        int signifRounded = increment ? signifFloor + 1 : signifFloor;
        int bits = ((exponent + EXP_BIAS))
                << (SIGNIFICAND_WIDTH - 1);
        bits += signifRounded;

        bits |= signum & SIGN_BIT_MASK;
        return Float.intBitsToFloat(bits);
    }

    public double doubleValue() {
        if (signum == 0) {
            return 0.0;
        }

        int exponent = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]) - 1;

        if (exponent < Long.SIZE - 1) {
            return longValue();
        } else if (exponent > Double.MAX_EXPONENT) {
            return signum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        int shift = exponent - SIGNIFICAND_WIDTH;

        long twiceSignifFloor;

        int nBits = shift & 0x1f;
        int nBits2 = 32 - nBits;

        int highBits;
        int lowBits;
        if (nBits == 0) {
            highBits = mag[0];
            lowBits = mag[1];
        } else {
            highBits = mag[0] >>> nBits;
            lowBits = (mag[0] << nBits2) | (mag[1] >>> nBits);
            if (highBits == 0) {
                highBits = lowBits;
                lowBits = (mag[1] << nBits2) | (mag[2] >>> nBits);
            }
        }

        twiceSignifFloor = ((highBits & LONG_MASK) << 32)
                | (lowBits & LONG_MASK);

        long signifFloor = twiceSignifFloor >> 1;
        signifFloor &= SIGNIF_BIT_MASK; // remove the implied bit


        boolean increment = (twiceSignifFloor & 1) != 0
                && ((signifFloor & 1) != 0 || abs().getLowestSetBit() < shift);
        long signifRounded = increment ? signifFloor + 1 : signifFloor;
        long bits = (long) ((exponent + EXP_BIAS))
                << (SIGNIFICAND_WIDTH - 1);
        bits += signifRounded;

        bits |= signum & SIGN_BIT_MASK;
        return Double.longBitsToDouble(bits);
    }

    private static int[] stripLeadingZeroInts(int val[]) {
        int vlen = val.length;
        int keep;

        for (keep = 0; keep < vlen && val[keep] == 0; keep++)
            ;
        return java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] trustedStripLeadingZeroInts(int val[]) {
        int vlen = val.length;
        int keep;

        for (keep = 0; keep < vlen && val[keep] == 0; keep++)
            ;
        return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
    }


    private static final int[] digitsPerLong = {0, 0,
            62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14,
            14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};

    private static final MockBigInteger[] longRadix = {null, null,
            valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL),
            valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL),
            valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L),
            valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L),
            valueOf( 0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L),
            valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL),
            valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L),
            valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L),
            valueOf(0x5da0e1e53c5c8000L), valueOf( 0xb16a458ef403f19L),
            valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L),
            valueOf(0x5658597bcaa24000L), valueOf( 0x6feb266931a75b7L),
            valueOf( 0xc29e98000000000L), valueOf(0x14adf4b7320334b9L),
            valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL),
            valueOf(0x5a3c23e39c000000L), valueOf( 0x4e900abb53e6b71L),
            valueOf( 0x7600ec618141000L), valueOf( 0xaee5720ee830681L),
            valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L),
            valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L),
            valueOf(0x41c21cb8e1000000L)};



    private static final int[] digitsPerInt = {0, 0, 30, 19, 15, 13, 11,
            11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};

    private static final int[] intRadix = {0, 0,
            0x40000000, 0x4546b3db, 0x40000000, 0x48c27395, 0x159fd800,
            0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00, 0xcc6db61,
            0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f,  0x10000000,
            0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000, 0x6b5a6e1d,
            0x6c20a40,  0x8d2d931,  0xb640000,  0xe8d4a51,  0x1269ae40,
            0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840, 0x34e63b41,
            0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519, 0x39aa400
    };

    private int signInt() {
        return signum < 0 ? -1 : 0;
    }
    private int getInt(int n) {
        if (n < 0)
            return 0;
        if (n >= mag.length)
            return signInt();

        int magInt = mag[mag.length-n-1];

        return (signum >= 0 ? magInt :
                (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private int firstNonzeroIntNum() {
        int fn = firstNonzeroIntNumPlusTwo - 2;
        if (fn == -2) { // firstNonzeroIntNum not initialized yet

            int i;
            int mlen = mag.length;
            for (i = mlen - 1; i >= 0 && mag[i] == 0; i--)
                ;
            fn = mlen - i - 1;
            firstNonzeroIntNumPlusTwo = fn + 2; // offset by two to initialize
        }
        return fn;
    }

    private static final long serialVersionUID = -8287574255936472291L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("signum", Integer.TYPE),
            new ObjectStreamField("magnitude", byte[].class),
            new ObjectStreamField("bitCount", Integer.TYPE),
            new ObjectStreamField("bitLength", Integer.TYPE),
            new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE),
            new ObjectStreamField("lowestSetBit", Integer.TYPE)
    };


    private void readObjectNoData()
            throws ObjectStreamException {
        throw new InvalidObjectException("DeserializedRefBigInteger objects need data");
    }



    private void writeObject(ObjectOutputStream s) throws IOException {

        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("signum", signum);
        fields.put("magnitude", magSerializedForm());


        fields.put("bitCount", -1);
        fields.put("bitLength", -1);
        fields.put("lowestSetBit", -2);
        fields.put("firstNonzeroByteNum", -2);

        s.writeFields();
    }

    private byte[] magSerializedForm() {
        int len = mag.length;

        int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
        int byteLen = (bitLen + 7) >>> 3;
        byte[] result = new byte[byteLen];

        for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0;
             i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            result[i] = (byte)nextInt;
        }
        return result;
    }

    public long longValueExact() {
        if (mag.length <= 2 && bitLength() <= 63)
            return longValue();
        else
            throw new ArithmeticException("MockBigInteger out of long range");
    }

}
