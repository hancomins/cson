package com.hancomins.cson;

import com.hancomins.cson.options.JsonParsingOptions;
import com.hancomins.cson.options.JsonWritingOptions;
import com.hancomins.cson.util.ArrayStack;
import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.NullValue;

import java.io.IOException;
import java.io.Reader;

import static com.hancomins.cson.ParsingState.Open;


public class JSON5ParserX {


    private ParsingState parsingState = Open;
    private final ValueBuffer valueBuffer;
    private final ArrayStack<CSONElement> csonElementStack = new ArrayStack<>();

    private String currentKey = null;
    private CommentBuffer commentBuffer = null;
    private String comment = null;
    private CommentState commentState = CommentState.None;

    private CSONElement currentElement = null;
    private CSONElement parentElement = null;

    int index = 0;
    int line = 0;

    private final boolean allowUnquoted;
    private final boolean allowComment;
    private final boolean ignoreNonNumeric;

    public JSON5ParserX(JsonParsingOptions jsonOption) {
        this.allowUnquoted = jsonOption.isAllowUnquoted();
        this.allowComment = jsonOption.isAllowComment();
        this.ignoreNonNumeric = jsonOption.isIgnoreNonNumeric();
        CharacterBuffer keyBuffer = new CharacterBuffer(128);
        valueBuffer = new ValueBuffer(keyBuffer, jsonOption);
    }



    /*public static <T extends CSONElement> T parse(Reader reader, JsonParsingOptions jsonOption) {
        //noinspection unchecked
        return (T) parse(reader, null, jsonOption);
    }

    public static CSONObject parseObject(Reader reader, JsonParsingOptions jsonOption) {
        CSONElement csonElement = parse(reader, null, jsonOption);
        if(csonElement instanceof CSONObject) {
            return (CSONObject)csonElement;
        } else {
            CSONObject csonObject = new CSONObject(csonElement.getWritingOptions());
            csonObject.putByParser("array", csonElement);
            return csonObject;
        }
    }

    public static CSONArray parseArray(Reader reader, JsonParsingOptions jsonOption) {
        CSONElement csonElement = parse(reader, null, jsonOption);
        if(csonElement instanceof CSONArray) {
            return (CSONArray) csonElement;
        } else {
            CSONArray csonArray = new CSONArray(csonElement.getWritingOptions());
            csonArray.addByParser(csonElement);
            return csonArray;
        }

    }


*/


    private CSONElement rootElement;

    boolean isJSON5 = false;
    boolean hasComment = false;

    public CSONElement parse(Reader reader,CSONElement rootElement_) {

        this.rootElement = rootElement_;

        try {
            int v;
            PREV_LOOP:
            while((v = reader.read()) != -1) {
                char c = (char)v;


                if(c == 0) break;



                switch (parsingState) {
                    case Open:
                        inStateOpen(reader, c);
                        break;
                    case WaitKey:
                        inStateWaitKey(reader, c);
                        break;
                    case InKey:
                        if(inStateInKey(reader, c)) {
                            continue;
                        }
                        break;
                    case InKeyUnquoted:
                        if(inStateInKeyUnquoted(reader, c)) {
                            continue;
                        }
                        break;
                    case WaitKeyEndSeparator:
                        inStateWaitKeyEndSeparator(reader, c);
                        break;
                    case WaitValue:
                       inStateWaitValue(reader, c);
                        break;
                    case String:
                        if(inStateInStringValue(reader, c)) {
                            continue;
                        }
                        break;
                    case InValueUnquoted:
                        if(inStateInValueUnquoted(reader,c)) {
                            continue;
                        }
                        break;
                    case WaitNextStoreSeparatorInObject:
                        inStateWaitNextStoreSeparatorInObject(reader,c);
                        break;
                    case WaitNextStoreSeparatorInArray:
                        inStateWaitNextStoreSeparatorInArray(reader,c);
                        break;
                    case Comment:
                        inStateComment(reader,c);
                        break;
                }
            }

            if(parsingState != ParsingState.Close) {
                throw new CSONParseException("Unexpected end of stream");
            }

        } catch (IOException e) {
            throw new CSONParseException(e.getMessage());
        }

        isJSON5 = isJSON5 || hasComment;
        rootElement.setWritingOptions(isJSON5 ?  (hasComment ? JsonWritingOptions.json5() :  JsonWritingOptions.prettyJson5()) : JsonWritingOptions.json());

        return rootElement;
    }

