package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;

final class JSON5ParserX {



    private JSON5ParserX() {
    }



    enum ParsingState {
        /**
         * 시작 상태: 아직 { 또는 [ 가 나오지 않은 상태
         */
        Open,
        /**
         * JSON 이 닫힌 상태. 이 상태에서는 주석만 파싱할 수 있다.
         */
        Close,
        /**
         * 문자열을 읽는 상태. WaitValue 이후로 " 또는 ' 가 나온 상태이다.
         */
        String,
        /**
         * 값을 파싱하는 상태. WaitKey 이후로 숫자 혹은 문자가 나온 상태이다.
         * '\n', '}', ']', ',' 이 나오기 전까지 값이 어떤 타입인지 알 수 없다.
         */
        Value,
        /**
         * 값을 기다리는 상태. Object 에서  ':' 이 나오거나 Array 에서 ',' 이후에 이 상태로 전환된다.
         */
        WaitValue,
        /**
         * 키 이후에 ':' 이 나온 상태. 이 상태에서는 값이 나와야 한다.
         * ' 또는 " 로 시작하고 끝나는 문자열에만 해당한다.
         * 키 값에 " 또는 ' 가 없다면, 또 이경우 키 마지막에 \n 이 없다면 이 상태 모드는 만날 수 없다.
         */
        WaitValueSeparator,
        /**
         * 다음 키 또는 값을 기다리는 상태. ',' 이 나오면 이 상태로 전환된다.
         * 또는 root element 가 아닌 상황에서 또는 '}' 또는 ']' 이 나오면 이 상태로 전환된다.
         */
        NextStoreSeparator, // , 가 나오기를 기다림
        /**
         * ' 또는 " 가 있는 키를 읽고 있는 상태.
         */
        InKey,
        /**
         * ' 또는 " 가 없는 키를 읽고 있는 상태. \n 또는 ':' 이 나오기 전까지 키를 읽는다.
         */
        InKeyUnquoted,
        /**
         * 키를 기다리는 상태. 이 상태에서 ' 또는 " 이 나오면 InKey 상태로 전환된다.
         * 만약, ' 또는 " 이 나오지 않고, 문자열이 나오면 InKeyUnquoted 상태로 전환된다.
         */
        WaitKey,
        /**
         * 주석을 처리하는 상태. 주석이 시작되면 이 상태로 전환된다.
         */
        InOpenComment,
        /**
         *
         */
        InCloseComment



    }


