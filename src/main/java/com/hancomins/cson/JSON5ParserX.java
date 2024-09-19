package com.hancomins.cson;

import com.hancomins.cson.util.ArrayStack;
import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;

import static com.hancomins.cson.ParsingState.Open;

/* 코멘트 파싱 위치.
    1. key 를 기다리는 상태. (Object)
    2. " 가 있던 key 뒤에 콜론을 기다리는 상태. (Object)



 */

final class JSON5ParserX {



    private JSON5ParserX() {
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
        ParsingState lastParsingState = null;
        //
        String key = null;
        //
        String lastKey = null;
        ArrayStack<String> parentKeyStack = null;

        CommentBuffer commentBuffer = null;
        if(allowComment) {
            commentBuffer = new CommentBuffer();
        }


        CommentObject keyCommentObject = null;
        CommentObject valueCommentObject = null;


        ParsingState commentBeforeParsingState = null;




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

                ++index;



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
                            case '/': // 코멘트 파싱 모드.
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
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
                        do {
                            c = (char)v;



                            switch (c) {
                                case '\n':
                                case '\r':
                                case '\t':
                                case ' ':
                                    currentParsingState = ParsingState.WaitKeyEndSeparator;
                                    break;

                                case ':':
                                    currentParsingState = ParsingState.WaitValue;
                                    key = valueBuffer.getStringAndReset();
                                    break;
                                case '/':
                                    currentParsingState = startParsingComment(commentBuffer, ParsingState.WaitValue);
                                    key = valueBuffer.getStringAndReset();
                                    break;
                                default:
                                    valueBuffer.append(c);
                                    break;
                            }

                        } while ((v = reader.read()) != -1);
                        if(v == -1) {
                            throw new CSONParseException("Unexpected end of stream");
                        }


                        break;
                    case WaitKeyEndSeparator:
                        switch (c) {
                            case ':':
                                currentParsingState = ParsingState.WaitValue;
                                break;
                            case '/':
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
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
                                if(c == '/') {
                                    v = reader.read();
                                    if(v == -1) {
                                        throw new CSONParseException("Unexpected end of stream");
                                    }
                                    c = (char)v;
                                    if(c == '/' || c == '*') {
                                        currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                        commentBuffer.append(c);
                                    }


                                }

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
                            case ' ':
                            case ',':
                            case '}':
                            case ']':
                            case '\n':
                            case '\r':
                            case '\t':
                                // todo 조건문이 반복적으로 실행 해결.
                                Object value  = valueBuffer.parseValue();
                                putData(value, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                if(allowComment && key != null) {
                                    parentKeyStack.push(key);
                                }
                                key = null;
                                currentParsingState = afterValue(currentElement);
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
                            case '/':
                                v = reader.read();
                                if(v == -1) {
                                    throw new CSONParseException("Unexpected end of stream");
                                }
                                c = (char)v;
                                if(c == '/' || c == '*') {
                                    value  = valueBuffer.parseValue();
                                    putData(value, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                    currentParsingState = startParsingComment(commentBuffer, afterValue(currentElement));
                                } else {
                                    valueBuffer.append('/');
                                    valueBuffer.append(c);
                                }
                            break;
                            default:
                                valueBuffer.append(c);
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
                            case '/':
                               currentParsingState = startParsingComment(commentBuffer, currentParsingState);
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
                            case '/':
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;
                    case Comment:

                    break;

                }








            }



        } catch (IOException e) {
            throw new CSONParseException(e.getMessage());
        }



    }


    private static ParsingState startParsingComment(CommentBuffer commentBuffer, ParsingState currentParsingState) {
        if(commentBuffer == null) {
            throw new CSONParseException("Invalid JSON5 document");
        }
        return commentBuffer.start(currentParsingState);
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
