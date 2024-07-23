package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;

class JSON5ParserX {


    private static final String NULL = "null";





    enum Mode {
        Open,
        String,
        Value,
        WaitValue,
        //OpenArray,
        //CloseObject,
        CloseArray,
        WaitValueSeparator,
        NextStoreSeparator, // , 가 나오기를 기다림
        InKey,
        InKeyUnquoted,
        WaitKey,
        //WaitNextCommentChar,
        InOpenComment,
        InCloseComment



    }



    void appendSpecialChar(Reader reader, CharacterBuffer dataStringBuilder, int c) throws IOException {
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

    static CSONElement parse(Reader reader, CSONElement rootElement, JSONOptions jsonOption) {

        boolean singleQuote;
        boolean allowUnquotedKey;
        boolean allowConsecutiveCommas;
        boolean allowComment;
        boolean trailingComma;
        boolean readyComment = false;


        singleQuote = jsonOption.isAllowSingleQuotes();
        allowUnquotedKey = jsonOption.isAllowUnquoted();
        allowConsecutiveCommas = jsonOption.isAllowConsecutiveCommas();
        trailingComma = jsonOption.isAllowTrailingComma();
        allowComment = jsonOption.isAllowComments();
        readyComment = false;

        char currentQuoteChar = '\0';



        Mode commentBeforeMode = null;
        Mode currentMode = Mode.Open;
        String key = null;
        String lastKey = null;

        CommentObject keyCommentObject = null;
        CommentObject valueCommentObject = null;
        ValueParseState valueParseState;
        CSONElement currentElement = null;



        valueParseState = new ValueParseState(jsonOption);
        ArrayDeque<CSONElement> csonElements = new ArrayDeque<>();


        int line = 1;
        int index = 0;



        try {
            int c;

            while((c = reader.read()) != -1) {
                /*if(c == '\n') {
                    ++line;
                }*/
                if(currentMode == Mode.WaitKey || currentMode == Mode.WaitValue || currentMode == Mode.WaitValueSeparator || currentMode == Mode.NextStoreSeparator) {
                    if(c == '\n' || c == '\r' || c == '\t' || c == ' ') {
                        while((c = reader.read()) != -1) {
                            if(c == '\n') {
                                ++line;
                            }
                            if(c == '\r' || c == '\n' || c == '\t' || c == ' ') {
                                continue;
                            }
                            break;
                        }
                    }
                }

                if(allowComment) {
                    boolean commentWrite = false;
                    if(currentMode == Mode.InCloseComment) {
                        if(c == '*') {
                            readyComment = true;
                            valueParseState.append((char) c);
                            continue;
                        } else if(readyComment && c == '/') {
                            readyComment = false;
                            commentWrite = true;
                        }

                    } else if(currentMode == Mode.InOpenComment) {
                        if(c == '\n') {
                            commentWrite = true;
                        } else {
                            valueParseState.append((char) c);
                            continue;
                        }
                    }

                    if(commentWrite) {
                        currentMode = commentBeforeMode;
                        if(currentMode == Mode.WaitKey) {
                            keyCommentObject = new CommentObject();
                            keyCommentObject.setBeforeComment(valueParseState.toTrimString());
                        }

                        else if(currentMode == Mode.WaitValueSeparator) {
                            if(keyCommentObject == null) {
                                keyCommentObject = new CommentObject();
                            }
                            keyCommentObject.setAfterComment(valueParseState.toTrimString());
                        } else if(currentMode == Mode.WaitValue) {
                            if(valueCommentObject == null) {
                                valueCommentObject = new CommentObject();
                            }
                            valueCommentObject.setBeforeComment(valueParseState.toTrimString());
                        }
                        else if(currentMode == Mode.NextStoreSeparator) {
                            if(currentElement instanceof CSONObject && lastKey != null) {
                                ((CSONObject)currentElement).setCommentAfterValue(lastKey, valueParseState.toTrimString());
                            } else if(currentElement instanceof CSONArray) {
                                 CSONArray array = (CSONArray)currentElement;
                                 if(!array.isEmpty()) {
                                     array.setCommentAfterValue(array.size() - 1, valueParseState.toTrimString());
                                 }
                            }
                        }
                        valueParseState.reset();
                        continue;
                    }
                    else if(readyComment && c == '/') {
                        valueParseState.reset();
                        valueParseState.setOnlyString(true);
                        currentMode = Mode.InOpenComment;
                    } else if(readyComment && c == '*') {
                        valueParseState.reset();
                        valueParseState.setOnlyString(false);
                        currentMode = Mode.InCloseComment;
                    }
                    else if(c == '/' && currentMode != Mode.InKeyUnquoted && currentMode != Mode.Value && currentMode != Mode.String) {
                        commentBeforeMode = currentMode;
                        readyComment = true;
                    } else {
                        readyComment = false;
                    }
                }

                if(currentMode == Mode.InKeyUnquoted && (c == ':')) {
                    String keyString = valueParseState.toTrimString();
                    if(keyString.isEmpty()) {
                        throw new CSONParseException(ExceptionMessages.getKeyNotFound(line, index));
                    }
                    key = keyString;
                    currentMode = Mode.WaitValueSeparator;
                    valueParseState.reset();
                }

                ++index;

                if((c != currentQuoteChar) && (currentMode == Mode.String || currentMode == Mode.InKey || currentMode == Mode.InKeyUnquoted)) {
                    valueParseState.append((char) c);
                } else if((currentMode == Mode.Value) &&  (c != ',' && c != '}' && c != ']')) {
                    if(c == '\n' || c == '\r') {
                        lastKey = putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;


                        currentMode = Mode.NextStoreSeparator;
                    }
                    else valueParseState.append((char)c);
                }
                else if(c == '{') {
                    if(currentMode != Mode.WaitValue && currentMode != Mode.Open) {
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
                        lastKey = putElementData(oldElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;

                    }
                    csonElements.offerLast(currentElement);
                } else if(c == '[') {
                    if(currentMode != Mode.Open && currentMode != Mode.WaitValue) {
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
                        lastKey = putElementData(oldElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
                    }
                    csonElements.offerLast(currentElement);
                } else if(c == ']'  || c == '}') {

                    if(currentMode == Mode.WaitValue || currentMode == Mode.WaitKey) {
                        if(!trailingComma) {
                            throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                        }
                    }
                    else if(currentMode == Mode.Value) {
                        putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
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

                else if(currentMode == Mode.Value && (c == '\n' || c == '\r')) {
                    putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                    key = null;
                    keyCommentObject = null;
                    valueCommentObject = null;
                    currentMode = Mode.NextStoreSeparator;
                }

                else if(c == ',') {

                    if(currentMode != Mode.NextStoreSeparator && currentMode != Mode.Value) {
                        if(allowConsecutiveCommas) {
                            // csonOBjcet 는 키가 있을때만 null 넣는다.
                            if((key != null && currentElement instanceof CSONObject) || currentElement instanceof CSONArray) {
                                lastKey = putData(null, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                            }
                            key = null;
                            keyCommentObject = null;
                            valueCommentObject = null;
                            currentMode = Mode.NextStoreSeparator;
                        } else {
                            throw new CSONParseException("Unexpected character ',' at " + index);
                        }
                    }
                    if(currentMode == Mode.Value) {
                        lastKey = putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
                    }

                    currentMode = afterComma(currentElement);

                }

                else if(c == currentQuoteChar && !valueParseState.isSpecialChar()) {

                    if(currentMode == Mode.String) {
                        lastKey = putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;

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
                    if(currentMode != Mode.String && currentMode != Mode.Value && currentMode != Mode.WaitKey && currentMode != Mode.WaitValue && currentMode != Mode.InKey) {
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

                /*else if(currentMode == Mode.NextStoreSeparator && (c == '\n' || c == '\r') ) {
                    throw new CSONParseException("Unexpected character '" + (char) c + "' at " + index);
                }*/




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


    private static String putData(ValueParseState valueParseState, CSONElement currentElement, String key, boolean allowComment, CommentObject keyCommentObject, CommentObject valueCommentObject) {

        if(valueParseState == null) {
            putNullData(currentElement, key);
        }
        else if(valueParseState.isNumber()) {
            putNumberData(currentElement, valueParseState.getNumber(), key);
        } else {
            Boolean booleanValue = valueParseState.getBoolean();
            if (booleanValue != null) {
                putBooleanData(currentElement, booleanValue, key);
            } else {
                putStringData(currentElement, valueParseState.toString(), key);
            }
        }
        if(allowComment) {
            putComment(currentElement, key, keyCommentObject, valueCommentObject);
        }
        String lastKey = key;
        valueParseState.reset();
        return lastKey;
    }

    private static void putComment(CSONElement currentElement, String key, CommentObject keyCommentObject, CommentObject valueCommentObject) {
        if(currentElement instanceof CSONObject) {
            ((CSONObject)currentElement).setCommentObjects(key, keyCommentObject, valueCommentObject);
        }


    }

    private static void putNumberData(CSONElement currentElement, Number value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }

    private static void putStringData(CSONElement currentElement, String value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            try {
                ((CSONArray) currentElement).add(value);
            } catch (Exception e) {
                throw e;
            }
        }
    }


    private static void putNullData(CSONElement currentElement,String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, null);
        } else {
            try {
                ((CSONArray) currentElement).add(null);
            } catch (Exception e) {
                throw e;
            }
        }
    }


    private static boolean isQuotedString(char c, boolean singleQuote) {
        return c == '"' || (singleQuote && c == '\'');
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


    private static String putElementData(CSONElement currentElement, CSONElement value, String key, boolean allowComment, CommentObject keyCommentObject, CommentObject valueCommentObject) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
        if(allowComment) {
            putComment(currentElement, key, keyCommentObject, valueCommentObject);
        }
        String lastKey = key;
        return lastKey;
    }



}