    /**
     * JSON5 문서를 파싱한다.
     * @param reader JSON5 문서를 읽는 Reader
     * @param rootElement 파싱된 결과를 저장할 CSONElement. CSONObject 또는 CSONArray 이어야 한다.
     * @param jsonOption JSON5 파싱 옵션
     */
    static void parse(Reader reader,CSONElement rootElement, JSONOptions jsonOption) {

        final boolean singleQuote = jsonOption.isAllowSingleQuotes();
        final boolean allowUnquotedKey = jsonOption.isAllowUnquoted();
        final boolean allowConsecutiveCommas = jsonOption.isAllowConsecutiveCommas();
        final boolean trailingComma = jsonOption.isAllowTrailingComma();
        final boolean allowComment = jsonOption.isAllowComments();

        char currentQuoteChar = '\0';
        boolean readyComment = false;

        // 현재 모드.
        ParsingState currentParsingState = ParsingState.Open;
        //
        String key = null;
        //
        String lastKey = null;
        ArrayDeque<String> parentKeyStack = null;

        if(allowComment) {
            parentKeyStack = new ArrayDeque<>();
        }


        CommentObject keyCommentObject = null;
        CommentObject valueCommentObject = null;


        ValueParseState valueParseState;

        CharacterBuffer commentBuffer = null;
        ParsingState commentBeforeParsingState = null;
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

                if(currentParsingState == ParsingState.WaitKey || currentParsingState == ParsingState.WaitValue || currentParsingState == ParsingState.WaitValueSeparator || currentParsingState == ParsingState.NextStoreSeparator) {
                    if(c == '\n' || c == '\r' || c == '\t' || c == ' ') {
                        while((c = reader.read()) != -1) {
                            if(c == '\r' || c == '\n' || c == '\t' || c == ' ') {
                                continue;
                            }
                            break;
                        }
                    }
                }


                /* ************************************************************************************
                 *
                 *  시작: 주석 처리 영역
                 *  만약, 주석를 허용하고 현재 모드가 문자열이 아닌 경우에만 처리한다.
                 *
                 ************************************************************************************/
                // 주석를 사용할 경우 처리
                if(allowComment && currentParsingState != ParsingState.String) {

                    boolean commentComplate = false;
                    if(currentParsingState == ParsingState.InCloseComment) {
                        if(c == '*') {
                            readyComment = true;
                            commentBuffer.append((char) c);
                            continue;
                        } else if(readyComment && c == '/') {
                            readyComment = false;
                            commentComplate = true;
                            commentBuffer.setLength(commentBuffer.length() - 1);
                        } else {
                            readyComment = false;
                            commentBuffer.append((char) c);
                            continue;
                        }

                    } else if(currentParsingState == ParsingState.InOpenComment) {
                        if(c == '\n') {
                            commentComplate = true;
                            readyComment = false;
                        } else {
                            commentBuffer.append((char) c);
                            continue;
                        }
                    }

                    // 주석가 완성되면 각 상태에 따라 처리한다.
                    if(commentComplate) {
                        currentParsingState = commentBeforeParsingState;
                        CommentObject commentObject = null;

                        if (currentParsingState == ParsingState.WaitKey || currentParsingState == ParsingState.WaitValueSeparator || currentParsingState == ParsingState.InKeyUnquoted) {
                            if (keyCommentObject == null) {
                                keyCommentObject = new CommentObject();
                            }
                            commentObject = keyCommentObject;
                            if (currentParsingState == ParsingState.WaitKey) {
                                commentObject.appendLeadingComment(commentBuffer.toTrimString());
                            } else {
                                commentObject.appendTrailingComment(commentBuffer.toTrimString());
                            }
                        } else if (currentParsingState == ParsingState.WaitValue || currentParsingState == ParsingState.Value) {
                            if (valueCommentObject == null) {
                                valueCommentObject = new CommentObject();
                            }
                            commentObject = valueCommentObject;
                            if (currentParsingState == ParsingState.WaitValue) {
                                commentObject.appendLeadingComment(commentBuffer.toTrimString());
                            } else {
                                commentObject.appendTrailingComment(commentBuffer.toTrimString());
                            }
                        } else if (currentParsingState == ParsingState.Open) {
                            String value = commentBuffer.toTrimString();
                            String alreadyComment = rootElement.getHeadComment();
                            if (alreadyComment != null) {
                                value = alreadyComment + "\n" + value;
                            }
                            rootElement.setHeadComment(value);
                        } else if (currentParsingState == ParsingState.Close) {
                            String alreadyComment = rootElement.getTailComment();
                            if (alreadyComment != null) {
                                rootElement.setTailComment(alreadyComment + "\n" + commentBuffer.toTrimString());
                            } else {
                                rootElement.setTailComment(commentBuffer.toTrimString());
                            }
                        } else if (currentParsingState == ParsingState.NextStoreSeparator) {
                            if (currentElement instanceof CSONObject && (lastKey != null || key != null)) {
                                if (lastKey == null) {
                                    lastKey = key;
                                }
                                ((CSONObject) currentElement).getOrCreateCommentObjectOfValue(lastKey).appendTrailingComment(commentBuffer.toTrimString());
                            } else if (currentElement instanceof CSONArray) {
                                CSONArray array = (CSONArray) currentElement;
                                if (!array.isEmpty()) {
                                    array.getOrCreateCommentObject(array.size() - 1).appendTrailingComment(commentBuffer.toTrimString());
                                }
                            }
                        }
                        commentBuffer.reset();
                        continue;
                    }  else if(readyComment && c == '/') {
                        valueParseState.prev();
                        currentParsingState = ParsingState.InOpenComment;
                        commentBuffer.reset();
                        continue;
                    } else if(readyComment && c == '*') {
                        valueParseState.prev();
                        currentParsingState = ParsingState.InCloseComment;
                        commentBuffer.reset();
                        continue;
                    }
                    else if(c == '/') {
                        commentBeforeParsingState = currentParsingState;
                        readyComment = true;
                    } else {
                        readyComment = false;
                    }
                    if(currentParsingState == ParsingState.Close || commentBeforeParsingState == ParsingState.Close) {
                        continue;
                    }
                }
                /* ************************************************************************************
                 *  끝: 주석 처리 영역
                 ************************************************************************************/


                if(currentParsingState == ParsingState.InKeyUnquoted && (c == ':')) {
                    String keyString = valueParseState.toTrimString();
                    if(keyString.isEmpty()) {
                        throw new CSONParseException(ExceptionMessages.getKeyNotFound(line, index));
                    }
                    key = keyString;
                    currentParsingState = ParsingState.WaitValueSeparator;
                    valueParseState.reset();
                }

                ++index;

                if((c != currentQuoteChar) && (currentParsingState == ParsingState.String || currentParsingState == ParsingState.InKey || currentParsingState == ParsingState.InKeyUnquoted)) {
                    valueParseState.append((char) c);
                } else if((currentParsingState == ParsingState.Value) &&  (c != ',' && c != '}' && c != ']')) {
                    if(c == '\n' || c == '\r') {
                        lastKey = putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);

                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
                        currentParsingState = ParsingState.NextStoreSeparator;
                    }
                    else valueParseState.append((char)c);
                }
                else if(c == '{') {
                    if(currentParsingState != ParsingState.WaitValue && currentParsingState != ParsingState.Open) {
                        throw new CSONParseException("Unexpected character '{' at " + index);
                    }
                    currentParsingState = ParsingState.WaitKey;
                    CSONElement oldElement = currentElement;
                    if(oldElement == null) {
                        if(rootElement == null) {
                            rootElement = new CSONObject(rootElement.getStringFormatOption());
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
                    if(currentParsingState !=
                            ParsingState.Open && currentParsingState != ParsingState.WaitValue) {
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

                    if(currentParsingState == ParsingState.WaitValue || currentParsingState == ParsingState.WaitKey) {
                        if(!trailingComma) {
                            throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                        }
                    }
                    else if(currentParsingState == ParsingState.Value) {
                        putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
                    } else if(currentParsingState != ParsingState.NextStoreSeparator) {
                        throw new CSONParseException("Unexpected character '" + (char)c + "' at " + index);
                    }
                    if(allowComment) {
                        valueCommentObject = null;
                        keyCommentObject = null;
                    }


                    currentParsingState = ParsingState.NextStoreSeparator;
                    csonElements.removeLast();
                    if(csonElements.isEmpty()) {
                        if(allowComment) {
                            currentParsingState = ParsingState.Close;
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

                else if(currentParsingState == ParsingState.Value && (c == '\n' || c == '\r')) {
                    putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                    key = null;
                    keyCommentObject = null;
                    valueCommentObject = null;
                    currentParsingState = ParsingState.NextStoreSeparator;
                }

                else if(c == ',') {
                    if(currentParsingState == ParsingState.WaitKey && !allowConsecutiveCommas) {
                        throw new CSONParseException("Unexpected character ',' at " + index + " ,line: " + line);
                    }

                    if(currentParsingState != ParsingState.NextStoreSeparator && currentParsingState != ParsingState.Value) {
                        if(allowConsecutiveCommas) {
                            // csonOBjcet 는 키가 있을때만 null 넣는다.
                            if((key != null && currentElement instanceof CSONObject) || currentElement instanceof CSONArray) {
                                lastKey = putData(null, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                            }
                            key = null;
                            keyCommentObject = null;
                            valueCommentObject = null;
                            currentParsingState = ParsingState.NextStoreSeparator;
                        } else {
                            throw new CSONParseException("Unexpected character ',' at " + index + " ,line: " + line);
                        }
                    }
                    if(currentParsingState == ParsingState.Value) {
                        lastKey = putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
                    }

                    currentParsingState = afterComma(currentElement);

                }

                else if(c == currentQuoteChar && !valueParseState.isSpecialChar()) {

                    if(currentParsingState == ParsingState.String) {
                        lastKey = putData(valueParseState, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                        key = null;
                        keyCommentObject = null;
                        valueCommentObject = null;
                        currentParsingState = ParsingState.NextStoreSeparator;
                    }
                    else if(currentParsingState == ParsingState.InKey) {
                        key = valueParseState.toString();
                        currentParsingState = ParsingState.WaitValueSeparator;
                    }
                    currentQuoteChar = '\0';
                }

                else if(isQuotedString((char)c, singleQuote)) {

                    if(currentParsingState != ParsingState.String && currentParsingState != ParsingState.Value && currentParsingState != ParsingState.WaitKey && currentParsingState != ParsingState.WaitValue && currentParsingState != ParsingState.InKey) {
                        throw new CSONParseException("Unexpected character '\"' at " + index);
                    }
                    else if(currentParsingState == ParsingState.WaitValue) {
                        currentParsingState = ParsingState.String;
                        currentQuoteChar = (char) c;
                        valueParseState.reset().setOnlyString(true);
                    }
                    else if(currentParsingState == ParsingState.WaitKey) {
                        currentParsingState = ParsingState.InKey;
                        currentQuoteChar = (char) c;
                        valueParseState.reset().setOnlyString(true);
                    } else {
                        valueParseState.append((char)c);
                    }
                }

                else if(c == ':') {
                    if(currentParsingState != ParsingState.WaitValueSeparator) {
                        throw new CSONParseException("Unexpected character ':' at " + index);
                    } else {
                        currentParsingState = ParsingState.WaitValue;
                    }
                }

                else if(currentParsingState == ParsingState.WaitValue && !isQuotedString((char)c, singleQuote)  && !Character.isSpaceChar(c)  && c != '\n' && c != '\r' && c != '\t' && c != '\b' && c != '\f' && c != '\0' && c != 0xFEFF) {
                    valueParseState.reset();
                    valueParseState.setOnlyNumber(!allowUnquotedKey);
                    valueParseState.setAllowControlChar(false);
                    valueParseState.append((char)c);
                    valueParseState.setTrimResult(true);
                    currentParsingState = ParsingState.Value;
                    currentQuoteChar = '\0';
                }

                else if(currentParsingState == ParsingState.WaitKey && allowUnquotedKey && !Character.isWhitespace(c)) {
                    valueParseState.reset();
                    valueParseState.setAllowControlChar(false);
                    valueParseState.append((char)c);
                    currentParsingState = ParsingState.InKeyUnquoted;
                }





            }

            /*
             * json5 문서의 마지막에 주석이 있는 경우 처리
             */
            if(allowComment && currentParsingState == ParsingState.InOpenComment) {
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
        if(currentParsingState == ParsingState.Close) {
            return;
        }


        throw new CSONParseException("Unexpected end of stream");
    }



    private static ParsingState afterComma(CSONElement element) {
        if(element instanceof CSONArray) {
            return ParsingState.WaitValue;
        } else {
            return ParsingState.WaitKey;
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
