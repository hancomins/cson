package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;

class JSON5ParserX {






    enum Mode {
        Open,
        Close,
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



    static void parse(Reader reader, CSONElement rootElement, JSONOptions jsonOption) {

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

        char currentQuoteChar = '\0';



        Mode currentMode = Mode.Open;
        String key = null;
        String lastKey = null;
        ArrayDeque<String> parentKeyStack = null;
        if(allowComment) {
            parentKeyStack = new ArrayDeque<>();
        }


        CommentObject keyCommentObject = null;
        CommentObject valueCommentObject = null;


        ValueParseState valueParseState;

        CharacterBuffer commentBuffer = null;
        Mode commentBeforeMode = null;
        if(allowComment) {
            commentBuffer = new CharacterBuffer(128);
        }



        CSONElement currentElement = null;



        valueParseState = new ValueParseState(jsonOption);
        valueParseState.setAllowControlChar(jsonOption.isAllowControlChar());
        ArrayDeque<CSONElement> csonElements = new ArrayDeque<>();


        int line = 1;
        int index = 0;


        try {
            int c;

            while((c = reader.read()) != -1) {

                if(c == '\n') {
                    ++line;
                }

                if(currentMode == Mode.WaitKey || currentMode == Mode.WaitValue || currentMode == Mode.WaitValueSeparator || currentMode == Mode.NextStoreSeparator) {
                    if(c == '\n' || c == '\r' || c == '\t' || c == ' ') {
                        while((c = reader.read()) != -1) {
                            if(c == '\r' || c == '\n' || c == '\t' || c == ' ') {
                                continue;
                            }
                            break;
                        }
                    }
                }


                // 코멘트를 사용할 경우 처리
                if(allowComment && currentMode != Mode.String) {
                    boolean commentWrite = false;
                    if(currentMode == Mode.InCloseComment) {
                        if(c == '*') {
                            readyComment = true;
                            commentBuffer.append((char) c);
                            continue;
                        } else if(readyComment && c == '/') {
                            readyComment = false;
                            commentWrite = true;
                            commentBuffer.setLength(commentBuffer.length() - 1);
                        } else {
                            readyComment = false;
                            commentBuffer.append((char) c);
                            continue;
                        }

                    } else if(currentMode == Mode.InOpenComment) {
                        if(c == '\n') {
                            commentWrite = true;
                            readyComment = false;
                        } else {
                            commentBuffer.append((char) c);
                            continue;
                        }
                    }

                    if(commentWrite) {
                        currentMode = commentBeforeMode;
                        if(currentMode == Mode.WaitKey) {
                            if(keyCommentObject == null) {
                                keyCommentObject = new CommentObject();
                            }
                            keyCommentObject.appendLeadingComment(commentBuffer.toTrimString());
                        }
                        else if(currentMode == Mode.WaitValueSeparator || currentMode == Mode.InKeyUnquoted) {
                            if(keyCommentObject == null) {
                                keyCommentObject = new CommentObject();
                            }
                            keyCommentObject.appendTrailingComment(commentBuffer.toTrimString());
                        } else if(currentMode == Mode.WaitValue) {
                            if(valueCommentObject == null) {
                                valueCommentObject = new CommentObject();
                            }
                            valueCommentObject.appendLeadingComment((commentBuffer.toTrimString()));
                        } else if(currentMode == Mode.Open) {
                            String value = commentBuffer.toTrimString();
                            String alreadyComment = rootElement.getHeadComment();
                            if(alreadyComment != null) {
                                value = alreadyComment + "\n" + value;
                            }
                            rootElement.setHeadComment(value);
                        }
                        else if(currentMode == Mode.Value) {
                            if(valueCommentObject == null) {
                                valueCommentObject = new CommentObject();
                            }
                            valueCommentObject.appendTrailingComment(commentBuffer.toTrimString());
                        }
                        else if(currentMode == Mode.Close) {
                            String alreadyComment = rootElement.getTailComment();
                            if(alreadyComment != null) {
                                rootElement.setTailComment(alreadyComment + "\n" + commentBuffer.toTrimString());
                            } else {
                                rootElement.setTailComment(commentBuffer.toTrimString());
                            }
                        }
                        else if(currentMode == Mode.NextStoreSeparator) {
                            if(currentElement instanceof CSONObject && (lastKey != null || key != null)) {
                                if(lastKey == null) {
                                    lastKey = key;
                                }
                                ((CSONObject)currentElement).getOrCreateCommentObjectOfValue(lastKey).appendTrailingComment(commentBuffer.toTrimString());
                            } else if(currentElement instanceof CSONArray) {
                                 CSONArray array = (CSONArray)currentElement;
                                 if(!array.isEmpty()) {
                                     array.getOrCreateCommentObject(array.size() - 1).appendTrailingComment(commentBuffer.toTrimString());
                                 }
                            }

                        }
                        commentBuffer.reset();
                        continue;
                    }  else if(readyComment && c == '/') {
                        valueParseState.prev();
                        currentMode = Mode.InOpenComment;
                        commentBuffer.reset();
                        continue;
                    } else if(readyComment && c == '*') {
                        valueParseState.prev();
                        currentMode = Mode.InCloseComment;

                        /*if(commentBeforeMode == Mode.InKeyUnquoted) {
                            if(valueParseState.isEmpty()) {
                                commentBeforeMode = Mode.WaitKey;
                            } else {
                                key = valueParseState.toTrimString();
                            }
                        } else if(commentBeforeMode == Mode.Value) {
                            if(valueParseState.isEmpty()) {
                                commentBeforeMode = Mode.WaitValue;
                            } else {
                                // 코멘트 전용 string buffer
                            }

                        }*/


                        commentBuffer.reset();
                        continue;
                    }
                    else if(c == '/') {
                        commentBeforeMode = currentMode;
                        readyComment = true;
                    } else {
                        readyComment = false;
                    }

                    if(currentMode == Mode.Close || commentBeforeMode == Mode.Close) {
                        continue;
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
                        if(allowComment && oldElement instanceof CSONObject) {
                            parentKeyStack.addLast(key);
                        }
                        lastKey = putElementData(oldElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);

                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;

                    }
                    csonElements.offerLast(currentElement);
                } else if(c == '[') {
                    if(currentMode !=
                            Mode.Open && currentMode != Mode.WaitValue) {
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
                        if(allowComment && oldElement instanceof CSONObject) {
                            parentKeyStack.addLast(key);
                        }
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
                    } else if(currentMode != Mode.NextStoreSeparator) {
                        throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                    }
                    if(allowComment) {
                        valueCommentObject = null;
                        keyCommentObject = null;
                    }


                    currentMode = Mode.NextStoreSeparator;
                    csonElements.removeLast();
                    if(csonElements.isEmpty()) {
                        if(allowComment) {
                            currentMode = Mode.Close;
                            continue;
                        } else {
                            return;
                        }
                    }
                    currentElement = csonElements.getLast();



                    if(allowComment && !(currentElement instanceof CSONArray)) {
                        lastKey = parentKeyStack.pollLast();
                    }
                }

                else if(currentMode == Mode.Value && (c == '\n' || c == '\r')) {
                    putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                    key = null;
                    keyCommentObject = null;
                    valueCommentObject = null;
                    currentMode = Mode.NextStoreSeparator;
                }

                else if(c == ',') {
                    if(currentMode == Mode.WaitKey && !allowConsecutiveCommas) {
                        throw new CSONParseException("Unexpected character ',' at " + index + " ,line: " + line);
                    }

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
                            throw new CSONParseException("Unexpected character ',' at " + index + " ,line: " + line);
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

                    if(currentMode != Mode.String && currentMode != Mode.Value && currentMode != Mode.WaitKey && currentMode != Mode.WaitValue && currentMode != Mode.InKey) {
                        throw new CSONParseException("Unexpected character '\"' at " + index);
                    }
                    else if(currentMode == Mode.WaitValue) {
                        currentMode = Mode.String;
                        currentQuoteChar = (char) c;
                        valueParseState.reset().setOnlyString(true);
                    }
                    else if(currentMode == Mode.WaitKey) {
                        currentMode  = Mode.InKey;
                        currentQuoteChar = (char) c;
                        valueParseState.reset().setOnlyString(true);
                    } else {
                        valueParseState.append((char)c);
                    }
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
                    valueParseState.setTrimResult(true);
                    currentMode  = Mode.Value;
                    currentQuoteChar = '\0';
                }

                else if(currentMode == Mode.WaitKey && allowUnquotedKey && !Character.isWhitespace(c)) {
                    valueParseState.reset();
                    valueParseState.setAllowControlChar(false);
                    valueParseState.append((char)c);
                    currentMode  = Mode.InKeyUnquoted;
                }





            }

            if(allowComment && currentMode == Mode.InOpenComment) {
                String alreadyComment = rootElement.getTailComment();
                if(alreadyComment != null) {
                    rootElement.setTailComment(alreadyComment + "\n" + commentBuffer.toTrimString());
                } else {
                    rootElement.setTailComment(commentBuffer.toTrimString());
                }
                return;
            }


        } catch (IOException e) {
            throw new CSONParseException(e.getMessage());
        }
        if(currentMode == Mode.Close) {
            return;
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



        if(valueParseState == null || valueParseState.isNull()) {
            putNullData(currentElement, key);
        }
        else if(valueParseState.isNumber()) {

            putNumberData(currentElement, valueParseState.getNumber(), key);
            valueParseState.reset();
        } else {
            Boolean booleanValue = valueParseState.getBoolean();
            if (booleanValue != null) {
                putBooleanData(currentElement, booleanValue, key);
            } else {
                String value = valueParseState.toString();
                putStringData(currentElement, value, key);
            }
            valueParseState.reset();
        }
        if(allowComment) {
            putComment(currentElement, key, keyCommentObject, valueCommentObject);
        }
        return key;
    }

    private static void putComment(CSONElement currentElement, String key, CommentObject keyCommentObject, CommentObject valueCommentObject) {
        if(currentElement instanceof CSONObject) {
            ((CSONObject)currentElement).setCommentObjects(key, keyCommentObject, valueCommentObject);

        } else {
            int index = ((CSONArray)currentElement).size() - 1;
            index = Math.max(index, 0);
            ((CSONArray)currentElement).setCommentObject(index, valueCommentObject);
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
            ((CSONArray) currentElement).add(value);
        }
    }


    private static void putNullData(CSONElement currentElement,String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, null);
        } else {
            ((CSONArray) currentElement).add(null);
        }
    }




    private static boolean isQuotedString(char c, boolean singleQuote) {
        return c == '"' || (singleQuote && c == '\'');
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
        return key;
    }



}
