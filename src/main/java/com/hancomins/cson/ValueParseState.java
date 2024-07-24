package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.MockBigInteger;
import com.hancomins.cson.util.NumberConversionUtil;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentLinkedDeque;

class ValueParseState {

    private static ConcurrentLinkedDeque<CharacterBuffer> characterBufferPool = new ConcurrentLinkedDeque<>();

    private CharacterBuffer characterBuffer;

    private static final char[] NaNSign = new char[]{'n','a','n'};
    private static final char[] Null = new char[]{'n','u','l','l'};
    private static final char[] InfinitySign = new char[]{'i','n','f','i','n','i','t','y'};
    private static final char[] HexadecimalSign = new char[]{'0','x'};
    private static final char[] TrueSign = new char[]{'t','r','u','e'};
    private static final char[] FalseSign = new char[]{'f','a','l','s','e'};


    private int specialSymbolIndex = 0;



    enum DoubtMode {
        None,
        NaN,
        Null,
        Infinity,
        NegativeInfinity,
        HexadecimalStart,
        HexadecimalValue,
        Exponential,
        Number,
        True,
        False,
        String
    }

    boolean isAppearRealNumberSing = false;
    boolean isAppearSign = false;
    boolean isAppearExponentialSign = false;
    boolean noneZeroStart = false;
    boolean isSpecialChar = false;
    boolean unicodeChar = false;
    boolean unicodeExtend = false;
    int unicodeCharCount = 0;

    private int index = 0;

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



    private DoubtMode doubtMode = DoubtMode.Number;


    private boolean onlyString = false;



    ValueParseState(NumberConversionUtil.NumberConversionOption numberConversionOption) {
        characterBuffer = new CharacterBuffer();
        allowNaN = numberConversionOption.isAllowNaN();
        allowInfinity = numberConversionOption.isAllowInfinity();
        allowHexadecimal = numberConversionOption.isAllowHexadecimal();
        leadingZeroOmission = numberConversionOption.isLeadingZeroOmission();
        allowPositiveSign = numberConversionOption.isAllowPositiveSing();
        ignoreNonNumeric = numberConversionOption.isIgnoreNonNumeric();
    }

    public void setAllowControlChar(boolean allowControlChar) {
        this.allowControlChar = allowControlChar;
    }

    /**
     * 숫자만을 허용할 것인지 여부를 설정한다.
     * @param onlyNumber
     */
    public void setOnlyNumber(boolean onlyNumber) {
        //this.ignoreNonNumeric = onlyNumber;
    }

    public void setOnlyString(boolean onlyString) {
        this.doubtMode = DoubtMode.String;
        this.onlyString = onlyString;
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

    void reset() {
        characterBuffer.reset();
        index = 0;
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
    }


    void append(char c) {

        if(index == 0) {
            if(this.doubtMode == DoubtMode.String) {
                appendChar(c);
                return;
            }
            char lowChar = Character.toLowerCase(c);
            if (NaNSign[specialSymbolIndex] == lowChar) {
                if(allowNaN) {
                    doubtMode = DoubtMode.NaN;
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if (InfinitySign[specialSymbolIndex] == lowChar) {
                if(allowInfinity) {
                    doubtMode = DoubtMode.Infinity;
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if (HexadecimalSign[specialSymbolIndex] == lowChar) {
                if (allowHexadecimal) {
                    doubtMode = DoubtMode.HexadecimalStart;
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if (TrueSign[specialSymbolIndex] == lowChar) {
                doubtMode = DoubtMode.True;
                ++specialSymbolIndex;
                appendChar(c);
            } else if (FalseSign[specialSymbolIndex] == lowChar) {
                doubtMode = DoubtMode.False;
                ++specialSymbolIndex;
                appendChar(c);
            }
            else if (c == '+') {
                if(allowPositiveSign) {
                    doubtMode = DoubtMode.Number;
                    isAppearSign = true;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if(c == '-') {
                doubtMode = DoubtMode.Number;
                appendChar(c);
                isAppearSign = true;
            } else if(c == '.'){
                if(leadingZeroOmission) {
                    isAppearRealNumberSing = true;
                    noneZeroStart = true;
                } else {
                    doubtMode = DoubtMode.String;
                    appendChar(c);
                }
            } else if(!Character.isDigit(c)) {
                doubtMode = DoubtMode.String;
                appendChar(c);
            } else {
                doubtMode = DoubtMode.Number;
                appendChar(c);
            }
        } else {

            if(doubtMode == DoubtMode.NaN) {
                if(specialSymbolIndex == NaNSign.length) {
                    doubtMode = DoubtMode.String;
                }
                else if(NaNSign[specialSymbolIndex] == Character.toLowerCase(c)) {
                    ++specialSymbolIndex;
                } else {
                    if(Null[specialSymbolIndex] == Character.toLowerCase(c)) {
                        doubtMode = DoubtMode.Null;
                        ++specialSymbolIndex;
                    } else {
                        doubtMode = DoubtMode.String;
                    }
                }
                appendChar(c);
            } else if(doubtMode == DoubtMode.Null) {
                if(specialSymbolIndex == Null.length) {
                    doubtMode = DoubtMode.String;
                }
                else if(Null[specialSymbolIndex] == Character.toLowerCase(c)) {
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if(doubtMode == DoubtMode.True) {
                if(specialSymbolIndex == TrueSign.length) {
                    doubtMode = DoubtMode.String;
                }
                else if(TrueSign[specialSymbolIndex] == Character.toLowerCase(c)) {
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if(doubtMode == DoubtMode.False) {
                if(specialSymbolIndex == FalseSign.length) {
                    doubtMode = DoubtMode.String;
                }
                else if(FalseSign[specialSymbolIndex] == Character.toLowerCase(c)) {
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            }
            else if(doubtMode == DoubtMode.Infinity) {
                if(specialSymbolIndex == InfinitySign.length) {
                    doubtMode = DoubtMode.String;
                }
                else if(InfinitySign[specialSymbolIndex] == Character.toLowerCase(c)) {
                    ++specialSymbolIndex;
                } else {
                    doubtMode = DoubtMode.String;
                }
                appendChar(c);
            } else if(doubtMode == DoubtMode.HexadecimalStart) {

                if(specialSymbolIndex == HexadecimalSign.length) {
                    doubtMode = DoubtMode.HexadecimalValue;
                    if(!isHexadecimalChar(c)) {
                        doubtMode = DoubtMode.String;
                    }
                }
                else if(HexadecimalSign[specialSymbolIndex] == Character.toLowerCase(c)) {
                    ++specialSymbolIndex;
                } else {
                    if(c == '.' || c == 'e' || c == 'E' || Character.isDigit(c)) {
                        isAppearRealNumberSing = true;
                        doubtMode = DoubtMode.Number;
                    }
                    else {
                        doubtMode = DoubtMode.String;
                    }
                }
                appendChar(c);
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
                appendChar(c);
            }
        }
        ++index;
    }

    int markStartUnicodeIndex = -1;

    boolean isSpecialChar() {
        return isSpecialChar;
    }
    
    private void appendChar(char c) {
        if(doubtMode == DoubtMode.String && c == '\\' && !isSpecialChar) {
            isSpecialChar = true;
        } else if(isSpecialChar) {
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
        }
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
            MockBigInteger bi = new MockBigInteger(characterBuffer.getChars(), 0, characterBuffer.length());
            if(bi.bitLength() <= 31){
                return bi.intValue();
            }
            if(bi.bitLength() <= 63){
                return bi.longValue();
            }
            return bi;
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




}
