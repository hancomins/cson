package com.hancomins.cson;

import com.hancomins.cson.util.ArrayStack;
import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;

import static com.hancomins.cson.ParsingState.Open;

final class JSON5ParserY {



    private JSON5ParserY() {
    }





    /**
     * JSON5 문서를 파싱한다.
     * @param reader JSON5 문서를 읽는 Reader
     * @param rootElement 파싱된 결과를 저장할 CSONElement. CSONObject 또는 CSONArray 이어야 한다.
     * @param jsonOption JSON5 파싱 옵션
     */
    static void parse(Reader reader,CSONElement rootElement, JSONOptions jsonOption) {

        final boolean singleQuote = jsonOption.isAllowSingleQuotes();
        final boolean allowUnquoted = jsonOption.isAllowUnquoted();
        final boolean allowConsecutiveCommas = jsonOption.isAllowConsecutiveCommas();
        final boolean trailingComma = jsonOption.isAllowTrailingComma();
        final boolean allowComment = jsonOption.isAllowComments();

        char currentQuoteChar = '\0';
        boolean readyComment = false;


        // 현재 모드.
        ParsingState currentParsingState = Open;
        //
        String key = null;
        //
        String lastKey = null;
        ArrayStack<String> parentKeyStack = null;

        if(allowComment) {
            parentKeyStack = new ArrayStack<>();
        }


        CommentObject keyCommentObject = null;
        CommentObject valueCommentObject = null;


        CharacterBuffer commentBuffer = null;
        ParsingState commentBeforeParsingState = null;
        if(allowComment) {
            commentBuffer = new CharacterBuffer(128);
        }



        CSONElement currentElement = null;
        CSONElement parentElement = null;


        CharacterBuffer keyBuffer_ = new CharacterBuffer(128);
        ValueBuffer valueBuffer = new ValueBuffer(keyBuffer_,jsonOption);
        //valueBuffer = new ValueBuffer(jsonOption);
        //valueBuffer.setAllowControlChar(jsonOption.isAllowControlChar());
        ArrayStack<CSONElement> csonElements = new ArrayStack<>();


        int line = 1;
        int index = 0;

        try {
            int v;

            while((v = reader.read()) != -1) {
                char c = (char)v;



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

                        if (currentParsingState == ParsingState.WaitKey || currentParsingState == ParsingState.WaitKeyEndSeparator || currentParsingState == ParsingState.InKeyUnquoted) {
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
                        } else if (currentParsingState == Open) {
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
                        } else if (currentParsingState == ParsingState.WaitNextStoreSeparator) {
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
                        keyBuffer_.prev();
                        currentParsingState = ParsingState.InOpenComment;
                        commentBuffer.reset();
                        continue;
                    } else if(readyComment && c == '*') {
                        keyBuffer_.prev();
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

                ++index;

                ////////////////////



                switch (currentParsingState) {
                    case Open:
                        c = skipSpace(reader, c);
                        switch (c) {
                            case '{':
                                if(rootElement == null) {
                                    rootElement = currentElement = new CSONObject();
                                } else {
                                    currentElement = rootElement;
                                }
                                currentParsingState = ParsingState.WaitKey;
                                csonElements.push(rootElement);
                                break;
                            case '[':
                                if(rootElement == null) {
                                    rootElement = currentElement = new CSONArray();
                                } else {
                                    currentElement = rootElement;
                                }
                                currentParsingState = ParsingState.WaitValue;
                                csonElements.push(rootElement);
                                break;
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;
                    case WaitKey:
                        c = skipSpace(reader, c);
                        switch (c) {
                            case '}':

                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements, rootElement);
                                    currentParsingState = afterValue(currentElement);
                                }
                                break;
                            case ',':
                                // todo 연속된 쉼표 처리
                                if(!allowConsecutiveCommas) {
                                    throw new CSONParseException("Invalid JSON5 document");
                                }
                                break;
                            case '\'':
                            case '"':
                                currentQuoteChar = c;
                                currentParsingState = ParsingState.InKey;
                                valueBuffer.setOnlyString(true);
                                break;
                            default:
                                if(allowUnquoted) {
                                    currentParsingState = ParsingState.InKeyUnquoted;
                                    valueBuffer.setOnlyString(true).append(c);
                                } else {
                                    throw new CSONParseException("Invalid JSON5 document");
                                }
                                break;
                        }
                        break;
                    case InKey:
                        if(c == currentQuoteChar) {
                            currentParsingState = ParsingState.WaitKeyEndSeparator;
                            key = valueBuffer.toString();
                            valueBuffer.reset();
                        } else {
                            valueBuffer.append(c);
                        }
                        break;
                    case InKeyUnquoted:
                        switch (c) {
                            case '\n':
                            case '\r':
                            case '\t':
                            case ' ':
                                currentParsingState = ParsingState.WaitKeyEndSeparator;
                                break;
                            case ':':
                                currentParsingState = ParsingState.WaitValue;
                                key = valueBuffer.toString();
                                valueBuffer.reset();
                                break;
                            default:
                                valueBuffer.append(c);
                                break;
                        }
                        break;
                    case WaitKeyEndSeparator:
                        switch (c) {
                            case ':':
                                currentParsingState = ParsingState.WaitValue;
                                break;
                            case ']':
                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements, rootElement);
                                    currentParsingState = afterValue(currentElement);
                                }
                                break;
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;
                    case WaitValue:
                        c = skipSpace(reader, c);
                        switch (c) {
                            case '\0':
                                throw new CSONParseException("Unexpected end of stream");
                            case '\'':
                            case '"':
                                currentQuoteChar = c;
                                currentParsingState = ParsingState.String;
                                valueBuffer.setOnlyString(true);
                                break;
                            case ']':
                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements, rootElement);
                                    currentParsingState = afterValue(currentElement);
                                    // todo after value 주석 처리 고민.
                                }
                                break;
                            case '{':
                                parentElement = currentElement;
                                currentElement = new CSONObject();
                                csonElements.push(currentElement);

                                // todo value after comment 처리 고민해서 넣어야함.
                                lastKey = putElementData(parentElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                if(allowComment && key != null) {
                                    parentKeyStack.push(key);
                                }
                                key = null;
                                currentParsingState = ParsingState.WaitKey;
                                break;
                            case '[':
                                parentElement = currentElement;
                                currentElement = new CSONArray();
                                csonElements.push(currentElement);
                                lastKey = putElementData(parentElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                if(allowComment && key != null) {
                                    parentKeyStack.push(key);
                                }
                                key = null;
                                // 이미 waitValue 상태이므로, 굳이 상태를 변경하지 않아도 된다.
                                // currentParsingState = ParsingState.WaitValue;
                                break;
                            default:
                                if(!allowUnquoted) {
                                    currentParsingState = ParsingState.Number;
                                } else  {
                                    currentParsingState = ParsingState.InValueUnquoted;

                                };
                                c = skipSpace(reader, c);
                                valueBuffer.append(c);
                                break;
                        }
                        break;
                    case String:

                        if (c == currentQuoteChar) {
                            putStringData(currentElement, valueBuffer.toString(), key);
                            valueBuffer.reset();
                            if (allowComment && key != null) {
                                parentKeyStack.push(key);
                            }
                            key = null;
                            currentParsingState = afterValue(currentElement);
                        } else {
                            valueBuffer.append(c);
                        }

                        break;
                    case InValueUnquoted:
                    case Number:
                        switch (c) {
                            case '\n':
                            case '\r':
                            case '\t':
                            case ' ':
                            case ',':
                            case ']':
                            case '}':
                                Object value  = valueBuffer.parseValue();
                                putData(value, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                if(allowComment && key != null) {
                                    parentKeyStack.push(key);
                                }
                                key = null;
                                valueBuffer.reset();
                                if(c == ',') {
                                    currentParsingState = afterComma(currentElement);
                                } else if(c == ']') {
                                    if(currentElement == rootElement) {
                                        currentParsingState = ParsingState.Close;

                                    } else {
                                        currentElement = popElement(csonElements, rootElement);
                                        currentParsingState = afterValue(currentElement);
                                    }
                                } else if(c == '}') {
                                    if(currentElement == rootElement) {
                                        currentParsingState = ParsingState.Close;
                                    } else {
                                        currentElement = popElement(csonElements, rootElement);
                                        currentParsingState = afterValue(currentElement);
                                    }
                                }

                                break;
                            default:
                                valueBuffer.append((char) c);
                                break;
                        }
                        break;
                    case WaitNextStoreSeparatorInObject:
                        c = skipSpace(reader, c);
                        switch (c) {
                            case ',':
                                currentParsingState = ParsingState.WaitKey;
                                break;
                            case '}':
                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements, rootElement);
                                    currentParsingState = afterValue(currentElement);
                                    // todo after value 주석 처리 고민.
                                }
                                break;
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;
                    case WaitNextStoreSeparatorInArray:
                        c = skipSpace(reader, c);
                        switch (c) {
                            case ',':
                                currentParsingState = ParsingState.WaitValue;
                                break;
                            case ']':
                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements, rootElement);
                                    currentParsingState = afterValue(currentElement);
                                    // todo after value 주석 처리 고민.
                                }
                                break;
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;

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



    }

    private static CSONElement popElement(ArrayStack<CSONElement> csonElements, CSONElement rootElement) {
        csonElements.pop();
        return csonElements.peek();
    }



    private static char skipSpace(Reader reader, char current) throws IOException {
        if (!isSpace(current)) {
            return current;
        }


        int c;
        while((c = reader.read()) != -1) {
            if(!isSpace((char)c)) {
                return (char)c;
            }
        }
        return '\0';
    }

    private static ParsingState afterValue(CSONElement element) {
        return element instanceof CSONArray ? ParsingState.WaitNextStoreSeparatorInArray : ParsingState.WaitNextStoreSeparatorInObject;
    }


    private static ParsingState afterComma(CSONElement element) {
        if(element instanceof CSONArray) {
            return ParsingState.WaitValue;
        } else {
            return ParsingState.WaitKey;
        }
    }

    private static boolean isSpace(char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                return true;
        }
        return false;
    }

    private static boolean isEndOfKeyAtUnquoted(char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
            case ':':
                return true;
        }
        return false;
    }


    private static void putData(Object value, CSONElement currentElement, String key, boolean allowComment, CommentObject keyCommentObject, CommentObject valueCommentObject) {

        if(currentElement instanceof CSONObject) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray)currentElement).addByParser(value);
        }
        if(allowComment) {
            putComment(currentElement, key, keyCommentObject, valueCommentObject);
        }
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


    private static void putStringData(CSONElement currentElement, String value, String key) {

        if(key != null) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray) currentElement).addByParser(value);
        }
    }



    private static String putElementData(CSONElement currentElement, CSONElement value, String key, boolean allowComment, CommentObject keyCommentObject, CommentObject valueCommentObject) {
        if(key != null) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray)currentElement).addByParser(value);
        }
        if(allowComment) {
            putComment(currentElement, key, keyCommentObject, valueCommentObject);
        }
        return key;
    }



}
