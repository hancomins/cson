package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.NumberConversionUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;

class PureJSONParser {



    private PureJSONParser() {
    }


    @SuppressWarnings("SameParameterValue")
    static CSONElement parsePureJSON(Reader reader, NumberConversionUtil.NumberConversionOption numberConversionOption) {
        return parsePureJSON(reader, null, numberConversionOption);
    }

    static void appendSpecialChar(Reader reader, CharacterBuffer dataStringBuilder, int c) throws IOException {
        switch (c) {
            case 'b':
                dataStringBuilder.append('\b');
                break;
            case 'f':
                dataStringBuilder.append('\f');
                break;
            case 'n':
                dataStringBuilder.append('\n');
                break;
            case 'r':
                dataStringBuilder.append('\r');
                break;
            case 't':
                dataStringBuilder.append('\t');
                break;
            case '"':
                dataStringBuilder.append('"');
                break;
            case 'u':
                char[] hexChars = new char[4];
                //noinspection ResultOfMethodCallIgnored
                reader.read(hexChars);
                String hexString = new String(hexChars);
                int hexValue = Integer.parseInt(hexString, 16);
                dataStringBuilder.append((char)hexValue);
                break;
            case '/':
            case '\\':
            default:
                dataStringBuilder.append((char)c);
                break;
        }
    }

    static CSONElement parsePureJSON(Reader reader, CSONElement rootElement,NumberConversionUtil.NumberConversionOption numberConversionOption) {
        //ArrayDeque<ParsingState> modeStack = new ArrayDeque<>();
        ArrayDeque<CSONElement> csonElements = new ArrayDeque<>();
        CSONElement currentElement = null;

        ParsingState currentParsingState = null;
        CharacterBuffer dataStringBuilder = new CharacterBuffer();
        String key = null;

        int index = 0;
        try {
            int c;
            int lastC = -1;
            boolean isSpecialChar = false;

            while((c = reader.read()) != -1) {

                ++index;
                //ParsingState currentParsingState = modeStack.peekLast();
                if((c != '"' || isSpecialChar) && (currentParsingState == ParsingState.String || currentParsingState == ParsingState.InKey)) {
                    if(isSpecialChar) {
                        isSpecialChar = false;
                        appendSpecialChar(reader, dataStringBuilder, c);
                    } else if(c == '\\') {
                        isSpecialChar = true;
                    }
                    else dataStringBuilder.append((char)c);
                } else if(currentParsingState == ParsingState.Number &&  (isSpecialChar ||  (c != ',' && c != '}' && c != ']'))) {
                    if(isSpecialChar) {
                        isSpecialChar = false;
                        appendSpecialChar(reader, dataStringBuilder, c);
                    }
                    else if(c == '\\') {
                        isSpecialChar = true;
                    }
                    else dataStringBuilder.append((char)c);
                }
                else if(c == '{') {
                    if(currentParsingState != ParsingState.WaitValue && currentParsingState != null) {
                        throw new CSONParseException("Unexpected character '{' at " + index);
                    }
                    currentParsingState = ParsingState.WaitKey;
                    CSONElement oldElement = currentElement;
                    if(oldElement == null) {
                        if(rootElement == null) {
                            rootElement = new CSONObject(StringFormatOption.json());
                        }
                        currentElement = rootElement;
                        if(!(currentElement instanceof CSONObject)) {
                            throw new CSONParseException("Unexpected character '{' at " + index);
                        }
                    }
                    else {
                        currentElement = new CSONObject();
                        putElementData(oldElement, currentElement, key);
                        key = null;
                    }
                    csonElements.offerLast(currentElement);
                } else if(c == '[') {
                    if(currentParsingState != null && currentParsingState != ParsingState.WaitValue) {
                        throw new CSONParseException("Unexpected character '[' at " + index);
                    }
                    currentParsingState = ParsingState.WaitValue;
                    CSONElement oldElement = currentElement;
                    if(oldElement == null) {
                        if(rootElement == null) {
                            rootElement = new CSONArray();
                        }
                        currentElement = rootElement;
                        if(!(currentElement instanceof CSONArray)) {
                            throw new CSONParseException("Unexpected character '{' at " + index);
                        }
                    }
                    else {
                        currentElement = new CSONArray();
                        putElementData(oldElement, currentElement, key);
                        key = null;
                    }
                    csonElements.offerLast(currentElement);
                } else if(c == ']'  || c == '}') {


                    if(/*currentParsingState == ParsingState.WaitValue || currentParsingState == ParsingState.WaitKey*/ lastC == ',') {


                        throw new CSONParseException("Unexpected character ',' at " + (index  - 1));

                    }
                    else if(currentParsingState == ParsingState.Number) {
                        char[] numberString = dataStringBuilder.getChars();
                        int len = dataStringBuilder.length();
                        processNumber(currentElement, numberString, len, key, index, numberConversionOption);
                        key = null;
                    } else if(currentParsingState != ParsingState.WaitNextStoreSeparator &&
                            (currentParsingState == ParsingState.WaitValue && currentElement instanceof CSONArray && !((CSONArray) currentElement).isEmpty()))  {

                        throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                    }

                    currentParsingState = ParsingState.WaitNextStoreSeparator;
                    csonElements.removeLast();
                    if(csonElements.isEmpty()) {
                        return currentElement;
                    }
                    currentElement = csonElements.getLast();
                } else if(c == ',') {
                    if(currentParsingState != ParsingState.WaitNextStoreSeparator && currentParsingState != ParsingState.Number) {
                        throw new CSONParseException("Unexpected character ',' at " + index);
                    }
                    if(currentParsingState == ParsingState.Number) {
                        char[] numberString = dataStringBuilder.getChars();
                        int len = dataStringBuilder.length();
                        processNumber(currentElement, numberString, len, key, index, numberConversionOption);
                        key = null;
                    }
                    if(currentElement instanceof CSONArray) {
                        currentParsingState = ParsingState.WaitValue;
                    } else {
                        currentParsingState = ParsingState.WaitKey;
                    }
                }
                else if(c == '"') {
                    if(currentParsingState != ParsingState.String && currentParsingState != ParsingState.WaitKey && currentParsingState != ParsingState.WaitValue && currentParsingState != ParsingState.InKey) {
                        throw new CSONParseException("Unexpected character '\"' at " + index);
                    }
                    else if(currentParsingState == ParsingState.InKey) {
                        key = dataStringBuilder.toString();
                        currentParsingState = ParsingState.WaitKeyEndSeparator;
                    }
                    else if(currentParsingState == ParsingState.String) {
                        String value = dataStringBuilder.toString();
                        putStringData(currentElement, value, key);
                        key = null;

                        currentParsingState = ParsingState.WaitNextStoreSeparator;
                    }
                    else if(currentParsingState == ParsingState.WaitValue) {

                        dataStringBuilder.reset();
                        currentParsingState = ParsingState.String;
                    }
                    else if(currentParsingState == ParsingState.WaitKey) {
                        dataStringBuilder.reset();
                        currentParsingState = ParsingState.InKey;
                    }
                } else if(c == ':') {
                    if(currentParsingState != ParsingState.WaitKeyEndSeparator) {
                        throw new CSONParseException("Unexpected character ':' at " + index);
                    } else {
                        
                        currentParsingState = ParsingState.WaitValue;
                    }
                } else if(currentParsingState == ParsingState.WaitValue && !Character.isSpaceChar(c)  && c != '\n' && c != '\r' && c != '\t' && c != '\b' && c != '\f' && c != '\0' && c != 0xFEFF) {
                    dataStringBuilder.reset();
                    dataStringBuilder.append((char)c);
                    currentParsingState = ParsingState.Number;
                }
                lastC = c;
            }
        } catch (CSONParseException e) {
            throw e;
        } catch (IOException e) {
            throw new CSONParseException(e.getMessage());
        }
        throw new CSONParseException("Unexpected end of stream");
    }

