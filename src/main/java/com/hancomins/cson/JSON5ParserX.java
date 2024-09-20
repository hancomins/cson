package com.hancomins.cson;

import com.hancomins.cson.util.ArrayStack;
import com.hancomins.cson.util.CharacterBuffer;

import java.io.IOException;
import java.io.Reader;

import static com.hancomins.cson.ParsingState.Open;


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



        ParsingState currentParsingState = Open;
        //
        String key = null;


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
            boolean cutSearch = false;
            Object value = null;
            top:
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
                                currentParsingState = ParsingState.InKey;
                                valueBuffer.setOnlyString(c);
                                break;
                            /*case '/': // 코멘트 파싱 모드.
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;*/
                            default:
                                if(allowUnquoted) {
                                    currentParsingState = ParsingState.InKeyUnquoted;
                                    valueBuffer.setOnlyString('\0').append(c);
                                } else {
                                    throw new CSONParseException("Invalid JSON5 document");
                                }
                                break;
                        }
                        break;
                    case InKey:
                        do {
                            c = (char)v;
                            valueBuffer.append(c);
                            if(valueBuffer.isEndQuote()) {
                                key = valueBuffer.getStringAndReset();
                                currentParsingState = ParsingState.WaitKeyEndSeparator;
                                continue top;
                            }
                        } while((v = reader.read()) != -1);
                        break;
                    case InKeyUnquoted:
                        do {
                            c = (char)v;
                            switch (c) {
                                case '\n':
                                case '\t':
                                case ' ':
                                    currentParsingState = ParsingState.WaitKeyEndSeparator;
                                    continue top;
                                case ':':
                                    currentParsingState = ParsingState.WaitValue;
                                    key = valueBuffer.getStringAndReset();
                                    continue top;
                                /*case '/':
                                    currentParsingState = startParsingComment(commentBuffer, ParsingState.WaitValue);
                                    key = valueBuffer.getStringAndReset();
                                    continue top;*/
                                default:
                                    valueBuffer.append(c);
                            }
                        } while ((v = reader.read()) != -1);
                        break;
                    case WaitKeyEndSeparator:
                        switch (c) {
                            case ':':
                                currentParsingState = ParsingState.WaitValue;
                                break;
                            /*case '/':
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;*/
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
                                currentParsingState = ParsingState.String;
                                valueBuffer.setOnlyString(c);
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
                                putElementData(parentElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                currentParsingState = ParsingState.WaitKey;
                                key = null;
                                break;
                            case '[':
                                parentElement = currentElement;
                                currentElement = new CSONArray();
                                csonElements.push(currentElement);
                                putElementData(parentElement, currentElement, key, allowComment, keyCommentObject, valueCommentObject);
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
                        do {
                            c = (char)v;
                            valueBuffer.append(c);
                            if(valueBuffer.isEndQuote()) {
                                putStringData(currentElement, valueBuffer.getStringAndReset(), key);
                                key = null;
                                currentParsingState = afterValue(currentElement);
                                continue top;
                            }
                        } while((v = reader.read()) != -1);
                        break;
                    case InValueUnquoted:
                    case Number:
                        do {
                            c = (char)v;
                            switch (c) {
                                case ',':
                                    putData(valueBuffer.parseValue(), currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                    currentParsingState = afterComma(currentElement);
                                    key = null;
                                    continue top;
                                case ' ':
                                case '\n':
                                case '\t':
                                    putData(valueBuffer.parseValue(), currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                    currentParsingState = afterValue(currentElement);
                                    key = null;
                                    continue top;
                                case '}':
                                case ']':
                                    putData(valueBuffer.parseValue(), currentElement, key, allowComment, keyCommentObject, valueCommentObject);
                                    key = null;
                                    if (currentElement == rootElement) {
                                        currentParsingState = ParsingState.Close;
                                    } else {
                                        currentElement = popElement(csonElements, rootElement);
                                        currentParsingState = afterValue(currentElement);
                                    }
                                    continue top;
                                /*case '/':
                                    break;*/
                                default:
                                    valueBuffer.append(c);
                                    break;
                            }
                        } while ((v = reader.read()) != -1);
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
                            /*case '/':
                               currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                               break;*/
                            default:
                                System.out.println("key: " + currentElement);
                                System.out.println("c: " + c);
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
                            /*case '/':
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;*/
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;
                    case Comment:

                    break;

                }






            }


            if(currentParsingState != ParsingState.Close) {
                throw new CSONParseException("Unexpected end of stream");
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
