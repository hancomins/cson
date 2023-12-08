package com.clipsoft.cson.util;


import java.util.Arrays;


public class MutableBigIntegerMock {

    static final long INFLATED = Long.MIN_VALUE;
    static final long LONG_MASK = 0xffffffffL;

    int[] value;

    int intLen;

    int offset = 0;


    static final MutableBigIntegerMock ONE = new MutableBigIntegerMock(1);

    static final int KNUTH_POW2_THRESH_LEN = 6;

    static final int KNUTH_POW2_THRESH_ZEROS = 3;


    MutableBigIntegerMock() {
        value = new int[1];
        intLen = 0;
    }

    MutableBigIntegerMock(int val) {
        value = new int[1];
        intLen = 1;
        value[0] = val;
    }

    MutableBigIntegerMock(int[] val) {
        value = val;
        intLen = val.length;
    }

    MutableBigIntegerMock(MockBigInteger b) {
        intLen = b.mag.length;
        value = Arrays.copyOf(b.mag, intLen);
    }

    MutableBigIntegerMock(MutableBigIntegerMock val) {
        intLen = val.intLen;
        value = Arrays.copyOfRange(val.value, val.offset, val.offset + intLen);
    }

    private void ones(int n) {
        if (n > value.length)
            value = new int[n];
        Arrays.fill(value, -1);
        offset = 0;
        intLen = n;
    }

    private int[] getMagnitudeArray() {
        if (offset > 0 || value.length != intLen)
            return Arrays.copyOfRange(value, offset, offset + intLen);
        return value;
    }

    private long toLong() {
        assert (intLen <= 2) : "this MutableBigIntegerMock exceeds the range of long";
        if (intLen == 0)
            return 0;
        long d = value[offset] & LONG_MASK;
        return (intLen == 2) ? d << 32 | (value[offset + 1] & LONG_MASK) : d;
    }

    MockBigInteger toBigInteger(int sign) {
        if (intLen == 0 || sign == 0)
            return MockBigInteger.ZERO;
        return new MockBigInteger(getMagnitudeArray(), sign);
    }

    MockBigInteger toBigInteger() {
        normalize();
        return toBigInteger(isZero() ? 0 : 1);
    }

    long toCompactValue(int sign) {
        if (intLen == 0 || sign == 0)
            return 0L;
        int[] mag = getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];


