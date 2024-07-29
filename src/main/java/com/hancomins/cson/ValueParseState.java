package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.MockBigInteger;
import com.hancomins.cson.util.NumberConversionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentLinkedDeque;

class ValueParseState {

    private static ConcurrentLinkedDeque<CharacterBuffer> characterBufferPool = new ConcurrentLinkedDeque<>();

    private CharacterBuffer characterBuffer;

    private static final char[] NaNSign = new char[]{'n','a','n'}; // 3
    private static final char[] Null = new char[]{'n','u','l','l'}; // 4
    private static final char[] TrueSign = new char[]{'t','r','u','e'}; // 4
    private static final char[] FalseSign = new char[]{'f','a','l','s','e'}; // 5
    private static final char[] InfinitySign = new char[]{'i','n','f','i','n','i','t','y'}; // 8
    private static final char[] HexadecimalSign = new char[]{'0','x'};






    enum DoubtMode {
        None,
        NaN,
        Null,
        Infinity,
        NegativeInfinity,
        Hexadecimal,
        Exponential,
        Number,
        True,
        False,
        String
    }

    boolean isAppearRealNumberSing = false;
    //boolean isAppearSign = false;
    boolean isAppearExponentialSign = false;
    boolean noneZeroStart = false;
    boolean isSpecialChar = false;
    boolean unicodeChar = false;
    boolean unicodeExtend = false;
    int unicodeCharCount = 0;



    private boolean allowNaN = false;
    private boolean allowInfinity = false;
    private boolean allowHexadecimal = false;
    private boolean leadingZeroOmission = false;
    private boolean allowPositiveSign = false;
    private boolean ignoreNonNumeric = false;

    private boolean trimResult = false;



    private boolean isoCtrlInNumber = false;

    /**
     * 제어문자를 허용할 것인지 여부를 설정한다.
     */
    private boolean allowControlChar = false;



    private DoubtMode doubtMode_ = DoubtMode.Number;
    private DoubtMode lastDoubtMode = DoubtMode.None;



    private boolean onlyString = false;

    private String resultString;
    private Number resultNumber;
    private Boolean resultBoolean;



    ValueParseState(NumberConversionUtil.NumberConversionOption numberConversionOption) {
        characterBuffer = new CharacterBuffer();
        allowNaN = numberConversionOption.isAllowNaN();
        allowInfinity = numberConversionOption.isAllowInfinity();
        allowHexadecimal = numberConversionOption.isAllowHexadecimal();
        leadingZeroOmission = numberConversionOption.isLeadingZeroOmission();
        allowPositiveSign = numberConversionOption.isAllowPositiveSing();
        ignoreNonNumeric = numberConversionOption.isIgnoreNonNumeric();
    }

    public ValueParseState setAllowControlChar(boolean allowControlChar) {
        this.allowControlChar = allowControlChar;
        return this;
    }

    /**
     * 숫자만을 허용할 것인지 여부를 설정한다.
     * @param onlyNumber
     */
    public void setOnlyNumber(boolean onlyNumber) {
        //this.ignoreNonNumeric = onlyNumber;
    }

    public ValueParseState setOnlyString(boolean onlyString) {
        this.doubtMode = DoubtMode.String;
        this.onlyString = onlyString;
        return this;
    }

    public void setTrimResult(boolean trimResult) {
        this.trimResult = trimResult;
    }


    /**
     * 오직 테스트에서만 사용한다.
     * @param value
     */
    void append(String value) {
        for(int i = 0; i < value.length(); ++i) {
            append(value.charAt(i));
        }
    }

    ValueParseState reset() {
        characterBuffer.reset();
        doubtMode = DoubtMode.Number;
        specialSymbolIndex = 0;
        isAppearRealNumberSing = false;
        isAppearSign = false;
        isAppearExponentialSign = false;
        noneZeroStart = false;

        isSpecialChar = false;
        unicodeChar = false;
        unicodeExtend = false;
        unicodeCharCount = 0;
        markStartUnicodeIndex = -1;
        isoCtrlInNumber = false;
        onlyString = false;
        return this;
    }

    void append(char c) {
        appendChar_(c);
    }