    private static void putStringData(CSONElement currentElement, String value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }

    private static int trimLast(char[] numberString, int len) {
        int lastIndex = len - 1;
        while(lastIndex >= 0) {
            char c = numberString[lastIndex];
            if(Character.isWhitespace(c) || c == '\b' || c == '\f' || c == '\0' || c == 0xFEFF) {
                --lastIndex;
            } else {
                break;
            }
        }
        return lastIndex + 1;
    }

    private static void processNumber(CSONElement currentElement, char[] numberString, int len, String key, int index, NumberConversionUtil.NumberConversionOption numberConversionOption) {
        if(len == 0) {
            throw new CSONParseException("Unexpected character ',' at " + index);
        }

        len = trimLast(numberString, len);


        if((numberString[0] == 'n' || numberString[0] == 'N') && (numberString[1] == 'u' || numberString[1] == 'U') && (numberString[2] == 'l' || numberString[2] == 'L')
                && (numberString[3] == 'l' || numberString[3] == 'L') )//NULL.equalsIgnoreCase(numberString)) {
        {
            putStringData(currentElement, null, key);
        } else {
            Number numberValue = null;
            Boolean booleanValue = null;
            char firstChar = numberString[0];

            try {
                if (firstChar == 't' || firstChar == 'T' || firstChar == 'F' ||   firstChar == 'f' ) {
                    String booleanString = new String(numberString, 0, len);
                    booleanValue = Boolean.parseBoolean(booleanString);
                } else {
                    numberValue = NumberConversionUtil.stringToNumber(numberString, 0, len, numberConversionOption);
                }
            } catch (NumberFormatException e) {
                    //throw new CSONParseException("Number format error value '" + numberString + "' at " + index, e);
                putStringData(currentElement, new String(numberString, 0, len), key);
                return;
            }
            if(booleanValue != null) {
                putBooleanData(currentElement, booleanValue, key);
            } else {
                putNumberData(currentElement, numberValue, key);
            }
        }
    }


    private static void putBooleanData(CSONElement currentElement, boolean value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }
    private static void putNumberData(CSONElement currentElement, Number value, String key) {
        if(key != null) {
           ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }

    private static void putElementData(CSONElement currentElement, CSONElement value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }

}
