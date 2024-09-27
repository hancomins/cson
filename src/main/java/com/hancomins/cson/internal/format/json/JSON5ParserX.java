package com.hancomins.cson.internal.format.json;

import com.hancomins.cson.internal.CSONArray;
import com.hancomins.cson.internal.CSONElement;
import com.hancomins.cson.internal.CSONObject;
import com.hancomins.cson.internal.ExceptionMessages;
import com.hancomins.cson.options.JsonParsingOptions;
import com.hancomins.cson.options.JsonWritingOptions;
import com.hancomins.cson.internal.util.ArrayStack;
import com.hancomins.cson.internal.util.CharacterBuffer;
import com.hancomins.cson.internal.util.NullValue;

import java.io.IOException;
import java.io.Reader;

import static com.hancomins.cson.internal.format.json.ParsingState.Open;


public class JSON5ParserX {


    private ParsingState parsingState = Open;
    private final ValueBuffer valueBuffer;
    private final ArrayStack<CSONElement> csonElementStack = new ArrayStack<>();

    private String currentKey = null;
    private CommentBuffer commentBuffer = null;
    private String comment = null;
    private CommentParsingState commentParsingState = CommentParsingState.None;

    private CSONElement currentElement = null;
    private CSONElement parentElement = null;


    int line = 1;

    private final boolean allowUnquoted;
    private final boolean allowComment;
    private final boolean ignoreNonNumeric;
    private final boolean ignoreTrailingData;
    private final boolean skipComments;
    private ReadCountReader readCountReader;

    private JSON5ParserX(JsonParsingOptions jsonOption) {
        this.allowUnquoted = jsonOption.isAllowUnquoted();
        this.allowComment = jsonOption.isAllowComments();
        this.ignoreNonNumeric = jsonOption.isIgnoreNonNumeric();
        this.skipComments = jsonOption.isSkipComments();
        this.consecutiveCommas = jsonOption.isAllowConsecutiveCommas();
        CharacterBuffer keyBuffer = new CharacterBuffer(128);
        this.ignoreTrailingData = jsonOption.isIgnoreTrailingData();
        valueBuffer = new ValueBuffer(keyBuffer, jsonOption);
        valueBuffer.setAllowControlChar(jsonOption.isAllowControlCharacters());
        valueBuffer.setIgnoreControlChar(jsonOption.isIgnoreControlCharacters());
    }



    private CSONElement rootElement;

    private boolean isJSON5 = false;
    private boolean hasComment = false;
    private boolean consecutiveCommas = false;
    private String headComment = null;


    @SuppressWarnings("UnusedReturnValue")
    public static CSONElement parse(Reader reader, CSONElement rootElement, JsonParsingOptions jsonOption) {
        JSON5ParserX parser = new JSON5ParserX(jsonOption);

        parser.doParse(reader, rootElement);

        return rootElement;
    }





