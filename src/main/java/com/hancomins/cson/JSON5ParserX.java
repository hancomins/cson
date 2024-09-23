package com.hancomins.cson;

import com.hancomins.cson.options.JsonParsingOptions;
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
    static void parse(Reader reader,CSONElement rootElement, JsonParsingOptions jsonOption) {

        final boolean singleQuote = jsonOption.isAllowSingleQuotes();
        final boolean allowUnquoted = jsonOption.isAllowUnquoted();
        final boolean allowConsecutiveCommas = jsonOption.isAllowConsecutiveCommas();
        final boolean trailingComma = jsonOption.isAllowTrailingComma();



        ParsingState currentParsingState = Open;
        //
        String key = null;


        CommentBuffer commentBuffer = null;



        String comment = null;




        CSONElement currentElement = null;
        CSONElement parentElement = null;


        CharacterBuffer keyBuffer_ = new CharacterBuffer(128);
        ValueBuffer valueBuffer = new ValueBuffer(keyBuffer_,jsonOption);
        ArrayStack<CSONElement> csonElements = new ArrayStack<>();



        CommentState commentState = CommentState.None;


        int line = 1;
        int index = 0;

        try {
            int v;
            boolean cutSearch = false;
            Object value = null;
            PREV_LOOP:
            while((v = reader.read()) != -1) {
                char c = (char)v;
                ++index;

                //System.out.print(c);


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
                            case '\'':
                            case '"':
                                currentParsingState = ParsingState.InKey;
                                valueBuffer.setOnlyString(c);
                                break;
                            case '}':
                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements);
                                    currentParsingState = afterValue(currentElement);
                                }
                                break;
                            case '/': // 코멘트 파싱 모드.
                                if(commentBuffer == null) {
                                    commentBuffer = new CommentBuffer();
                                }
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;
                            case ',':
                                throw new CSONParseException("Invalid JSON5 document");
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
                                if(commentState == CommentState.BeforeKey) {
                                    //noinspection DataFlowIssue
                                    ((CSONObject)currentElement).appendCommentForKey(key, comment);
                                    commentState = CommentState.None;
                                }
                                continue PREV_LOOP;
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
                                    key = valueBuffer.getStringAndReset();
                                    if(commentState == CommentState.BeforeKey) {
                                        //noinspection DataFlowIssue
                                        ((CSONObject)currentElement).appendCommentForKey(key, comment);
                                        commentState = CommentState.None;
                                    }
                                    continue PREV_LOOP;
                                case ':':
                                    currentParsingState = ParsingState.WaitValue;
                                    key = valueBuffer.getStringAndReset();
                                    if(commentState == CommentState.BeforeKey) {
                                        //noinspection DataFlowIssue
                                        ((CSONObject)currentElement).appendCommentForKey(key, comment);
                                        commentState = CommentState.None;
                                    }
                                    continue PREV_LOOP;
                                case '/':
                                    if(commentBuffer == null) {
                                        commentBuffer = new CommentBuffer();
                                    }
                                    currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                    continue PREV_LOOP;
                                default:
                                    valueBuffer.append(c);
                            }
                        } while ((v = reader.read()) != -1);
                        break;
                    case WaitKeyEndSeparator:
                        c = skipSpace(reader, c);
                        switch (c) {
                            case ':':
                                currentParsingState = ParsingState.WaitValue;
                                break;
                            case '/':
                                if(commentBuffer == null) {
                                    commentBuffer = new CommentBuffer();
                                }
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
                                currentParsingState = ParsingState.String;
                                valueBuffer.setOnlyString(c);
                                break;
                            case ']':
                                if(currentElement == rootElement) {
                                    currentParsingState = ParsingState.Close;
                                } else {
                                    currentElement = popElement(csonElements);
                                    currentParsingState = afterValue(currentElement);
                                }
                                break;
                            case '{':
                                parentElement = currentElement;
                                currentElement = new CSONObject();
                                csonElements.push(currentElement);
                                putElementData(parentElement, currentElement, key);
                                currentParsingState = ParsingState.WaitKey;
                                key = null;
                                break;
                            case '[':
                                parentElement = currentElement;
                                currentElement = new CSONArray();
                                csonElements.push(currentElement);
                                putElementData(parentElement, currentElement, key);
                                key = null;
                                // 이미 waitValue 상태이므로, 굳이 상태를 변경하지 않아도 된다.
                                // currentParsingState = ParsingState.WaitValue;
                                break;
                            case '/':
                                if(commentBuffer == null) {
                                    commentBuffer = new CommentBuffer();
                                }
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;
                            case ',':
                                throw new CSONParseException("Invalid JSON5 document");
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
                                continue PREV_LOOP;
                            }
                        } while((v = reader.read()) != -1);
                        break;
                    case InValueUnquoted:
                    case Number:
                        do {
                            c = (char)v;
                            switch (c) {
                                case ',':
                                    putData(valueBuffer.parseValue(), currentElement, key);
                                    currentParsingState = afterComma(currentElement);
                                    key = null;
                                    continue PREV_LOOP;
                                case ' ':
                                case '\n':
                                case '\t':
                                    putData(valueBuffer.parseValue(), currentElement, key);
                                    currentParsingState = afterValue(currentElement);
                                    key = null;
                                    continue PREV_LOOP;
                                case '}':
                                case ']':
                                    putData(valueBuffer.parseValue(), currentElement, key);
                                    key = null;
                                    if (currentElement == rootElement) {
                                        currentParsingState = ParsingState.Close;
                                    } else {
                                        currentElement = popElement(csonElements);
                                        currentParsingState = afterValue(currentElement);
                                    }
                                    continue PREV_LOOP;
                                case '/':
                                    if(commentBuffer == null) {
                                        commentBuffer = new CommentBuffer();
                                    }
                                    if(currentParsingState == ParsingState.Number) {
                                        putData(valueBuffer.parseValue(), currentElement, key);
                                    }
                                    currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                    continue PREV_LOOP;
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
                                    currentElement = popElement(csonElements);
                                    currentParsingState = afterValue(currentElement);
                                }
                                break;
                            case '/':
                                if(commentBuffer == null) {
                                    commentBuffer = new CommentBuffer();
                                }
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
                                    currentElement = popElement(csonElements);
                                    currentParsingState = afterValue(currentElement);
                                }
                                break;
                            case '/':
                                if(commentBuffer == null) {
                                    commentBuffer = new CommentBuffer();
                                }
                                currentParsingState = startParsingComment(commentBuffer, currentParsingState);
                                break;
                            default:
                                throw new CSONParseException("Invalid JSON5 document");
                        }
                        break;
                    case Comment:
                        if(commentBuffer == null) {
                            // 코멘트를 지원하지 않는 예외.
                            throw new CSONParseException("Invalid JSON5 document");
                        }
                        CommentBuffer.AppendResult result = commentBuffer.append(c);
                        ParsingState lastParsingState = null;
                        switch (result) {
                            case Fail:
                                currentParsingState = commentBuffer.lastParsingState();
                                // 주석 상태가 유효하지 않다면, 주석 상태를 끝내고 다음 상태로 전환한다.
                                if(currentParsingState == ParsingState.InValueUnquoted || currentParsingState == ParsingState.InKeyUnquoted) {
                                    valueBuffer.append('/');
                                    valueBuffer.append(c);
                                    continue;
                                } else {
                                    throw new CSONParseException("Invalid JSON5 document");
                                }
                            case InComment:
                                lastParsingState = commentBuffer.lastParsingState();
                                switch (lastParsingState) {
                                    case InValueUnquoted:
                                    case Number:
                                        putData(valueBuffer.parseValue(), currentElement, key);
                                        commentBuffer.changeLastParsingState(afterValue(currentElement));
                                        break;
                                    case InKeyUnquoted:
                                        key = valueBuffer.getStringAndReset();
                                        commentBuffer.changeLastParsingState(ParsingState.WaitKeyEndSeparator);
                                        if(commentState == CommentState.BeforeKey) {
                                            ((CSONObject)currentElement).appendCommentForKey(key,comment);
                                            commentState = CommentState.None;
                                        }
                                        break;
                                }

                                break;
                            case End:
                                currentParsingState = commentBuffer.lastParsingState();
                                CommentState oldCommentState = commentState;
                                commentState = commentBuffer.commentParsingState();
                                String newComment = commentBuffer.getComment();
                                if(newComment.isEmpty()) {
                                    commentState = CommentState.None;
                                    comment = "";
                                }
                                else if(oldCommentState == commentState) {
                                    comment += "\n" + newComment;
                                } else {
                                    comment = newComment;
                                }


                                if(currentElement instanceof CSONObject) {
                                    CSONObject csonObject = (CSONObject)currentElement;
                                    if (commentState == CommentState.AfterKey) {
                                        csonObject.setCommentAfterKey(key, comment);
                                    } else if (commentState == CommentState.BeforeValue) {
                                        csonObject.setCommentForValue(key, comment);
                                    } else if(commentState == CommentState.AfterValue) {
                                        String lastKey = csonObject.getLastPutKeyAndSetNull();
                                        csonObject.setCommentAfterValue(lastKey, comment);
                                    }
                                } else {
                                    CSONArray csonArray = (CSONArray)currentElement;
                                    int size = csonArray.size();
                                    if (commentState == CommentState.BeforeValue) {
                                        csonArray.setCommentForValue(size, comment);
                                    } else if(commentState == CommentState.AfterValue) {
                                        csonArray.setCommentAfterValue(size - 1, comment);
                                    }
                                }

                                break;
                        }
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


    private static CSONElement popElement(ArrayStack<CSONElement> csonElements) {
        csonElements.pop();
        return csonElements.peek();
    }



    private static char skipSpace(Reader reader, char current) throws IOException {
        if (isNotSpace(current)) {
            return current;
        }


        int c;
        while((c = reader.read()) != -1) {
            if(isNotSpace((char) c)) {
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

    private static boolean isNotSpace(char c) {
        switch (c) {
            case '\n':
            case '\r':
            case '\t':
            case ' ':
                return false;
        }
        return true;
    }


    private static void putData(Object value, CSONElement currentElement, String key) {

        if(currentElement instanceof CSONObject) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray)currentElement).addByParser(value);
        }
    }




    private static void putStringData(CSONElement currentElement, String value, String key) {

        if(key != null) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray) currentElement).addByParser(value);
        }
    }



    private static void putElementData(CSONElement currentElement, CSONElement value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray)currentElement).addByParser(value);
        }
    }



}
