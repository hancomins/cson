package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.NumberConversionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

class ValueParseState {


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


    //boolean isAppearSign = false;

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

    /**
     * 제어문자를 허용할 것인지 여부를 설정한다.
     */
    private boolean allowControlChar = false;



    private DoubtMode doubtMode_ = DoubtMode.None;




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
        this.doubtMode_ = DoubtMode.String;
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
        doubtMode_ = DoubtMode.None;
        noneZeroStart = false;

        isSpecialChar = false;
        unicodeChar = false;
        unicodeExtend = false;
        unicodeCharCount = 0;
        markStartUnicodeIndex = -1;

        resultString = null;
        resultNumber = null;
        resultBoolean = null;

        onlyString = false;
        sign = Sign.None;
        return this;
    }

    enum Sign {
            None,
        Positive,
        Negative
    }
    private Sign sign = Sign.None;


    void append(char c) {
        appendChar_(c);
    }


    private void readType() {
        if(onlyString) {
            resultString = characterBuffer.toString();
            return;
        }

        boolean isAppearRealNumberSing = false;
        boolean isAppearExponentialSign = false;
        boolean noneZeroStart = false;
        char[] chars = characterBuffer.getChars();
        int end = characterBuffer.length();

        int start =0;

        // 문자열의 시작에 공백이 있으면 제거한다.
        for(int i = 0; i < end; ++i) {
            if(!isSpaceChar(chars[i])) {
                start = i;
                break;
            }
        }

        boolean originEnd = true;

        // 문자열의 끝에 공백이 있으면 제거한다.
        if(isSpaceChar(chars[end - 1])) {
            for (; start < end; --end) {
                if (!isSpaceChar(chars[end - 1])) {
                    break;
                }
            }
        }


        int len = end - start;
        int startDigitOffset = 0;


        DoubtMode doubtMode = this.doubtMode_;

        boolean isStart = true;
        for(int i = start; i < end; ++i) {
            char c = chars[i];
            int staticSignIndex = i - startDigitOffset;
            if(isStart) {
                isStart = false;
                if(doubtMode == DoubtMode.String) {
                    resultString = new String(chars, start, len);
                    return;
                }
                char lowChar = Character.toLowerCase(c);
                if (allowNaN && NaNSign[staticSignIndex] == lowChar && len == NaNSign.length) {
                    doubtMode = DoubtMode.NaN;
                } else  if(Null[staticSignIndex] == lowChar && len == Null.length) {
                    doubtMode = DoubtMode.Null;
                }
                else if (allowInfinity && InfinitySign[staticSignIndex] == lowChar && len == InfinitySign.length) {
                    doubtMode = DoubtMode.Infinity;
                } else if (allowHexadecimal && HexadecimalSign[staticSignIndex] == lowChar && len > 2) {
                    ++i;
                    char next = Character.toLowerCase(chars[i]);
                    if(next == '.') {
                        doubtMode = DoubtMode.Number;
                        isAppearRealNumberSing = true;
                        continue;
                    } else if(Character.isDigit(next)) {
                        doubtMode_ = DoubtMode.Number;
                        continue;
                    }
                    if(Character.toLowerCase(HexadecimalSign[++staticSignIndex]) != next) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                        return;
                    }
                    doubtMode = DoubtMode.Hexadecimal;
                } else if (TrueSign[staticSignIndex] == lowChar && len == TrueSign.length && sign == Sign.None) {
                    doubtMode = DoubtMode.True;
                } else if (FalseSign[staticSignIndex] == lowChar  && len == FalseSign.length && sign == Sign.None) {
                    doubtMode = DoubtMode.False;
                }
                /**
                 * + 부호가 나오면 숫자로 의심한다.
                 * 단, allowPositiveSign 이 true 일 경우에만 진행한다.
                 */
                else if (allowPositiveSign && c == '+' && len > 1) {
                    doubtMode = DoubtMode.Number;
                    //++i;
                    sign = Sign.Positive;
                    ++start;
                    --len;
                    startDigitOffset = 1;
                    isStart = true;

                    /*startDigitOffset = 1;
                    lowChar = Character.toLowerCase(chars[i]);
                    // infinity 의 앞 글자 i 가 나오면 일단 infinity 의심 모드로 전환한다.
                    if (allowInfinity && InfinitySign[i - startDigitOffset] == lowChar && len - i == InfinitySign.length) {

                        doubtMode = DoubtMode.Infinity;
                    } else if(lowChar == '.') {
                        isAppearRealNumberSing = true;
                    } else if(!Character.isDigit(lowChar)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                        return;
                    }*/
                }
                /**
                 * - 부호가 나오면 숫자로 의심한다.
                 */
                else if(c == '-'  && len > 1) {
                    doubtMode = DoubtMode.Number;
                    sign = Sign.Negative;
                    ++start;
                    --len;
                    startDigitOffset = 1;
                    isStart = true;
                    /*++i;
                    lowChar = Character.toLowerCase(chars[i]);
                    startDigitOffset = 1;
                    // infinity 의 앞 글자 i 가 나오면 일단 infinity 의심 모드로 전환한다.
                    if (allowInfinity && InfinitySign[i - startDigitOffset] == lowChar && len - i == InfinitySign.length) {
                        doubtMode = DoubtMode.Infinity;
                    } else if(lowChar == '.') {
                        isAppearRealNumberSing = true;
                    } else if(!Character.isDigit(lowChar)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                        return;
                    }*/
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
                    char lowChar = Character.toLowerCase(c);
                    if (i + 1 == NaNSign.length) {
                        resultNumber = Double.NaN;
                        doubtMode_ = DoubtMode.NaN;
                        return;
                    } else if(NaNSign[staticSignIndex] != lowChar) {
                        if(Null[staticSignIndex] == lowChar) {
                            doubtMode = DoubtMode.Null;
                            continue;
                        }
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    }
                }
                 else if(doubtMode == DoubtMode.Null) {
                    if(Null[staticSignIndex] != Character.toLowerCase(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    } else if(i + 1 ==  Null.length) {
                        resultNumber = null;
                        resultString = null;
                        doubtMode_ = doubtMode;
                        return;
                    }
                } else if(doubtMode == DoubtMode.True) {
                    if(TrueSign[staticSignIndex] != Character.toLowerCase(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                    } else if(i + 1 == TrueSign.length) {
                        resultBoolean = Boolean.TRUE;
                        doubtMode_ = doubtMode;
                        return;
                    }
                } else if(doubtMode == DoubtMode.False) {
                    if(FalseSign[staticSignIndex] != Character.toLowerCase(c)) {
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
                        resultNumber = sign == Sign.Negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        doubtMode_ = doubtMode;
                        return;
                    }
                } else if(doubtMode == DoubtMode.Hexadecimal) {
                    if(!isHexadecimalChar(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                        return;
                    }
                }
                else if(doubtMode == DoubtMode.Number) {
                    if(c == '.') {
                        // 소수점이 두 번 나오면 문자열로 처리한다.
                        if(isAppearRealNumberSing) {
                            doubtMode_ = DoubtMode.String;
                            resultString = new String(chars, start, len);
                            return;
                        }
                        isAppearRealNumberSing = true;
                    } else if(c == 'e' || c == 'E') {
                        doubtMode = DoubtMode.Exponential;
                    }
                    else if(!Character.isDigit(c)) {
                        doubtMode_ = DoubtMode.String;
                        resultString = new String(chars, start, len);
                        return;
                    }
                } else if(doubtMode == DoubtMode.Exponential) {
                    if(c == '+' || c == '-') {
                        if(isAppearExponentialSign) {
                            doubtMode_ = DoubtMode.String;
                            resultString = new String(chars, start, len);

                        }
                        isAppearExponentialSign = true;
                    } else if(!Character.isDigit(c)) {
                        doubtMode = DoubtMode.String;
                    }
                }
            }
        }
        this.doubtMode_ = doubtMode;

        if(doubtMode == DoubtMode.Exponential) {
            resultNumber = new BigDecimal(chars, start, len);
        } else if(doubtMode == DoubtMode.Null) {

        }

        else if(doubtMode == DoubtMode.Number) {
            if(sign == Sign.Negative) {
                --start;
                ++len;
            }
            if(isAppearRealNumberSing || isAppearExponentialSign) {
                resultNumber = new BigDecimal(chars, start, len);
            } else {
                resultString = new String(chars, start, len);


                BigInteger bigInteger = new BigInteger(resultString);
                if(bigInteger.bitLength() <= 31){
                    resultNumber = bigInteger.intValue();
                } else if(bigInteger.bitLength() <= 63){
                    resultNumber = bigInteger.longValue();
                } else {
                    resultNumber = bigInteger;
                }
            }
        } else if(doubtMode == DoubtMode.String) {
            resultString = new String(chars, start, len);
        } else if(doubtMode == DoubtMode.Hexadecimal) {

            if(sign == Sign.Negative) {
                ++start;
                --len;
                chars[start] = '-';
            } else if(sign == Sign.Positive) {
                start += 2;
                len -= 2;
            } else {
                start += 2;
                len -= 2;
            }


            resultNumber = new BigInteger(new String(chars, start, len), 16);
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
                    //if(c != '\n' && c != '\r') {
                        characterBuffer.append(c);
                    //}
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

    boolean isNull() {
        if(doubtMode_ == DoubtMode.None) {
            readType();
        }
        return doubtMode_ == DoubtMode.Null;
    }

    boolean isNumber() {
        if(doubtMode_ == DoubtMode.None) {
            readType();
        }
        return doubtMode_ != DoubtMode.String && doubtMode_ != DoubtMode.True && doubtMode_ != DoubtMode.False;
    }

    Boolean getBoolean() {
        if(doubtMode_ == DoubtMode.None) {
            readType();
        }
        if(doubtMode_ == DoubtMode.True) {
            return Boolean.TRUE;
        } else if(doubtMode_ == DoubtMode.False) {
            return Boolean.FALSE;
        }
        return null;
    }


    Number getNumber() {
        if(resultNumber == null) {
            readType();
        }
        return resultNumber;
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
        if(doubtMode_ == DoubtMode.Null) {
            return null;
        }
        if(trimResult) {
            return characterBuffer.toTrimString();
        }
        return characterBuffer.toString();
    }

    public String toTrimString() {
        if(doubtMode_ == DoubtMode.Null) {
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







    private static boolean isSpaceChar(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000B' || c == '\0';
    }

}
