package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;

public class CommentBuffer {

    private CommentParsingState commentParsingState = CommentParsingState.None;
    private CharacterBuffer commentBuffer;
    private ParsingState lastState;

    public CommentBuffer() {}

    public ParsingState start(ParsingState lastState) {
        this.lastState = lastState;
        switch (lastState) {
            case WaitKey:
                commentParsingState = CommentParsingState.BeforeKey;
                break;
            case WaitKeyEndSeparator:
                commentParsingState = CommentParsingState.AfterKey;
                break;
            case WaitValue:
                commentParsingState = CommentParsingState.BeforeValue;
                break;
            case WaitNextStoreSeparatorInArray:
            case WaitNextStoreSeparatorInObject:
                commentParsingState = CommentParsingState.AfterValue;
                break;
        }
        if(commentBuffer == null) {
            commentBuffer = new CharacterBuffer();
        }
        commentBuffer.reset();
        return null;
    }

    public void append(char c) {
        if(commentBuffer != null) {
            commentBuffer.append(c);
        }
    }




}