    private void doParse(Reader reader, CSONElement rootElement_) {


        reader = this.readCountReader  = new ReadCountReader(reader);

        this.rootElement = rootElement_;

        try {
            int v;



            while((v = reader.read()) != -1) {
                char c = (char)v;

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
                    case Close:
                        inStateClose(reader, c);
                        break;
                    case Comment:
                        inStateComment(c);
                        break;
                }
            }

            // todo : parsingState 가 Close 가 아닌 경우 예외 처리.

            switch (parsingState) {
                case Comment:
                    inStateComment('\0');
                    if (parsingState != ParsingState.Close) {
                        throw new CSONParseException("Unexpected end of stream", line, readCountReader.readCount);
                    }
                    break;
                case Close:
                    break;
                default:
                    throw new CSONParseException("Unexpected end of stream", line, readCountReader.readCount);
            }

        } catch (IOException e) {
            throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.STRING_READ_ERROR), line, readCountReader.readCount, e);
        } finally {
            readCountReader = null;
            try {
                reader.close();
            } catch (IOException ignored) {}
        }


        endParse();


    }

    private void endParse() {
        isJSON5 = isJSON5 || hasComment;
        rootElement.setWritingOptions(isJSON5 ?  (hasComment ? JsonWritingOptions.json5() :  JsonWritingOptions.prettyJson5()) : JsonWritingOptions.json());
        readCountReader = null;
    }

    private void inStateClose(Reader reader, char c) throws IOException {
        if(ignoreTrailingData) {
            return;
        }
        c = skipSpace(reader, c);
        switch (c) {
            case '\0':
                return;
            case '/':
                startParsingCommentMode();
                return;
            default:
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, c), line, readCountReader.readCount);
        }
    }

    private void inStateOpen(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case '{':
                if(rootElement == null) rootElement = currentElement = new CSONObject();
                else {
                    currentElement = rootElement;
                }
                if(headComment != null) {
                    currentElement.setHeadComment(headComment);
                }
                parsingState = ParsingState.WaitKey;
                currentElement.setHeadComment(headComment);
                csonElementStack.push(rootElement);
                break;
            case '[':
                if(rootElement == null) {
                    rootElement = currentElement = new CSONArray();
                } else {
                    currentElement = rootElement;
                }
                if(headComment != null) {
                    currentElement.setHeadComment(headComment);
                }
                parsingState = ParsingState.WaitValue;
                csonElementStack.push(rootElement);
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.JSON5_BRACKET_NOT_FOUND), line, readCountReader.readCount);
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
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN_LONG, c," : / "), line, readCountReader.readCount);

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
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.END_OF_STREAM), line, readCountReader.readCount);
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
                if(!allowUnquoted) {
                    throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, c), line, readCountReader.readCount);
                } else {
                    putNullData(currentElement, currentKey);
                    parsingState = afterComma(currentElement);
                }
                break;
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
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN_LONG, current, ", } /") , line, readCountReader.readCount);
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
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN_LONG, current, ", ] /") , line, readCountReader.readCount);
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
                if(!consecutiveCommas) {
                    throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, current), line, readCountReader.readCount);
                }
                break;
            default:
                if(allowUnquoted) {
                    isJSON5 = true;
                    parsingState = ParsingState.InKeyUnquoted;
                    valueBuffer.append(current);
                } else {
                    throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, current) , line, readCountReader.readCount);
                }
                break;
        }
    }



    private void inStateComment(char current) {
        if(commentBuffer == null) {
            // 코멘트를 지원하지 않는 예외.
            throw new CSONParseException("Invalid JSON5 document", line, readCountReader.readCount);
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
                    throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, current) , line, readCountReader.readCount);
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
                        if(commentParsingState == CommentParsingState.BeforeKey) {
                            ((CSONObject)currentElement).appendCommentForKey(currentKey, comment);
                            commentParsingState = CommentParsingState.None;
                        }
                        break;
                }

                break;
            case End:
                parsingState = commentBuffer.lastParsingState();
                CommentParsingState oldCommentParsingState = commentParsingState;
                commentParsingState = commentBuffer.commentParsingState();
                String newComment = commentBuffer.getComment();
                if(newComment.isEmpty()) {
                    commentParsingState = CommentParsingState.None;
                    comment = "";
                }
                else if(oldCommentParsingState == commentParsingState) {
                    comment += "\n" + newComment;
                } else {
                    comment = newComment;
                }
                if(skipComments) {
                    comment = null;
                }
                else if(currentElement instanceof CSONObject) {
                    setCommentToCSONObject();
                } else if(currentElement instanceof CSONArray) {
                    setCommentToCSONArray();
                }  else {
                    headComment = comment;
                }

                break;
        }
    }

    private void setCommentToCSONObject() {
        CSONObject csonObject = (CSONObject)currentElement;


        switch (commentParsingState) {
            case BeforeKey:
                csonObject.setCommentForKey(currentKey, comment);
                break;
            case AfterKey:
                csonObject.setCommentAfterKey(currentKey, comment);
                break;
            case BeforeValue:
                csonObject.setCommentForValue(currentKey, comment);
                break;
            case AfterValue:
                String lastKey = csonObject.getLastPutKeyAndSetNull();
                csonObject.setCommentAfterValue(lastKey, comment);
                break;
            case Tail:
                csonObject.setTailComment(comment);
                break;
        }
    }

    private void setCommentToCSONArray() {
        CSONArray csonArray = (CSONArray)currentElement;
        int size = csonArray.size();
        switch (commentParsingState) {
            case BeforeValue:
                csonArray.setCommentForValue(size, comment);
                break;
            case AfterValue:
                csonArray.setCommentAfterValue(size - 1, comment);
                break;
            case Tail:
                csonArray.setTailComment(comment);
                break;
        }
    }






    private void closeCSONElement() {
        commentParsingState = CommentParsingState.None;
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
        if(commentParsingState == CommentParsingState.BeforeKey) {
            ((CSONObject)currentElement).appendCommentForKey(currentKey, comment);
            commentParsingState = CommentParsingState.None;
        }
    }



    private void startParsingCommentMode() {
        if(commentBuffer == null) {
            if(!allowComment) {
                throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.NOT_ALLOWED_COMMENT), line, readCountReader.readCount);
            }
            hasComment = true;
            commentBuffer = new CommentBuffer();
        }
        parsingState = startParsingCommentMode(commentBuffer, parsingState);
    }

    private void putCSONObject() {
        commentParsingState = CommentParsingState.None;
        parentElement = currentElement;
        currentElement = new CSONObject();
        csonElementStack.push(currentElement);
        putElementData(parentElement, currentElement, currentKey);
        parsingState = ParsingState.WaitKey;
        currentKey = null;
    }


    private void putCSONArray() {
        commentParsingState = CommentParsingState.None;
        parentElement = currentElement;
        currentElement = new CSONArray();
        csonElementStack.push(currentElement);
        putElementData(parentElement, currentElement, currentKey);
        currentKey = null;
    }


    private ParsingState startParsingCommentMode(CommentBuffer commentBuffer, ParsingState currentParsingState) {
        if(commentBuffer == null) {
            throw new CSONParseException("Comment unsupported", line, readCountReader.readCount);
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
                readCountReader.readCount = 0;
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
        if(inValue instanceof String) {
            isJSON5 = true;
            if(!allowUnquoted) {
                if(ignoreNonNumeric) {
                    inValue = NullValue.Instance;
                } else {
                    throw new CSONParseException(ExceptionMessages.formatMessage(ExceptionMessages.NOT_ALLOWED_UNQUOTED_STRING), line, readCountReader.readCount);
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

    private static void putNullData(CSONElement currentElement, String key) {
        if(key != null) {
            ((CSONObject)currentElement).putByParser(key, NullValue.Instance);
        } else {
            ((CSONArray)currentElement).addByParser(NullValue.Instance);
        }
    }



    private static void putElementData(CSONElement currentElement, CSONElement value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).putByParser(key, value);
        } else {
            ((CSONArray)currentElement).addByParser(value);
        }
    }




    private static class ReadCountReader extends Reader {

        private final Reader reader;
        private int readCount = 0;


        public ReadCountReader(Reader reader) {
            this.reader = reader;
        }


        @Override
        public int read() throws IOException {
            ++readCount;
            return this.reader.read();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read = this.reader.read(cbuf, off, len);
            readCount += read;
            return read;
        }


        @Override
        public void close() throws IOException {
            this.reader.close();
        }
    }

}