        if (len > 2 || (d < 0 && len == 2))
            return INFLATED;
        long v = (len == 2) ?
                ((mag[1] & LONG_MASK) | (d & LONG_MASK) << 32) :
                d & LONG_MASK;
        return sign == -1 ? -v : v;
    }

    void clear() {
        offset = intLen = 0;
        for (int index=0, n=value.length; index < n; index++)
            value[index] = 0;
    }

    void reset() {
        offset = intLen = 0;
    }

    final int compare(MutableBigIntegerMock b) {
        int blen = b.intLen;
        if (intLen < blen)
            return -1;
        if (intLen > blen)
            return 1;


        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < intLen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j]  + 0x80000000;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    private int compareShifted(MutableBigIntegerMock b, int ints) {
        int blen = b.intLen;
        int alen = intLen - ints;
        if (alen < blen)
            return -1;
        if (alen > blen)
            return 1;


        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < alen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j]  + 0x80000000;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    final int compareHalf(MutableBigIntegerMock b) {
        int blen = b.intLen;
        int len = intLen;
        if (len <= 0)
            return blen <= 0 ? 0 : -1;
        if (len > blen)
            return 1;
        if (len < blen - 1)
            return -1;
        int[] bval = b.value;
        int bstart = 0;
        int carry = 0;

        if (len != blen) { // len == blen - 1
            if (bval[bstart] == 1) {
                ++bstart;
                carry = 0x80000000;
            } else
                return -1;
        }


        int[] val = value;
        for (int i = offset, j = bstart; i < len + offset;) {
            int bv = bval[j++];
            long hb = ((bv >>> 1) + carry) & LONG_MASK;
            long v = val[i++] & LONG_MASK;
            if (v != hb)
                return v < hb ? -1 : 1;
            carry = (bv & 1) << 31; // carray will be either 0x80000000 or 0
        }
        return carry == 0 ? 0 : -1;
    }

    private final int getLowestSetBit() {
        if (intLen == 0)
            return -1;
        int j, b;
        for (j=intLen-1; (j > 0) && (value[j+offset] == 0); j--)
            ;
        b = value[j+offset];
        if (b == 0)
            return -1;
        return ((intLen-1-j)<<5) + Integer.numberOfTrailingZeros(b);
    }

    private final int getInt(int index) {
        return value[offset+index];
    }

    private final long getLong(int index) {
        return value[offset+index] & LONG_MASK;
    }

    final void normalize() {
        if (intLen == 0) {
            offset = 0;
            return;
        }

        int index = offset;
        if (value[index] != 0)
            return;

        int indexBound = index+intLen;
        do {
            index++;
        } while(index < indexBound && value[index] == 0);

        int numZeros = index - offset;
        intLen -= numZeros;
        offset = (intLen == 0 ?  0 : offset+numZeros);
    }

    private final void ensureCapacity(int len) {
        if (value.length < len) {
            value = new int[len];
            offset = 0;
            intLen = len;
        }
    }




    int[] toIntArray() {
        int[] result = new int[intLen];
        for(int i=0; i < intLen; i++)
            result[i] = value[offset+i];
        return result;
    }






    void setValue(int[] val, int length) {
        value = val;
        intLen = length;
        offset = 0;
    }




    void copyValue(MutableBigIntegerMock src) {
        int len = src.intLen;
        if (value.length < len)
            value = new int[len];
        System.arraycopy(src.value, src.offset, value, 0, len);
        intLen = len;
        offset = 0;
    }




    void copyValue(int[] val) {
        int len = val.length;
        if (value.length < len)
            value = new int[len];
        System.arraycopy(val, 0, value, 0, len);
        intLen = len;
        offset = 0;
    }



    boolean isOne() {
        return (intLen == 1) && (value[offset] == 1);
    }



    boolean isZero() {
        return (intLen == 0);
    }



    boolean isEven() {
        return (intLen == 0) || ((value[offset + intLen - 1] & 1) == 0);
    }



    boolean isOdd() {
        return isZero() ? false : ((value[offset + intLen - 1] & 1) == 1);
    }





    boolean isNormal() {
        if (intLen + offset > value.length)
            return false;
        if (intLen == 0)
            return true;
        return (value[offset] != 0);
    }



    public String toString() {
        MockBigInteger b = toBigInteger(1);
        return b.toString();
    }



    void safeRightShift(int n) {
        if (n/32 >= intLen) {
            reset();
        } else {
            rightShift(n);
        }
    }




    void rightShift(int n) {
        if (intLen == 0)
            return;
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        this.intLen -= nInts;
        if (nBits == 0)
            return;
        int bitsInHighWord = MockBigInteger.bitLengthForInt(value[offset]);
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits);
            this.intLen--;
        } else {
            primitiveRightShift(nBits);
        }
    }



    void safeLeftShift(int n) {
        if (n > 0) {
            leftShift(n);
        }
    }



    void leftShift(int n) {






        if (intLen == 0)
            return;
        int nInts = n >>> 5;
        int nBits = n&0x1F;
        int bitsInHighWord = MockBigInteger.bitLengthForInt(value[offset]);

        if (n <= (32-bitsInHighWord)) {
            primitiveLeftShift(nBits);
            return;
        }

        int newLen = intLen + nInts +1;
        if (nBits <= (32-bitsInHighWord))
            newLen--;
        if (value.length < newLen) {

            int[] result = new int[newLen];
            for (int i=0; i < intLen; i++)
                result[i] = value[offset+i];
            setValue(result, newLen);
        } else if (value.length - offset >= newLen) {

            for(int i=0; i < newLen - intLen; i++)
                value[offset+intLen+i] = 0;
        } else {

            for (int i=0; i < intLen; i++)
                value[i] = value[offset+i];
            for (int i=intLen; i < newLen; i++)
                value[i] = 0;
            offset = 0;
        }
        intLen = newLen;
        if (nBits == 0)
            return;
        if (nBits <= (32-bitsInHighWord))
            primitiveLeftShift(nBits);
        else
            primitiveRightShift(32 -nBits);
    }





    private int divadd(int[] a, int[] result, int offset) {
        long carry = 0;

        for (int j=a.length-1; j >= 0; j--) {
            long sum = (a[j] & LONG_MASK) +
                    (result[j+offset] & LONG_MASK) + carry;
            result[j+offset] = (int)sum;
            carry = sum >>> 32;
        }
        return (int)carry;
    }





    private int mulsub(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;

        for (int j=len-1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset] - product;
            q[offset--] = (int)difference;
            carry = (product >>> 32)
                    + (((difference & LONG_MASK) >
                    (((~(int)product) & LONG_MASK))) ? 1:0);
        }
        return (int)carry;
    }




    private int mulsubBorrow(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;
        for (int j=len-1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset--] - product;
            carry = (product >>> 32)
                    + (((difference & LONG_MASK) >
                    (((~(int)product) & LONG_MASK))) ? 1:0);
        }
        return (int)carry;
    }





    private final void primitiveRightShift(int n) {
        int[] val = value;
        int n2 = 32 - n;
        for (int i=offset+intLen-1, c=val[i]; i > offset; i--) {
            int b = c;
            c = val[i-1];
            val[i] = (c << n2) | (b >>> n);
        }
        val[offset] >>>= n;
    }





    private final void primitiveLeftShift(int n) {
        int[] val = value;
        int n2 = 32 - n;
        for (int i=offset, c=val[i], m=i+intLen-1; i < m; i++) {
            int b = c;
            c = val[i+1];
            val[i] = (b << n) | (c >>> n2);
        }
        val[offset+intLen-1] <<= n;
    }




    private MockBigInteger getLower(int n) {
        if (isZero()) {
            return MockBigInteger.ZERO;
        } else if (intLen < n) {
            return toBigInteger(1);
        } else {

            int len = n;
            while (len > 0 && value[offset+intLen-len] == 0)
                len--;
            int sign = len > 0 ? 1 : 0;
            return new MockBigInteger(Arrays.copyOfRange(value, offset+intLen-len, offset+intLen), sign);
        }
    }



    private void keepLower(int n) {
        if (intLen >= n) {
            offset += intLen - n;
            intLen = n;
        }
    }





    void add(MutableBigIntegerMock addend) {
        int x = intLen;
        int y = addend.intLen;
        int resultLen = (intLen > addend.intLen ? intLen : addend.intLen);
        int[] result = (value.length < resultLen ? new int[resultLen] : value);

        int rstart = result.length-1;
        long sum;
        long carry = 0;

        while(x > 0 && y > 0) {
            x--; y--;
            sum = (value[x+offset] & LONG_MASK) +
                    (addend.value[y+addend.offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        while(x > 0) {
            x--;
            if (carry == 0 && result == value && rstart == (x + offset))
                return;
            sum = (value[x+offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while(y > 0) {
            y--;
            sum = (addend.value[y+addend.offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        if (carry > 0) { // Result must grow in length
            resultLen++;
            if (result.length < resultLen) {
                int temp[] = new int[resultLen];


                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }





    void addShifted(MutableBigIntegerMock addend, int n) {
        if (addend.isZero()) {
            return;
        }

        int x = intLen;
        int y = addend.intLen + n;
        int resultLen = (intLen > y ? intLen : y);
        int[] result = (value.length < resultLen ? new int[resultLen] : value);

        int rstart = result.length-1;
        long sum;
        long carry = 0;

        while (x > 0 && y > 0) {
            x--; y--;
            int bval = y+addend.offset < addend.value.length ? addend.value[y+addend.offset] : 0;
            sum = (value[x+offset] & LONG_MASK) +
                    (bval & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        while (x > 0) {
            x--;
            if (carry == 0 && result == value && rstart == (x + offset)) {
                return;
            }
            sum = (value[x+offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (y > 0) {
            y--;
            int bval = y+addend.offset < addend.value.length ? addend.value[y+addend.offset] : 0;
            sum = (bval & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        if (carry > 0) { // Result must grow in length
            resultLen++;
            if (result.length < resultLen) {
                int temp[] = new int[resultLen];


                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }





    void addDisjoint(MutableBigIntegerMock addend, int n) {
        if (addend.isZero())
            return;

        int x = intLen;
        int y = addend.intLen + n;
        int resultLen = (intLen > y ? intLen : y);
        int[] result;
        if (value.length < resultLen)
            result = new int[resultLen];
        else {
            result = value;
            Arrays.fill(value, offset+intLen, value.length, 0);
        }

        int rstart = result.length-1;

        System.arraycopy(value, offset, result, rstart+1-x, x);
        y -= x;
        rstart -= x;

        int len = Math.min(y, addend.value.length-addend.offset);
        System.arraycopy(addend.value, addend.offset, result, rstart+1-y, len);

        for (int i=rstart+1-y+len; i < rstart+1; i++)
            result[i] = 0;

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }



    void addLower(MutableBigIntegerMock addend, int n) {
        MutableBigIntegerMock a = new MutableBigIntegerMock(addend);
        if (a.offset + a.intLen >= n) {
            a.offset = a.offset + a.intLen - n;
            a.intLen = n;
        }
        a.normalize();
        add(a);
    }




    int subtract(MutableBigIntegerMock b) {
        MutableBigIntegerMock a = this;

        int[] result = value;
        int sign = a.compare(b);

        if (sign == 0) {
            reset();
            return 0;
        }
        if (sign < 0) {
            MutableBigIntegerMock tmp = a;
            a = b;
            b = tmp;
        }

        int resultLen = a.intLen;
        if (result.length < resultLen)
            result = new int[resultLen];

        long diff = 0;
        int x = a.intLen;
        int y = b.intLen;
        int rstart = result.length - 1;

        while (y > 0) {
            x--; y--;

            diff = (a.value[x+a.offset] & LONG_MASK) -
                    (b.value[y+b.offset] & LONG_MASK) - ((int)-(diff>>32));
            result[rstart--] = (int)diff;
        }

        while (x > 0) {
            x--;
            diff = (a.value[x+a.offset] & LONG_MASK) - ((int)-(diff>>32));
            result[rstart--] = (int)diff;
        }

        value = result;
        intLen = resultLen;
        offset = value.length - resultLen;
        normalize();
        return sign;
    }





    private int difference(MutableBigIntegerMock b) {
        MutableBigIntegerMock a = this;
        int sign = a.compare(b);
        if (sign == 0)
            return 0;
        if (sign < 0) {
            MutableBigIntegerMock tmp = a;
            a = b;
            b = tmp;
        }

        long diff = 0;
        int x = a.intLen;
        int y = b.intLen;

        while (y > 0) {
            x--; y--;
            diff = (a.value[a.offset+ x] & LONG_MASK) -
                    (b.value[b.offset+ y] & LONG_MASK) - ((int)-(diff>>32));
            a.value[a.offset+x] = (int)diff;
        }

        while (x > 0) {
            x--;
            diff = (a.value[a.offset+ x] & LONG_MASK) - ((int)-(diff>>32));
            a.value[a.offset+x] = (int)diff;
        }

        a.normalize();
        return sign;
    }




    void multiply(MutableBigIntegerMock y, MutableBigIntegerMock z) {
        int xLen = intLen;
        int yLen = y.intLen;
        int newLen = xLen + yLen;

        if (z.value.length < newLen)
            z.value = new int[newLen];
        z.offset = 0;
        z.intLen = newLen;

        long carry = 0;
        for (int j=yLen-1, k=yLen+xLen-1; j >= 0; j--, k--) {
            long product = (y.value[j+y.offset] & LONG_MASK) *
                    (value[xLen-1+offset] & LONG_MASK) + carry;
            z.value[k] = (int)product;
            carry = product >>> 32;
        }
        z.value[xLen-1] = (int)carry;

        for (int i = xLen-2; i >= 0; i--) {
            carry = 0;
            for (int j=yLen-1, k=yLen+i; j >= 0; j--, k--) {
                long product = (y.value[j+y.offset] & LONG_MASK) *
                        (value[i+offset] & LONG_MASK) +
                        (z.value[k] & LONG_MASK) + carry;
                z.value[k] = (int)product;
                carry = product >>> 32;
            }
            z.value[i] = (int)carry;
        }

        z.normalize();
    }




    void mul(int y, MutableBigIntegerMock z) {
        if (y == 1) {
            z.copyValue(this);
            return;
        }

        if (y == 0) {
            z.clear();
            return;
        }

        long ylong = y & LONG_MASK;
        int[] zval = (z.value.length < intLen+1 ? new int[intLen + 1]
                : z.value);
        long carry = 0;
        for (int i = intLen-1; i >= 0; i--) {
            long product = ylong * (value[i+offset] & LONG_MASK) + carry;
            zval[i+1] = (int)product;
            carry = product >>> 32;
        }

        if (carry == 0) {
            z.offset = 1;
            z.intLen = intLen;
        } else {
            z.offset = 0;
            z.intLen = intLen + 1;
            zval[0] = (int)carry;
        }
        z.value = zval;
    }








    int divideOneWord(int divisor, MutableBigIntegerMock quotient) {
        long divisorLong = divisor & LONG_MASK;

        if (intLen == 1) {
            long dividendValue = value[offset] & LONG_MASK;
            int q = (int) (dividendValue / divisorLong);
            int r = (int) (dividendValue - q * divisorLong);
            quotient.value[0] = q;
            quotient.intLen = (q == 0) ? 0 : 1;
            quotient.offset = 0;
            return r;
        }

        if (quotient.value.length < intLen)
            quotient.value = new int[intLen];
        quotient.offset = 0;
        quotient.intLen = intLen;

        int shift = Integer.numberOfLeadingZeros(divisor);

        int rem = value[offset];
        long remLong = rem & LONG_MASK;
        if (remLong < divisorLong) {
            quotient.value[0] = 0;
        } else {
            quotient.value[0] = (int)(remLong / divisorLong);
            rem = (int) (remLong - (quotient.value[0] * divisorLong));
            remLong = rem & LONG_MASK;
        }
        int xlen = intLen;
        while (--xlen > 0) {
            long dividendEstimate = (remLong << 32) |
                    (value[offset + intLen - xlen] & LONG_MASK);
            int q;
            if (dividendEstimate >= 0) {
                q = (int) (dividendEstimate / divisorLong);
                rem = (int) (dividendEstimate - q * divisorLong);
            } else {
                long tmp = divWord(dividendEstimate, divisor);
                q = (int) (tmp & LONG_MASK);
                rem = (int) (tmp >>> 32);
            }
            quotient.value[intLen - xlen] = q;
            remLong = rem & LONG_MASK;
        }

        quotient.normalize();

        if (shift > 0)
            return rem % divisor;
        else
            return rem;
    }





    MutableBigIntegerMock divide(MutableBigIntegerMock b, MutableBigIntegerMock quotient) {
        return divide(b,quotient,true);
    }

    MutableBigIntegerMock divide(MutableBigIntegerMock b, MutableBigIntegerMock quotient, boolean needRemainder) {
        if (b.intLen < MockBigInteger.BURNIKEL_ZIEGLER_THRESHOLD ||
                intLen - b.intLen < MockBigInteger.BURNIKEL_ZIEGLER_OFFSET) {
            return divideKnuth(b, quotient, needRemainder);
        } else {
            return divideAndRemainderBurnikelZiegler(b, quotient);
        }
    }



    MutableBigIntegerMock divideKnuth(MutableBigIntegerMock b, MutableBigIntegerMock quotient) {
        return divideKnuth(b,quotient,true);
    }











    MutableBigIntegerMock divideKnuth(MutableBigIntegerMock b, MutableBigIntegerMock quotient, boolean needRemainder) {
        if (b.intLen == 0)
            throw new ArithmeticException("MockBigInteger divide by zero");

        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigIntegerMock() : null;
        }

        int cmp = compare(b);

        if (cmp < 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigIntegerMock(this) : null;
        }

        if (cmp == 0) {
            quotient.value[0] = quotient.intLen = 1;
            quotient.offset = 0;
            return needRemainder ? new MutableBigIntegerMock() : null;
        }

        quotient.clear();

        if (b.intLen == 1) {
            int r = divideOneWord(b.value[b.offset], quotient);
            if(needRemainder) {
                if (r == 0)
                    return new MutableBigIntegerMock();
                return new MutableBigIntegerMock(r);
            } else {
                return null;
            }
        }

        if (intLen >= KNUTH_POW2_THRESH_LEN) {
            int trailingZeroBits = Math.min(getLowestSetBit(), b.getLowestSetBit());
            if (trailingZeroBits >= KNUTH_POW2_THRESH_ZEROS*32) {
                MutableBigIntegerMock a = new MutableBigIntegerMock(this);
                b = new MutableBigIntegerMock(b);
                a.rightShift(trailingZeroBits);
                b.rightShift(trailingZeroBits);
                MutableBigIntegerMock r = a.divideKnuth(b, quotient);
                r.leftShift(trailingZeroBits);
                return r;
            }
        }

        return divideMagnitude(b, quotient, needRemainder);
    }











    MutableBigIntegerMock divideAndRemainderBurnikelZiegler(MutableBigIntegerMock b, MutableBigIntegerMock quotient) {
        int r = intLen;
        int s = b.intLen;

        quotient.offset = quotient.intLen = 0;

        if (r < s) {
            return this;
        } else {




            int m = 1 << (32-Integer.numberOfLeadingZeros(s/ MockBigInteger.BURNIKEL_ZIEGLER_THRESHOLD));

            int j = (s+m-1) / m;      // step 2a: j = ceil(s/m)
            int n = j * m;            // step 2b: block length in 32-bit units
            long n32 = 32L * n;         // block length in bits
            int sigma = (int) Math.max(0, n32 - b.bitLength());   // step 3: sigma = max{T | (2^T)*B < beta^n}
            MutableBigIntegerMock bShifted = new MutableBigIntegerMock(b);
            bShifted.safeLeftShift(sigma);   // step 4a: shift b so its length is a multiple of n
            MutableBigIntegerMock aShifted = new MutableBigIntegerMock(this);
            aShifted.safeLeftShift(sigma);     // step 4b: shift a by the same amount

            int t = (int) ((aShifted.bitLength()+n32) / n32);
            if (t < 2) {
                t = 2;
            }

            MutableBigIntegerMock a1 = aShifted.getBlock(t-1, t, n);   // the most significant block of a

            MutableBigIntegerMock z = aShifted.getBlock(t-2, t, n);    // the second to most significant block
            z.addDisjoint(a1, n);   // z[t-2]

            MutableBigIntegerMock qi = new MutableBigIntegerMock();
            MutableBigIntegerMock ri;
            for (int i=t-2; i > 0; i--) {

                ri = z.divide2n1n(bShifted, qi);

                z = aShifted.getBlock(i-1, t, n);   // a[i-1]
                z.addDisjoint(ri, n);
                quotient.addShifted(qi, i*n);   // update q (part of step 9)
            }

            ri = z.divide2n1n(bShifted, qi);
            quotient.add(qi);

            ri.rightShift(sigma);   // step 9: a and b were shifted, so shift back
            return ri;
        }
    }










    private MutableBigIntegerMock divide2n1n(MutableBigIntegerMock b, MutableBigIntegerMock quotient) {
        int n = b.intLen;

        if (n%2 != 0 || n < MockBigInteger.BURNIKEL_ZIEGLER_THRESHOLD) {
            return divideKnuth(b, quotient);
        }

        MutableBigIntegerMock aUpper = new MutableBigIntegerMock(this);
        aUpper.safeRightShift(32*(n/2));   // aUpper = [a1,a2,a3]
        keepLower(n/2);   // this = a4

        MutableBigIntegerMock q1 = new MutableBigIntegerMock();
        MutableBigIntegerMock r1 = aUpper.divide3n2n(b, q1);

        addDisjoint(r1, n/2);   // this = [r1,this]
        MutableBigIntegerMock r2 = divide3n2n(b, quotient);

        quotient.addDisjoint(q1, n/2);
        return r2;
    }









    private MutableBigIntegerMock divide3n2n(MutableBigIntegerMock b, MutableBigIntegerMock quotient) {
        int n = b.intLen / 2;   // half the length of b in ints

        MutableBigIntegerMock a12 = new MutableBigIntegerMock(this);
        a12.safeRightShift(32*n);

        MutableBigIntegerMock b1 = new MutableBigIntegerMock(b);
        b1.safeRightShift(n * 32);
        MockBigInteger b2 = b.getLower(n);

        MutableBigIntegerMock r;
        MutableBigIntegerMock d;
        if (compareShifted(b, n) < 0) {

            r = a12.divide2n1n(b1, quotient);

            d = new MutableBigIntegerMock(quotient.toBigInteger().multiply(b2));
        } else {

            quotient.ones(n);
            a12.add(b1);
            b1.leftShift(32*n);
            a12.subtract(b1);
            r = a12;

            d = new MutableBigIntegerMock(b2);
            d.leftShift(32 * n);
            d.subtract(new MutableBigIntegerMock(b2));
        }


        r.leftShift(32 * n);
        r.addLower(this, n);

        while (r.compare(d) < 0) {
            r.add(b);
            quotient.subtract(MutableBigIntegerMock.ONE);
        }
        r.subtract(d);

        return r;
    }









    private MutableBigIntegerMock getBlock(int index, int numBlocks, int blockLength) {
        int blockStart = index * blockLength;
        if (blockStart >= intLen) {
            return new MutableBigIntegerMock();
        }

        int blockEnd;
        if (index == numBlocks-1) {
            blockEnd = intLen;
        } else {
            blockEnd = (index+1) * blockLength;
        }
        if (blockEnd > intLen) {
            return new MutableBigIntegerMock();
        }

        int[] newVal = Arrays.copyOfRange(value, offset+intLen-blockEnd, offset+intLen-blockStart);
        return new MutableBigIntegerMock(newVal);
    }

    long bitLength() {
        if (intLen == 0)
            return 0;
        return intLen*32L - Integer.numberOfLeadingZeros(value[offset]);
    }







    long divide(long v, MutableBigIntegerMock quotient) {
        if (v == 0)
            throw new ArithmeticException("MockBigInteger divide by zero");

        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return 0;
        }
        if (v < 0)
            v = -v;

        int d = (int)(v >>> 32);
        quotient.clear();

        if (d == 0)
            return divideOneWord((int)v, quotient) & LONG_MASK;
        else {
            return divideLongMagnitude(v, quotient).toLong();
        }
    }

    private static void copyAndShift(int[] src, int srcFrom, int srcLen, int[] dst, int dstFrom, int shift) {
        int n2 = 32 - shift;
        int c=src[srcFrom];
        for (int i=0; i < srcLen-1; i++) {
            int b = c;
            c = src[++srcFrom];
            dst[dstFrom+i] = (b << shift) | (c >>> n2);
        }
        dst[dstFrom+srcLen-1] = c << shift;
    }





    private MutableBigIntegerMock divideMagnitude(MutableBigIntegerMock div,
                                                  MutableBigIntegerMock quotient,
                                                  boolean needRemainder ) {


        int shift = Integer.numberOfLeadingZeros(div.value[div.offset]);

        final int dlen = div.intLen;
        int[] divisor;
        MutableBigIntegerMock rem; // Remainder starts as dividend with space for a leading zero
        if (shift > 0) {
            divisor = new int[dlen];
            copyAndShift(div.value,div.offset,dlen,divisor,0,shift);
            if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
                int[] remarr = new int[intLen + 1];
                rem = new MutableBigIntegerMock(remarr);
                rem.intLen = intLen;
                rem.offset = 1;
                copyAndShift(value,offset,intLen,remarr,1,shift);
            } else {
                int[] remarr = new int[intLen + 2];
                rem = new MutableBigIntegerMock(remarr);
                rem.intLen = intLen+1;
                rem.offset = 1;
                int rFrom = offset;
                int c=0;
                int n2 = 32 - shift;
                for (int i=1; i < intLen+1; i++,rFrom++) {
                    int b = c;
                    c = value[rFrom];
                    remarr[i] = (b << shift) | (c >>> n2);
                }
                remarr[intLen+1] = c << shift;
            }
        } else {
            divisor = Arrays.copyOfRange(div.value, div.offset, div.offset + div.intLen);
            rem = new MutableBigIntegerMock(new int[intLen + 1]);
            System.arraycopy(value, offset, rem.value, 1, intLen);
            rem.intLen = intLen;
            rem.offset = 1;
        }

        int nlen = rem.intLen;

        final int limit = nlen - dlen + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;

        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen++;
        }

        int dh = divisor[0];
        long dhLong = dh & LONG_MASK;
        int dl = divisor[1];

        for (int j=0; j < limit-1; j++) {


            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j+rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j+1+rem.offset];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long)nh) << 32) | (nm & LONG_MASK);
                if (nChunk >= 0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - (qhat * dhLong));
                } else {
                    long tmp = divWord(nChunk, dh);
                    qhat = (int) (tmp & LONG_MASK);
                    qrem = (int) (tmp >>> 32);
                }
            }

            if (qhat == 0)
                continue;

            if (!skipCorrection) { // Correct qhat
                long nl = rem.value[j+2+rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int)((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >=  dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            rem.value[j+rem.offset] = 0;
            int borrow = mulsub(rem.value, divisor, qhat, dlen, j+rem.offset);

            if (borrow + 0x80000000 > nh2) {

                divadd(divisor, rem.value, j+1+rem.offset);
                qhat--;
            }

            q[j] = qhat;
        } // D7 loop on j


        int qhat = 0;
        int qrem = 0;
        boolean skipCorrection = false;
        int nh = rem.value[limit - 1 + rem.offset];
        int nh2 = nh + 0x80000000;
        int nm = rem.value[limit + rem.offset];

        if (nh == dh) {
            qhat = ~0;
            qrem = nh + nm;
            skipCorrection = qrem + 0x80000000 < nh2;
        } else {
            long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
            if (nChunk >= 0) {
                qhat = (int) (nChunk / dhLong);
                qrem = (int) (nChunk - (qhat * dhLong));
            } else {
                long tmp = divWord(nChunk, dh);
                qhat = (int) (tmp & LONG_MASK);
                qrem = (int) (tmp >>> 32);
            }
        }
        if (qhat != 0) {
            if (!skipCorrection) { // Correct qhat
                long nl = rem.value[limit + 1 + rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            int borrow;
            rem.value[limit - 1 + rem.offset] = 0;
            if(needRemainder)
                borrow = mulsub(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);
            else
                borrow = mulsubBorrow(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);

            if (borrow + 0x80000000 > nh2) {

                if(needRemainder)
                    divadd(divisor, rem.value, limit - 1 + 1 + rem.offset);
                qhat--;
            }

            q[(limit - 1)] = qhat;
        }


        if (needRemainder) {

            if (shift > 0)
                rem.rightShift(shift);
            rem.normalize();
        }
        quotient.normalize();
        return needRemainder ? rem : null;
    }





    private MutableBigIntegerMock divideLongMagnitude(long ldivisor, MutableBigIntegerMock quotient) {

        MutableBigIntegerMock rem = new MutableBigIntegerMock(new int[intLen + 1]);
        System.arraycopy(value, offset, rem.value, 1, intLen);
        rem.intLen = intLen;
        rem.offset = 1;

        int nlen = rem.intLen;

        int limit = nlen - 2 + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;

        int shift = Long.numberOfLeadingZeros(ldivisor);
        if (shift > 0) {
            ldivisor<<=shift;
            rem.leftShift(shift);
        }

        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen++;
        }

        int dh = (int)(ldivisor >>> 32);
        long dhLong = dh & LONG_MASK;
        int dl = (int)(ldivisor & LONG_MASK);

        for (int j = 0; j < limit; j++) {


            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j + rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j + 1 + rem.offset];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
                if (nChunk >= 0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - (qhat * dhLong));
                } else {
                    long tmp = divWord(nChunk, dh);
                    qhat =(int)(tmp & LONG_MASK);
                    qrem = (int)(tmp>>>32);
                }
            }

            if (qhat == 0)
                continue;

            if (!skipCorrection) { // Correct qhat
                long nl = rem.value[j + 2 + rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            rem.value[j + rem.offset] = 0;
            int borrow = mulsubLong(rem.value, dh, dl, qhat,  j + rem.offset);

            if (borrow + 0x80000000 > nh2) {

                divaddLong(dh,dl, rem.value, j + 1 + rem.offset);
                qhat--;
            }

            q[j] = qhat;
        } // D7 loop on j

        if (shift > 0)
            rem.rightShift(shift);

        quotient.normalize();
        rem.normalize();
        return rem;
    }





    private int divaddLong(int dh, int dl, int[] result, int offset) {
        long carry = 0;

        long sum = (dl & LONG_MASK) + (result[1+offset] & LONG_MASK);
        result[1+offset] = (int)sum;

        sum = (dh & LONG_MASK) + (result[offset] & LONG_MASK) + carry;
        result[offset] = (int)sum;
        carry = sum >>> 32;
        return (int)carry;
    }





    private int mulsubLong(int[] q, int dh, int dl, int x, int offset) {
        long xLong = x & LONG_MASK;
        offset += 2;
        long product = (dl & LONG_MASK) * xLong;
        long difference = q[offset] - product;
        q[offset--] = (int)difference;
        long carry = (product >>> 32)
                + (((difference & LONG_MASK) >
                (((~(int)product) & LONG_MASK))) ? 1:0);
        product = (dh & LONG_MASK) * xLong + carry;
        difference = q[offset] - product;
        q[offset--] = (int)difference;
        carry = (product >>> 32)
                + (((difference & LONG_MASK) >
                (((~(int)product) & LONG_MASK))) ? 1:0);
        return (int)carry;
    }




    private boolean unsignedLongCompare(long one, long two) {
        return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE);
    }







    static long divWord(long n, int d) {
        long dLong = d & LONG_MASK;
        long r;
        long q;
        if (dLong == 1) {
            q = (int)n;
            r = 0;
            return (r << 32) | (q & LONG_MASK);
        }

        q = (n >>> 1) / (dLong >>> 1);
        r = n - q*dLong;

        while (r < 0) {
            r += dLong;
            q--;
        }
        while (r >= dLong) {
            r -= dLong;
            q++;
        }

        return (r << 32) | (q & LONG_MASK);
    }














    MutableBigIntegerMock sqrt() {

        if (this.isZero()) {
            return new MutableBigIntegerMock(0);
        } else if (this.value.length == 1
                && (this.value[0] & LONG_MASK) < 4) { // result is unity
            return ONE;
        }

        if (bitLength() <= 63) {

            long v = new MockBigInteger(this.value, 1).longValueExact();
            long xk = (long)Math.floor(Math.sqrt(v));

            do {
                long xk1 = (xk + v/xk)/2;

                if (xk1 >= xk) {
                    return new MutableBigIntegerMock(new int[] {
                            (int)(xk >>> 32), (int)(xk & LONG_MASK)
                    });
                }

                xk = xk1;
            } while (true);
        } else {


            int bitLength = (int) this.bitLength();
            if (bitLength != this.bitLength()) {
                throw new ArithmeticException("bitLength() integer overflow");
            }

            int shift = bitLength - 63;
            if (shift % 2 == 1) {
                shift++;
            }

            MutableBigIntegerMock xk = new MutableBigIntegerMock(this);
            xk.rightShift(shift);
            xk.normalize();

            double d = new MockBigInteger(xk.value, 1).doubleValue();
            MockBigInteger bi = MockBigInteger.valueOf((long)Math.ceil(Math.sqrt(d)));
            xk = new MutableBigIntegerMock(bi.mag);

            xk.leftShift(shift / 2);

            MutableBigIntegerMock xk1 = new MutableBigIntegerMock();
            do {

                this.divide(xk, xk1, false);
                xk1.add(xk);
                xk1.rightShift(1);

                if (xk1.compare(xk) >= 0) {
                    return xk;
                }

                xk.copyValue(xk1);

                xk1.reset();
            } while (true);
        }
    }



    MutableBigIntegerMock hybridGCD(MutableBigIntegerMock b) {


        MutableBigIntegerMock a = this;
        MutableBigIntegerMock q = new MutableBigIntegerMock();

        while (b.intLen != 0) {
            if (Math.abs(a.intLen - b.intLen) < 2)
                return a.binaryGCD(b);

            MutableBigIntegerMock r = a.divide(b, q);
            a = b;
            b = r;
        }
        return a;
    }




    private MutableBigIntegerMock binaryGCD(MutableBigIntegerMock v) {

        MutableBigIntegerMock u = this;
        MutableBigIntegerMock r = new MutableBigIntegerMock();

        int s1 = u.getLowestSetBit();
        int s2 = v.getLowestSetBit();
        int k = (s1 < s2) ? s1 : s2;
        if (k != 0) {
            u.rightShift(k);
            v.rightShift(k);
        }

        boolean uOdd = (k == s1);
        MutableBigIntegerMock t = uOdd ? v: u;
        int tsign = uOdd ? -1 : 1;

        int lb;
        while ((lb = t.getLowestSetBit()) >= 0) {

            t.rightShift(lb);

            if (tsign > 0)
                u = t;
            else
                v = t;

            if (u.intLen < 2 && v.intLen < 2) {
                int x = u.value[u.offset];
                int y = v.value[v.offset];
                x  = binaryGcd(x, y);
                r.value[0] = x;
                r.intLen = 1;
                r.offset = 0;
                if (k > 0)
                    r.leftShift(k);
                return r;
            }

            if ((tsign = u.difference(v)) == 0)
                break;
            t = (tsign >= 0) ? u : v;
        }

        if (k > 0)
            u.leftShift(k);
        return u;
    }



    static int binaryGcd(int a, int b) {
        if (b == 0)
            return a;
        if (a == 0)
            return b;

        int aZeros = Integer.numberOfTrailingZeros(a);
        int bZeros = Integer.numberOfTrailingZeros(b);
        a >>>= aZeros;
        b >>>= bZeros;

        int t = (aZeros < bZeros ? aZeros : bZeros);

        while (a != b) {
            if ((a+0x80000000) > (b+0x80000000)) {  // a > b as unsigned
                a -= b;
                a >>>= Integer.numberOfTrailingZeros(a);
            } else {
                b -= a;
                b >>>= Integer.numberOfTrailingZeros(b);
            }
        }
        return a<<t;
    }




    MutableBigIntegerMock mutableModInverse(MutableBigIntegerMock p) {

        if (p.isOdd())
            return modInverse(p);

        if (isEven())
            throw new ArithmeticException("MockBigInteger not invertible.");

        int powersOf2 = p.getLowestSetBit();

        MutableBigIntegerMock oddMod = new MutableBigIntegerMock(p);
        oddMod.rightShift(powersOf2);

        if (oddMod.isOne())
            return modInverseMP2(powersOf2);

        MutableBigIntegerMock oddPart = modInverse(oddMod);

        MutableBigIntegerMock evenPart = modInverseMP2(powersOf2);

        MutableBigIntegerMock y1 = modInverseBP2(oddMod, powersOf2);
        MutableBigIntegerMock y2 = oddMod.modInverseMP2(powersOf2);

        MutableBigIntegerMock temp1 = new MutableBigIntegerMock();
        MutableBigIntegerMock temp2 = new MutableBigIntegerMock();
        MutableBigIntegerMock result = new MutableBigIntegerMock();

        oddPart.leftShift(powersOf2);
        oddPart.multiply(y1, result);

        evenPart.multiply(oddMod, temp1);
        temp1.multiply(y2, temp2);

        result.add(temp2);
        return result.divide(p, temp1);
    }



    MutableBigIntegerMock modInverseMP2(int k) {
        if (isEven())
            throw new ArithmeticException("Non-invertible. (GCD != 1)");

        if (k > 64)
            return euclidModInverse(k);

        int t = inverseMod32(value[offset+intLen-1]);

        if (k < 33) {
            t = (k == 32 ? t : t & ((1 << k) - 1));
            return new MutableBigIntegerMock(t);
        }

        long pLong = (value[offset+intLen-1] & LONG_MASK);
        if (intLen > 1)
            pLong |=  ((long)value[offset+intLen-2] << 32);
        long tLong = t & LONG_MASK;
        tLong = tLong * (2 - pLong * tLong);  // 1 more Newton iter step
        tLong = (k == 64 ? tLong : tLong & ((1L << k) - 1));

        MutableBigIntegerMock result = new MutableBigIntegerMock(new int[2]);
        result.value[0] = (int)(tLong >>> 32);
        result.value[1] = (int)tLong;
        result.intLen = 2;
        result.normalize();
        return result;
    }



    static int inverseMod32(int val) {

        int t = val;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        return t;
    }



    static long inverseMod64(long val) {

        long t = val;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        assert(t * val == 1);
        return t;
    }



    static MutableBigIntegerMock modInverseBP2(MutableBigIntegerMock mod, int k) {

        return fixup(new MutableBigIntegerMock(1), new MutableBigIntegerMock(mod), k);
    }









    private MutableBigIntegerMock modInverse(MutableBigIntegerMock mod) {
        MutableBigIntegerMock p = new MutableBigIntegerMock(mod);
        MutableBigIntegerMock f = new MutableBigIntegerMock(this);
        MutableBigIntegerMock g = new MutableBigIntegerMock(p);
        SignedMutableBigInteger c = new SignedMutableBigInteger(1);
        SignedMutableBigInteger d = new SignedMutableBigInteger();
        MutableBigIntegerMock temp = null;
        SignedMutableBigInteger sTemp = null;

        int k = 0;

        if (f.isEven()) {
            int trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k = trailingZeros;
        }

        while (!f.isOne()) {

            if (f.isZero())
                throw new ArithmeticException("MockBigInteger not invertible.");

            if (f.compare(g) < 0) {
                temp = f; f = g; g = temp;
                sTemp = d; d = c; c = sTemp;
            }

            if (((f.value[f.offset + f.intLen - 1] ^
                    g.value[g.offset + g.intLen - 1]) & 3) == 0) {
                f.subtract(g);
                c.signedSubtract(d);
            } else { // If f != g (mod 4)
                f.add(g);
                c.signedAdd(d);
            }

            int trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k += trailingZeros;
        }

        if (c.compare(p) >= 0) {
            MutableBigIntegerMock remainder = c.divide(p,
                    new MutableBigIntegerMock());



            c.copyValue(remainder);
        }

        if (c.sign < 0) {
            c.signedAdd(p);
        }

        return fixup(c, p, k);
    }





    static MutableBigIntegerMock fixup(MutableBigIntegerMock c, MutableBigIntegerMock p,
                                       int k) {
        MutableBigIntegerMock temp = new MutableBigIntegerMock();

        int r = -inverseMod32(p.value[p.offset+p.intLen-1]);

        for (int i=0, numWords = k >> 5; i < numWords; i++) {

            int  v = r * c.value[c.offset + c.intLen-1];

            p.mul(v, temp);
            c.add(temp);

            c.intLen--;
        }
        int numBits = k & 0x1f;
        if (numBits != 0) {

            int v = r * c.value[c.offset + c.intLen-1];
            v &= ((1<<numBits) - 1);

            p.mul(v, temp);
            c.add(temp);

            c.rightShift(numBits);
        }

        if (c.compare(p) >= 0)
            c = c.divide(p, new MutableBigIntegerMock());

        return c;
    }




    MutableBigIntegerMock euclidModInverse(int k) {
        MutableBigIntegerMock b = new MutableBigIntegerMock(1);
        b.leftShift(k);
        MutableBigIntegerMock mod = new MutableBigIntegerMock(b);

        MutableBigIntegerMock a = new MutableBigIntegerMock(this);
        MutableBigIntegerMock q = new MutableBigIntegerMock();
        MutableBigIntegerMock r = b.divide(a, q);

        MutableBigIntegerMock swapper = b;

        b = r;
        r = swapper;

        MutableBigIntegerMock t1 = new MutableBigIntegerMock(q);
        MutableBigIntegerMock t0 = new MutableBigIntegerMock(1);
        MutableBigIntegerMock temp = new MutableBigIntegerMock();

        while (!b.isOne()) {
            r = a.divide(b, q);

            if (r.intLen == 0)
                throw new ArithmeticException("MockBigInteger not invertible.");

            swapper = r;
            a = swapper;

            if (q.intLen == 1)
                t1.mul(q.value[q.offset], temp);
            else
                q.multiply(t1, temp);
            swapper = q;
            q = temp;
            temp = swapper;
            t0.add(q);

            if (a.isOne())
                return t0;

            r = b.divide(a, q);

            if (r.intLen == 0)
                throw new ArithmeticException("MockBigInteger not invertible.");

            swapper = b;
            b =  r;

            if (q.intLen == 1)
                t0.mul(q.value[q.offset], temp);
            else
                q.multiply(t0, temp);
            swapper = q; q = temp; temp = swapper;

            t1.add(q);
        }
        mod.subtract(t1);
        return mod;
    }
}
