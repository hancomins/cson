package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.MockBigInteger;
import com.hancomins.cson.util.NullValue;
import com.hancomins.cson.util.NumberConversionUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

class ValueBuffer {


    private CharacterBuffer numberBuffer;
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

        True,
        False,
        String,

        ZeroStartNumber,
        Number,
        RealNumber,
        SignNumber,
        // 지수
        ExponentialNumberStart,
        ExponentialNegativeNumber,

        ExponentialNumber,








    }


    //boolean isAppearSign = false;


    boolean isSpecialChar = false;
    boolean unicodeChar = false;
    boolean unicodeExtend = false;
    int unicodeCharCount = 0;

    private boolean endString = false;


    private boolean allowNaN = false;
    private boolean allowInfinity = false;
    private boolean allowHexadecimal = false;
    private boolean leadingZeroOmission = false;
    private boolean allowPositiveSign = false;
    private boolean onlyPrimitiveValue = false;

    /**
     * 제어문자를 허용할 것인지 여부를 설정한다.
     */
    private boolean allowControlChar = false;



    private DoubtMode doubtMode_ = DoubtMode.None;
    private char quoteChar = '\0';


    ValueBuffer(NumberConversionUtil.NumberConversionOption numberConversionOption) {
        this(new CharacterBuffer(), numberConversionOption);
    }

    ValueBuffer(CharacterBuffer characterBuffer, NumberConversionUtil.NumberConversionOption numberConversionOption) {
        this.characterBuffer = characterBuffer;
        numberBuffer = new CharacterBuffer();
        allowNaN = numberConversionOption.isAllowNaN();
        allowInfinity = numberConversionOption.isAllowInfinity();
        allowHexadecimal = numberConversionOption.isAllowHexadecimal();
        leadingZeroOmission = numberConversionOption.isLeadingZeroOmission();
        allowPositiveSign = numberConversionOption.isAllowPositiveSing();
        onlyPrimitiveValue = !numberConversionOption.isIgnoreNonNumeric();

    }

    public ValueBuffer setAllowControlChar(boolean allowControlChar) {
        this.allowControlChar = allowControlChar;
        return this;
    }



    public ValueBuffer setOnlyString(char quote) {
        this.doubtMode_ = DoubtMode.String;
        quoteChar = quote;
        return this;
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

    ValueBuffer reset() {

        characterBuffer.reset();
        doubtMode_ = DoubtMode.None;


        isSpecialChar = false;
        unicodeChar = false;
        unicodeExtend = false;
        unicodeCharCount = 0;
        markStartUnicodeIndex = -1;
        endString = false;


        numberBuffer.reset();
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
        switch (doubtMode_) {
            case None:
                switch (c) {
                    case  '+':
                        if(!allowPositiveSign) {
                            doubtMode_ = DoubtMode.String;
                            appendChar_(c);
                            break;
                        }
                        doubtMode_ = DoubtMode.SignNumber;
                        characterBuffer.append(c);
                        break;
                    case '-':
                        doubtMode_ = DoubtMode.SignNumber;
                        characterBuffer.append(c);
                        numberBuffer.append(c);
                        break;
                    case '.':
                        if(!leadingZeroOmission) {
                            doubtMode_ = DoubtMode.String;
                            appendChar_(c);
                            break;
                        }
                        doubtMode_ = DoubtMode.RealNumber;
                        characterBuffer.append(c);
                        numberBuffer.append('0');
                        numberBuffer.append('.');
                        break;
                    case '0':
                        doubtMode_ = DoubtMode.ZeroStartNumber;
                        characterBuffer.append(c);
                        break;
                    default:
                        if (c >= '1' && c <= '9') {
                            doubtMode_ = DoubtMode.Number;
                            characterBuffer.append(c);
                            numberBuffer.append(c);
                        }
                        else {
                            doubtMode_ = DoubtMode.String;
                            appendChar_(c);
                        }
                }
                break;
            case ZeroStartNumber:
                if(c == '.') {
                    characterBuffer.append(c);
                    numberBuffer.append('0').append('.');
                    doubtMode_ = DoubtMode.RealNumber;
                } else if(c == '0') {
                    if(!leadingZeroOmission) {
                        doubtMode_ = DoubtMode.String;
                        appendChar_(c);
                        break;
                    }
                    characterBuffer.append(c);
                }
                else if(c == 'e' || c == 'E') {
                    characterBuffer.append(c);
                    numberBuffer.append('0').append(c);
                    doubtMode_ = DoubtMode.ExponentialNumberStart;
                }
                else if(c == 'x' || c == 'X') {
                    if(!allowHexadecimal) {
                        doubtMode_ = DoubtMode.String;
                        appendChar_(c);
                        break;
                    }
                    doubtMode_ = DoubtMode.Hexadecimal;
                    characterBuffer.append(c);
                }
                else if(c >= '1' && c <= '9') {
                    doubtMode_ = DoubtMode.Number;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case Hexadecimal:
                if(isHexadecimalChar(c)) {
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case SignNumber:
                if(c == '.' && leadingZeroOmission) {
                    doubtMode_ = DoubtMode.RealNumber;
                    characterBuffer.append(c);
                    numberBuffer.append('0').append('.');
                } else if(c == '0') {
                    doubtMode_ = DoubtMode.ZeroStartNumber;
                    characterBuffer.append(c);
                }
                else if(c >= '1' && c <= '9') {
                    doubtMode_ = DoubtMode.Number;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case RealNumber:
                if(c == 'e' || c == 'E') {
                    doubtMode_ = DoubtMode.ExponentialNumberStart;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else if(c >= '0' && c <= '9') {
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case Number:
                if(c == '.') {
                    doubtMode_ = DoubtMode.RealNumber;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else if(c == 'e' || c == 'E') {
                    doubtMode_ = DoubtMode.ExponentialNumberStart;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else if(c >= '0' && c <= '9') {
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case ExponentialNumberStart:
                if(c == '+') {
                    doubtMode_ = DoubtMode.ExponentialNumber;
                    characterBuffer.append(c);

                } else if(c == '-') {
                    numberBuffer.append(c);
                    characterBuffer.append(c);
                    doubtMode_ = DoubtMode.ExponentialNegativeNumber;
                }
                else if(c >= '0' && c <= '9') {
                    doubtMode_ = DoubtMode.ExponentialNumber;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case ExponentialNegativeNumber:
                if(c >= '0' && c <= '9') {
                    doubtMode_ = DoubtMode.ExponentialNumber;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            case ExponentialNumber:
                if(c >= '0' && c <= '9') {
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                } else {
                    doubtMode_ = DoubtMode.String;
                    appendChar_(c);
                }
                break;
            default:
                appendChar_(c);
        }
    }

    public Object parseValue() {
        try {
            switch (doubtMode_) {
                case Number:
                    return parseInteger(numberBuffer);
                case RealNumber:
                case ExponentialNumber:
                    return parseDecimal(numberBuffer);
                case Hexadecimal:
                    return parseHexadecimal(numberBuffer);
                case ZeroStartNumber:
                    return 0;
            }
            int len =  characterBuffer.length();
            String value = characterBuffer.toString();
            if(len < 10 ) {
                String lowerCaseValue = value.toLowerCase();
                if (lowerCaseValue.equals("true")) {
                    return Boolean.TRUE;
                } else if (lowerCaseValue.equals("false")) {
                    return Boolean.FALSE;
                } else if (lowerCaseValue.equals("null")) {
                    return NullValue.Instance;
                } else if (allowNaN && lowerCaseValue.equals("nan")) {
                    return Double.NaN;
                } else if (allowInfinity) {
                    if (lowerCaseValue.equals("infinity")) {
                        return Double.POSITIVE_INFINITY;
                    } else if (lowerCaseValue.equals("-infinity")) {
                        return Double.NEGATIVE_INFINITY;
                    } else if (allowPositiveSign && lowerCaseValue.equals("+infinity")) {
                        return Double.POSITIVE_INFINITY;
                    }
                }
            }
            if(onlyPrimitiveValue) {
                throw new NumberFormatException("Invalid number format VALUE: " + value);
            }




            return value;
        } finally {
            reset();
        }
    }

    public static Object parseDecimal(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        return new BigDecimal(chars, 0, length);
    }


    private static Object parseHexadecimal(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        if(length < 3) {
            throw new NumberFormatException("Invalid hexadecimal number format");
        }
        String resultString = new String(chars, 0, length);
        BigInteger bigInteger = new BigInteger(resultString, 16);
        if(bigInteger.bitLength() <= 31){
            return bigInteger.intValue();
        } else if(bigInteger.bitLength() <= 63) {
            return bigInteger.longValue();
        }
        return bigInteger;
    }

    private static Object parseInteger(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();

        if(length < 18) {
            MockBigInteger mockBigInteger = new MockBigInteger(chars, 0, length);
            if(mockBigInteger.bitLength() <= 31){
                return mockBigInteger.intValue();
            } else  {
                return mockBigInteger.longValue();
            }
        }

        String resultString = new String(chars,0, length);
        BigInteger bigInteger = new BigInteger(resultString);
        if(bigInteger.bitLength() <= 63){
            return bigInteger.longValue();
        }
        return bigInteger;
    }

    boolean isEndQuote() {
        return endString;
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
            if(c == quoteChar) {
                endString = true;
                return;
            }

            characterBuffer.append(c);
        }
    }

    void prev() {
        int len = characterBuffer.length();
        if(len > 0) {
            characterBuffer.setLength(len - 1);
        }

    }




    Number parseNumber() {
        Object value = parseValue();
        if(value instanceof Number) {
            return (Number)value;
        }
        return null;
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

    public String getStringAndReset() {
        String result = characterBuffer.toString();
        reset();
        return result;
    }


    @Override
    public String toString() {
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
        return (c >= '1' && c <= '9') || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    boolean isEmpty() {
        return characterBuffer.isEmpty();
    }



    private static boolean isNumberOrSign(char c) {
        return (c >= '0' && c <= '9');
    }





}