    private void inStateOpen(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case '{':
                if(rootElement == null) rootElement = currentElement = new CSONObject();
                else {
                    currentElement = rootElement;
                }
                parsingState = ParsingState.WaitKey;
                csonElementStack.push(rootElement);
                break;
            case '[':
                if(rootElement == null) {
                    rootElement = currentElement = new CSONArray();
                } else {
                    currentElement = rootElement;
                }
                parsingState = ParsingState.WaitValue;
                csonElementStack.push(rootElement);
                break;
            default:
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.JSON5_BRACKET_NOT_FOUND, line, index));
        }
    }



    private boolean inStateInStringValue(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            valueBuffer.append(c);
            if(valueBuffer.isEndQuote()) {
                putStringData(currentElement, valueBuffer.getStringAndReset(), currentKey);
                currentKey = null;
                parsingState = afterValue(currentElement);
                return true;
            }
        } while((v = reader.read()) != -1);
        return false;
    }
    
    private boolean inStateInKey(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            valueBuffer.append(c);
            if(valueBuffer.isEndQuote()) {
                keyEnd(ParsingState.WaitKeyEndSeparator);
                return true;
            }
        } while((v = reader.read()) != -1);
        return false;
    }

    private void inStateWaitKeyEndSeparator(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case ':':
                parsingState = ParsingState.WaitValue;
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new CSONParseException("Invalid JSON5 document");
        }
    }

    private boolean inStateInKeyUnquoted(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            switch (c) {
                case '\n':
                case '\t':
                case '\r':
                case ' ':
                    keyEnd(ParsingState.WaitKeyEndSeparator);
                    return true;
                case ':':
                    keyEnd(ParsingState.WaitValue);
                    return true;
                case '/':
                    startParsingCommentMode();
                    return true;
                default:
                    valueBuffer.append(c);
            }
        } while ((v = reader.read()) != -1);
        return false;
    }

    private void inStateWaitValue(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case '\0':
                throw new CSONParseException("Unexpected end of stream");
            case '\'':
                isJSON5 = true;
            case '"':
                parsingState = ParsingState.String;
                valueBuffer.setOnlyString(c);
                break;
            case ']':
                closeCSONElement();
                break;
            case '{':
                putCSONObject();
                break;
            case '[':
                putCSONArray();
                break;
            case '/':
                startParsingCommentMode();
                break;
            case ',':
                throw new CSONParseException("Invalid JSON5 document");
            default:
                parsingState = ParsingState.InValueUnquoted;
                c = skipSpace(reader, c);
                valueBuffer.append(c);
                break;
        }
    }


    private void inStateWaitNextStoreSeparatorInObject(Reader reader, char current) throws IOException {
        current = skipSpace(reader, current);
        switch (current) {
            case ',':
                parsingState = ParsingState.WaitKey;
                break;
            case '}':
                closeCSONElement();
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new CSONParseException("Invalid JSON5 document");
        }
    }

    private void inStateWaitNextStoreSeparatorInArray(Reader reader, char current) throws IOException {
        current = skipSpace(reader, current);
        switch (current) {
            case ',':
                parsingState = ParsingState.WaitValue;
                break;
            case ']':
                closeCSONElement();
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new CSONParseException("Invalid JSON5 document");
        }

    }

    private boolean inStateInValueUnquoted(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            switch (c) {
                case ',':
                    putValueInUnquoted(valueBuffer, currentElement, currentKey, allowUnquoted, ignoreNonNumeric);
                    parsingState = afterComma(currentElement);
                    currentKey = null;
                    return true;
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    putValueInUnquoted(valueBuffer, currentElement, currentKey, allowUnquoted, ignoreNonNumeric);
                    parsingState = afterValue(currentElement);
                    return true;

                case '}':
                case ']':
                    putValueInUnquoted(valueBuffer, currentElement, currentKey, allowUnquoted, ignoreNonNumeric);
                    closeCSONElement();
                    return true;
                case '/':
                    startParsingCommentMode();
                    return true;
                default:
                    valueBuffer.append(c);
                    break;
            }
        } while ((v = reader.read()) != -1);
        return false;
    }

    private void inStateWaitKey(Reader reader, char current) throws IOException {
        current = skipSpace(reader, current);
        switch (current) {
            case '\'':
                isJSON5 = true;
            case '"':
                parsingState = ParsingState.InKey;
                valueBuffer.setOnlyString(current);
                break;
            case '}':
                closeCSONElement();
                break;
            case '/': // 코멘트 파싱 모드.
                startParsingCommentMode();
                break;
            case ',':
                throw new CSONParseException("Invalid JSON5 document");
            default:
                if(allowUnquoted) {
                    isJSON5 = true;
                    parsingState = ParsingState.InKeyUnquoted;
                    valueBuffer.setOnlyString('\0').append(current);
                } else {
                    throw new CSONParseException("Invalid JSON5 document");
                }
                break;
        }
    }

    private void inStateComment(Reader reader, char current) {
        if(commentBuffer == null) {
            // 코멘트를 지원하지 않는 예외.
            throw new CSONParseException("Invalid JSON5 document");
        }
        CommentBuffer.AppendResult result = commentBuffer.append(current);
        ParsingState lastParsingState = null;
        switch (result) {
            case Fail:
                parsingState = commentBuffer.lastParsingState();
                // 주석 상태가 유효하지 않다면, 주석 상태를 끝내고 다음 상태로 전환한다.
                if(parsingState == ParsingState.InValueUnquoted || parsingState == ParsingState.InKeyUnquoted) {
                    valueBuffer.append('/');
                    valueBuffer.append(current);
                    return;
                } else {
                    throw new CSONParseException("Invalid JSON5 document");
                }
            case InComment:
                lastParsingState = commentBuffer.lastParsingState();
                switch (lastParsingState) {
                    case InValueUnquoted:
                        //case Number:
                        // todo: currentKey 가 null 되어도 괜찮은지 check.
                        putValueInUnquoted(valueBuffer, currentElement, currentKey, allowUnquoted, ignoreNonNumeric);
                        commentBuffer.changeLastParsingState(afterValue(currentElement));
                        break;
                    case InKeyUnquoted:
                        currentKey = valueBuffer.getStringAndReset();
                        commentBuffer.changeLastParsingState(ParsingState.WaitKeyEndSeparator);
                        if(commentState == CommentState.BeforeKey) {
                            ((CSONObject)currentElement).appendCommentForKey(currentKey, comment);
                            commentState = CommentState.None;
                        }
                        break;
                }

                break;
            case End:
                parsingState = commentBuffer.lastParsingState();
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
                        csonObject.setCommentAfterKey(currentKey, comment);
                    } else if (commentState == CommentState.BeforeValue) {
                        csonObject.setCommentForValue(currentKey, comment);
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
    }


    private void closeCSONElement() {
        if(currentElement == rootElement) {
            parsingState = ParsingState.Close;
        } else {
            currentElement = popElement(csonElementStack);
            parsingState = afterValue(currentElement);
        }
    }


    private void keyEnd(ParsingState parsingState) {
        this.parsingState = parsingState;
        currentKey = valueBuffer.getStringAndReset();
        if(commentState == CommentState.BeforeKey) {
            ((CSONObject)currentElement).appendCommentForKey(currentKey, comment);
            commentState = CommentState.None;
        }
    }



    private void startParsingCommentMode() {
        if(commentBuffer == null) {
            if(!allowComment) {
                throw new CSONParseException("Invalid JSON5 document");
            }
            hasComment = true;
            commentBuffer = new CommentBuffer();
        }
        parsingState = startParsingCommentMode(commentBuffer, parsingState);
    }

    private void putCSONObject() {
        parentElement = currentElement;
        currentElement = new CSONObject();
        csonElementStack.push(currentElement);
        putElementData(parentElement, currentElement, currentKey);
        parsingState = ParsingState.WaitKey;
        currentKey = null;
    }


    private void putCSONArray() {
        parentElement = currentElement;
        currentElement = new CSONArray();
        csonElementStack.push(currentElement);
        putElementData(parentElement, currentElement, currentKey);
        currentKey = null;
    }


    private static ParsingState startParsingCommentMode(CommentBuffer commentBuffer, ParsingState currentParsingState) {
        if(commentBuffer == null) {
            throw new CSONParseException("Invalid JSON5 document");
        }
        return commentBuffer.start(currentParsingState);
    }


    private static CSONElement popElement(ArrayStack<CSONElement> csonElements) {
        csonElements.pop();
        return csonElements.peek();
    }



    private char skipSpace(Reader reader, char current) throws IOException {
        if(checkSpace(current) != '\0') {
            return current;
        }
        int v;
        while((v = reader.read()) != -1) {
            char c = (char)v;
            if(checkSpace(c) != '\0') {
                return c;
            }
        }
        return '\0';
    }


    private char checkSpace(char current) throws IOException {
        switch (current) {
            case '\n':
                index = 0;
                line++;
                break;
            case '\r':
            case '\t':
            case ' ':
                break;
            default:
                return current;
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

    /**
     * 값을 넣는다.
     * @param valueBuffer 값을 파싱하는 ValueBuffer
     * @param currentElement 현재 Element
     * @param key 키
     * @param allowUnquoted unquoted 허용 여부
     * @param ignoreNonNumeric 숫자가 아닌 값 무시 여부
     */
    private void putValueInUnquoted(ValueBuffer valueBuffer, CSONElement currentElement, String key, boolean allowUnquoted, boolean ignoreNonNumeric) {
        Object inValue = valueBuffer.parseValue();
        if(!(inValue instanceof Number)) {
            isJSON5 = true;
            if(!allowUnquoted) {
                if(ignoreNonNumeric) {
                    inValue = NullValue.Instance;
                } else {
                    throw new CSONParseException("Invalid JSON5 document");
                }
            }
        }
        if(currentElement instanceof CSONObject) {
            ((CSONObject)currentElement).putByParser(key, inValue);
        } else {
            ((CSONArray)currentElement).addByParser(inValue);
        }
        currentKey = null;

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
