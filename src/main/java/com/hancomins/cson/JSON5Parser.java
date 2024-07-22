package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;

class JSON5Parser {


    private static final String NULL = "null";


    private JSON5Parser() {
    }
    enum Mode {
        String,
        Value,
        WaitValue,
        OpenArray,
        CloseObject,
        CloseArray,
        WaitValueSeparator,
        NextStoreSeparator, // , 가 나오기를 기다림
        InKey,
        InKeyUnquoted,
        WaitKey,
        WaitNextCommentChar,
        InOpenComment,
        InCloseComment



    }


    static CSONElement parsePureJSON(Reader reader, JSONOptions jsonOption) {
        return parsePureJSON(reader, null, jsonOption);
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

    static CSONElement parsePureJSON(Reader reader, CSONElement rootElement,JSONOptions jsonOption) {

        boolean singleQuote = jsonOption.isAllowSingleQuotes();
        boolean allowUnquotedKey = jsonOption.isAllowUnquoted();
        boolean allowConsecutiveCommas = jsonOption.isAllowConsecutiveCommas();
        boolean trailingComma = jsonOption.isAllowTrailingComma();
        boolean allowComment = jsonOption.isAllowComments();



        char currentQuoteChar = '\0';

        //ArrayDeque<Mode> modeStack = new ArrayDeque<>();
        ArrayDeque<CSONElement> csonElements = new ArrayDeque<>();
        CSONElement currentElement = null;

        Mode commentBeforeMode = null;
        Mode currentMode = null;
        //CharacterBuffer dataStringBuilder = new CharacterBuffer();
        String key = null;

        int line = 1;

        int index = 0;
        ValueParseState valueParseState = new ValueParseState(jsonOption);


        try {
            int c;


            while((c = reader.read()) != -1) {
                if(c == '\n') {
                    ++line;
                }

                if(currentMode == Mode.InKeyUnquoted && (c == ':')) {
                    String keyString = valueParseState.toString();
                    if(keyString.isEmpty()) {
                        throw new CSONParseException(ExceptionMessages.getKeyNotFound(line, index));
                    }
                    key = keyString;
                    currentMode = Mode.WaitValueSeparator;
                    valueParseState.reset();
                }

                ++index;

                if((c != currentQuoteChar) && (currentMode == Mode.String || currentMode == Mode.InKey || currentMode == Mode.InKeyUnquoted)) {
                    valueParseState.append((char)c);
                } else if((currentMode == Mode.Value) &&  (c != ',' && c != '}' && c != ']')) {
                    char cs = (char)c;
                    if(Character.isWhitespace((char)c)) {
                        if(valueParseState.isNumber()) {
                            putNumberData(currentElement, valueParseState.getNumber(), key);
                        } else {
                            putStringData(currentElement, valueParseState.toString(), key);
                        }
                        currentMode = Mode.NextStoreSeparator;
                    }
                    else valueParseState.append((char)c);
                }
                else if(c == '{') {
                    if(currentMode != Mode.WaitValue && currentMode != null) {
                        throw new CSONParseException("Unexpected character '{' at " + index);
                    }
                    currentMode = Mode.WaitKey;
                    CSONElement oldElement = currentElement;
                    if(oldElement == null) {
                        if(rootElement == null) {
                            rootElement = new CSONObject(StringFormatOption.jsonPure());
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
                    if(currentMode != null && currentMode != Mode.WaitValue) {
                        throw new CSONParseException("Unexpected character '[' at " + index);
                    }
                    currentMode  = Mode.WaitValue;
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

                    if(currentMode == Mode.WaitValue || currentMode == Mode.WaitKey) {
                        if(!trailingComma) {
                            throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                        }
                    }
                    else if(currentMode == Mode.Value) {

                        if(valueParseState.isNumber()) {
                            putNumberData(currentElement, valueParseState.getNumber(), key);
                        } else {
                            putStringData(currentElement, valueParseState.toString(), key);
                        }

                        key = null;
                    } else if(currentMode != Mode.NextStoreSeparator && currentMode != Mode.Value) {
                        throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                    }

                    currentMode = Mode.NextStoreSeparator;
                    csonElements.removeLast();
                    if(csonElements.isEmpty()) {
                        return currentElement;
                    }
                    currentElement = csonElements.getLast();
                }

                else if(currentMode == Mode.Value && Character.isWhitespace((char)c)) {
                    if(valueParseState.isNumber()) {
                        putNumberData(currentElement, valueParseState.getNumber(), key);
                    } else {
                        putStringData(currentElement, valueParseState.toString(), key);
                    }
                    key = null;
                    currentMode = Mode.NextStoreSeparator;
                }

                else if(c == ',') {

                    if(currentMode != Mode.NextStoreSeparator && currentMode != Mode.Value) {
                        if(allowConsecutiveCommas) {
                            if(currentElement instanceof CSONArray) {
                                ((CSONArray)currentElement).add(null);
                            } else if(currentMode == Mode.WaitValue && currentElement instanceof CSONObject) {
                                ((CSONObject)currentElement).put(key, null);
                                key = null;
                            }
                        } else {
                            throw new CSONParseException("Unexpected character ',' at " + index);
                        }
                    }
                    if(currentMode == Mode.Value) {
                        if(valueParseState.isNumber()) {
                            putNumberData(currentElement, valueParseState.getNumber(), key);
                        } else {
                            putStringData(currentElement, valueParseState.toString(), key);
                        }
                        key = null;
                    }

                    currentMode = afterComma(currentElement);

                }

                else if(c == currentQuoteChar) {

                    if(currentMode == Mode.String) {
                        String value = valueParseState.toString();
                        putStringData(currentElement, value, key);
                        key = null;
                        currentMode  = Mode.NextStoreSeparator;
                    }
                    else if(currentMode == Mode.InKey) {
                        key = valueParseState.toString();
                        currentMode  = Mode.WaitValueSeparator;
                    }

                    currentQuoteChar = '\0';
                }

                else if(isQuotedString((char)c, singleQuote)) {
                    currentQuoteChar = (char) c;
                    if(currentMode != Mode.String && currentMode != Mode.WaitKey && currentMode != Mode.WaitValue && currentMode != Mode.InKey) {
                        throw new CSONParseException("Unexpected character '\"' at " + index);
                    }
                    else if(currentMode == Mode.WaitValue) {
                        currentMode = Mode.String;
                    }
                    else if(currentMode == Mode.WaitKey) {
                        currentMode  = Mode.InKey;
                    }
                    valueParseState.reset();
                    valueParseState.setOnlyString(true);
                    valueParseState.setAllowControlChar(true);
                }

                else if(c == ':') {
                    if(currentMode != Mode.WaitValueSeparator) {
                        throw new CSONParseException("Unexpected character ':' at " + index);
                    } else {
                        currentMode  = Mode.WaitValue;
                    }
                }

                else if(currentMode == Mode.WaitValue && !isQuotedString((char)c, singleQuote)  && !Character.isSpaceChar(c)  && c != '\n' && c != '\r' && c != '\t' && c != '\b' && c != '\f' && c != '\0' && c != 0xFEFF) {
                    valueParseState.reset();
                    valueParseState.setOnlyNumber(!allowUnquotedKey);
                    valueParseState.setAllowControlChar(false);
                    valueParseState.append((char)c);
                    currentMode  = Mode.Value;
                    currentQuoteChar = '\0';
                }


                else if(currentMode == Mode.WaitKey && allowUnquotedKey && !Character.isWhitespace(c)) {
                    valueParseState.reset();
                    valueParseState.setAllowControlChar(false);
                    valueParseState.append((char)c);
                    currentMode  = Mode.InKeyUnquoted;
                }

                else if(currentMode == Mode.WaitValue && !Character.isWhitespace(c)) {
                    valueParseState.reset();
                    valueParseState.setOnlyNumber(!allowUnquotedKey);
                    valueParseState.append((char)c);
                    currentMode  = Mode.Value;
                }

                else if(currentMode == Mode.NextStoreSeparator && (c == '\n' || c == '\r') ) {
                    throw new CSONParseException("Unexpected character '" + (char) c + "' at " + index);
                }

                else if(currentMode == Mode.WaitNextCommentChar && c == '/') {
                    commentBeforeMode = currentMode;
                    currentMode = Mode.InOpenComment;
                }

                else if(currentMode == Mode.InOpenComment && c == '\n')  {

                }



                else if(c == '/') {
                    currentMode = Mode.WaitNextCommentChar;
                }








            }


        } catch (CSONParseException e) {
            throw e;
        } catch (IOException e) {
            throw new CSONParseException(e.getMessage());
        } finally {
            valueParseState.release();
        }
        throw new CSONParseException("Unexpected end of stream");
    }



    private static Mode afterComma(CSONElement element) {
        if(element instanceof CSONArray) {
            return Mode.WaitValue;
        } else {
            return Mode.WaitKey;
        }
    }




    private static boolean isQuotedString(char c, boolean singleQuote) {
        return c == '"' || (singleQuote && c == '\'');
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