    private void readType() {
        char[] chars = characterBuffer.getChars();
        int end = characterBuffer.length();

        int start =0;

        for(int i = 0; i < end; ++i) {
            if(!isSpaceChar(chars[i])) {
                start = i;
                break;
            }
        }

        for(; start < end; --end) {
            if(!isSpaceChar(chars[end])) {
                ++end;
                break;
            }
        }
        final int len = end - start;
        int startDigitOffset = start;

        boolean negativeSign = false;

        DoubtMode doubtMode = this.doubtMode_;
        //int specialSymbolIndex = start;

        boolean isStart = true;
        for(int i = start; i < end; ++i) {
            char c = chars[i];
            if(isStart) {
                isStart = false;
                if(doubtMode == DoubtMode.String) {
                    resultString = new String(chars, start, len);
                    return;
                }
                char lowChar = Character.toLowerCase(c);
                if (allowNaN && NaNSign[i] == lowChar && len == NaNSign.length) {
                    doubtMode = DoubtMode.NaN;
                } else if (allowInfinity && InfinitySign[i] == lowChar && len == InfinitySign.length) {
                    doubtMode = DoubtMode.Infinity;
                } else if (allowHexadecimal && HexadecimalSign[i] == lowChar && len > 2) {
                    if(Character.toLowerCase(HexadecimalSign[i]) != Character.toLowerCase(chars[1])) {
                        doubtMode_ = DoubtMode.String;
                        return;
                    }
                    doubtMode = DoubtMode.Hexadecimal;
                    i = 2;
                } else if (TrueSign[i] == lowChar && len == TrueSign.length) {
                    doubtMode = DoubtMode.True;
                } else if (FalseSign[i] == lowChar  && len == FalseSign.length) {
                    doubtMode = DoubtMode.False;
                }
                /**
                 * + 부호가 나오면 숫자로 의심한다.
                 * 단, allowPositiveSign 이 true 일 경우에만 진행한다.
                 */
                else if (allowPositiveSign && c == '+' && len > 1) {
                    doubtMode = DoubtMode.Number;
                    ++i;
                    // + 부호 뒤에 빈 공간을 제거한다.
                    for(;i < end; ++i) {
                        c = chars[i];
                        if(!isSpaceChar(c)) {
                            --i;
                            break;
                        }
                    }
                    // infinity 의 앞 글자 i 가 나오면 일단 infinity 의심 모드로 전환한다.
                    if (allowInfinity && InfinitySign[i] == lowChar && len - i == InfinitySign.length) {
                        startDigitOffset = i;
                        doubtMode = DoubtMode.Infinity;
                    }
                }
                /**
                 * - 부호가 나오면 숫자로 의심한다.
                 */
                else if(c == '-'  && len > 1) {
                    doubtMode = DoubtMode.Number;
                    negativeSign = true;
                    // - 부호 뒤에 빈 공간을 제거한다.
                    for(;i < end; ++i) {
                        c = chars[i];
                        if(!isSpaceChar(c)) {
                            --i;
                            break;
                        }
                    }
                    // infinity 의 앞 글자 i 가 나오면 일단 infinity 의심 모드로 전환한다.
                    if (allowInfinity && InfinitySign[i] == lowChar && len - i == InfinitySign.length) {
                        startDigitOffset = i - start;
                        doubtMode = DoubtMode.Infinity;
                    }
                } else if(leadingZeroOmission && c == '.'  && len > 1){
                    doubtMode = DoubtMode.Number;
                    isAppearRealNumberSing = true;
                    noneZeroStart = true;
                } else if(!Character.isDigit(c)) {
                    doubtMode_ = DoubtMode.String;
                    resultString = new String(chars, start, len);
                    return;
                } else {
                    doubtMode = DoubtMode.Number;
                }
            } else {
                if (doubtMode == DoubtMode.NaN) {
                    if (NaNSign[i] != Character.toLowerCase(c) && Null[i] == Character.toLowerCase(c)) {
                        doubtMode = DoubtMode.Null;
                    } else if (i + 1 == NaNSign.length) {
                        resultNumber = Double.NaN;
                        doubtMode_ = doubtMode;
                        return;
                    } else {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    }
                }
                 else if(doubtMode == DoubtMode.Null) {
                    if(Null[i] != Character.toLowerCase(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    } else if(i + 1 ==  Null.length) {
                        resultNumber = null;
                        resultString = null;
                        doubtMode_ = doubtMode;
                        return;
                    }
                } else if(doubtMode == DoubtMode.True) {
                    if(TrueSign[i] != Character.toLowerCase(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    } else if(i + 1 == TrueSign.length) {
                        resultBoolean = Boolean.TRUE;
                        doubtMode_ = doubtMode;
                        return;
                    }
                } else if(doubtMode == DoubtMode.False) {
                    if(FalseSign[i] != Character.toLowerCase(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    } else if(i + 1 == FalseSign.length) {
                        resultBoolean = Boolean.FALSE;
                        doubtMode_ = doubtMode;
                        return;
                    }
                }
                else if(doubtMode == DoubtMode.Infinity) {
                    if(InfinitySign[i - startDigitOffset] != Character.toLowerCase(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    } else if(i + 1 == InfinitySign.length + startDigitOffset) {
                        resultNumber = negativeSign ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        doubtMode_ = doubtMode;
                        return;
                    }
                } else if(doubtMode == DoubtMode.Hexadecimal) {
                    if(!(c == '.' || c == 'e' || c == 'E' || Character.isDigit(c))) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                        return;
                    }

                } else if(doubtMode == DoubtMode.HexadecimalValue) {
                    if(!isHexadecimalChar(c)) {
                        doubtMode = DoubtMode.String;
                    }
                    appendChar(c);
                }
                else if(doubtMode == DoubtMode.Number) {
                    if(Character.isISOControl(c)) {
                        if(!allowControlChar) {
                            throw new CSONException(ExceptionMessages.getCtrlCharNotAllowed(c));
                        }
                        isoCtrlInNumber = true;
                        appendChar(c);
                        return;
                    }
                    if(c == '.') {
                        if(isAppearRealNumberSing) {
                            doubtMode = DoubtMode.String;
                        }
                        isAppearRealNumberSing = true;
                        appendChar(c);
                    } else if(c == 'e' || c == 'E') {
                        doubtMode = DoubtMode.Exponential;
                        appendChar(c);
                    } else if((c == 'x' || c == 'X') && allowHexadecimal && index == 2 && isAppearSign && characterBuffer.charAt(index - 1) == '0') {
                        doubtMode = DoubtMode.HexadecimalValue;
                        specialSymbolIndex = 2;
                        appendChar(c);
                    } else if((c == 'i' || c == 'I') && allowInfinity && index == 1 && isAppearSign) {
                        doubtMode = DoubtMode.Infinity;
                        specialSymbolIndex = 1;
                        appendChar(c);
                    }
                    else if(!Character.isDigit(c)) {
                        doubtMode = DoubtMode.String;
                        appendChar(c);
                    } else {
                        appendChar(c);
                    }
                } else if(doubtMode == DoubtMode.Exponential) {
                    if(c == '+' || c == '-') {
                        if(isAppearExponentialSign) {
                            doubtMode = DoubtMode.String;
                        }
                        isAppearExponentialSign = true;
                    } else if(!Character.isDigit(c)) {
                        doubtMode = DoubtMode.String;
                    }
                    appendChar(c);
                }
                else {
                    if(lastDoubtMode != DoubtMode.String && Character.isSpaceChar(c)) {
                        lastDoubtMode = doubtMode;
                    }
                    appendChar(c);
                }
            }
            ++index;
        }

    }



    int markStartUnicodeIndex = -1;

    boolean isSpecialChar() {
        return isSpecialChar;
    }
    
    private void appendChar_(char c) {
        if(c == '\\' && !isSpecialChar) {
            isSpecialChar = true;
        } else if(isSpecialChar) {
            doubtMode_ = DoubtMode.String;
            if(unicodeChar) {
                readUnicode(c);
                return;
            }
            isSpecialChar = false;
            switch (c) {
                case 'b':
                    characterBuffer.append('\b');
                    break;
                case 't':
                    characterBuffer.append('\t');
                    break;
                case 'n':
                    characterBuffer.append('\n');
                    break;
                case 'f':
                    characterBuffer.append('\f');
                    break;
                case 'v':
                    characterBuffer.append('\u000B');
                    break;
                case 'r':
                    characterBuffer.append('\r');
                    break;
                case 'u':
                    isSpecialChar = true;
                    unicodeChar = true;
                    unicodeCharCount = 0;
                    break;
                default:
                    characterBuffer.append(c);
                    isSpecialChar = false;
                    break;
            }
        } else if(!allowControlChar && Character.isISOControl(c)) {
            throw new CSONException(ExceptionMessages.getCtrlCharNotAllowed(c));
        }
        else {
            characterBuffer.append(c);
        }
    }

    void prev() {
        int len = characterBuffer.length();
        if(len > 0) {
            characterBuffer.setLength(len - 1);
            doubtMode = lastDoubtMode;
        }

    }

    boolean isNull() {
        return doubtMode == DoubtMode.Null;
    }

    boolean isNumber() {
        return doubtMode != DoubtMode.String && doubtMode != DoubtMode.True && doubtMode != DoubtMode.False;
    }


    Boolean getBoolean() {
        if(doubtMode == DoubtMode.True) {
            return Boolean.TRUE;
        } else if(doubtMode == DoubtMode.False) {
            return Boolean.FALSE;
        }
        return null;
    }


    Number getNumber() {
        if(doubtMode == DoubtMode.Null) {
            return null;
        }
        if(isoCtrlInNumber) {
            String value = characterBuffer.toString();
            // ctrl 문자 제거
            value = value.replaceAll("\\p{C}", "");
            characterBuffer.setLength(0);
            characterBuffer.append(value);
            isoCtrlInNumber = false;
        }


        if(noneZeroStart) {
            characterBuffer.insert(0, "0.");
        }

        if(doubtMode == DoubtMode.NaN) {
            return Double.NaN;
        } else if(doubtMode == DoubtMode.Infinity) {
            char sign = characterBuffer.charAt(0);
            if(sign == '-') {
                return Double.NEGATIVE_INFINITY;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        } else if(doubtMode == DoubtMode.HexadecimalValue) {
            boolean isNegative = characterBuffer.charAt(0) == '-';
            int len = characterBuffer.length();
            if(isAppearSign) {
                char sign = characterBuffer.charAt(0);
                if(sign == '-') {
                    isNegative = true;
                }
                //String val = characterBuffer.subSequence(3, characterBuffer.length()).toString();

                //MockBigInteger bi = new MockBigInteger(val, 16);

                MockBigInteger bi = new MockBigInteger(characterBuffer.getChars(), 3, len - 3, 16);
                if(isNegative) {
                    bi = bi.negate();
                }
                if(bi.bitLength() <= 31){
                    return bi.intValue();
                }
                if(bi.bitLength() <= 63){
                    return bi.longValue();
                }
                return bi;
            } else {
                //String val = characterBuffer.subSequence(2, characterBuffer.length()).toString();
                //MockBigInteger bi = new MockBigInteger(val, 16);
                MockBigInteger bi = new MockBigInteger(characterBuffer.getChars(), 2, len - 2, 16);
                if(bi.bitLength() <= 31){
                    return bi.intValue();
                }
                if(bi.bitLength() <= 63){
                    return bi.longValue();
                }
                return bi;
            }
        }
        else if(isAppearRealNumberSing || doubtMode == DoubtMode.Exponential) {
            return new BigDecimal(characterBuffer.getChars(), 0, characterBuffer.length());
        }
        else {
            BigInteger mbi = new BigInteger(characterBuffer.toString());

            if(mbi.bitLength() <= 31){
                return mbi.intValue();
            }
            if(mbi.bitLength() <= 63){
                return mbi.longValue();
            }
            return mbi;
        }
    }


    private static String hexToUnicode(String hexString ) {
        int unicode = Integer.parseInt(hexString, 16);
        return new String(Character.toChars(unicode));
    }

    private void readUnicode(char c) {
        if(c == '{') {
            unicodeExtend = true;
            markStartUnicodeIndex = characterBuffer.length();
            characterBuffer.append(c);
            return;
        } else if(markStartUnicodeIndex == -1) {
            markStartUnicodeIndex = characterBuffer.length();
        }
        ++unicodeCharCount;
        characterBuffer.append(c);
        if((unicodeCharCount == 4 && !unicodeExtend) || (unicodeExtend && c == '}')) {

            unicodeCharCount = 0;
            int end = characterBuffer.length();
            if(unicodeExtend) {
                end -= 1;
                ++markStartUnicodeIndex;
            }
            String unicode = characterBuffer.subSequence(markStartUnicodeIndex, end).toString();
            if(unicodeExtend) {
                characterBuffer.setLength(markStartUnicodeIndex - 1);
            } else {
                characterBuffer.setLength(markStartUnicodeIndex);
            }

            characterBuffer.append(hexToUnicode(unicode));
            markStartUnicodeIndex = -1;
            isSpecialChar = false;
            unicodeChar = false;
            unicodeExtend = false;
        }
    }


    @Override
    public String toString() {
        if(doubtMode == DoubtMode.Null) {
            return null;
        }
        if(trimResult) {
            return characterBuffer.toTrimString();
        }
        return characterBuffer.toString();
    }

    public String toTrimString() {
        if(doubtMode == DoubtMode.Null) {
            return null;
        }
        return characterBuffer.toTrimString();
    }



    public String toString(boolean quote) {
        if(quote) {
            characterBuffer.decreaseLength(1);
        }
        return characterBuffer.toString();

    }

    private static boolean isHexadecimalChar(char c) {
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    boolean isEmpty() {
        return characterBuffer.isEmpty();
    }



    void release() {
        characterBuffer.reset();
        //characterBufferPool.add(characterBuffer);
    }




    private static boolean isSpaceChar(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000B';
    }

}
