package com.hancomins.cson;

import com.hancomins.cson.util.CharacterBuffer;

public class CommentBuffer {

    enum CommentType {
        Line,
        Block,
        Wait
    }

    public enum AppendResult {
        Continue,
        End,
        Fail,
        InComment
    }


    private CommentState commentParsingState = CommentState.None;
    private CharacterBuffer commentBuffer;
    private ParsingState lastState;

    /**
     * 블록 코멘트: /* ~ * /<br>
     * 라인 코멘트: // ~ \n
     */
    private CommentType commentType = CommentType.Wait;
    private boolean isEnd = false;

    public CommentBuffer() {}

    public ParsingState start(ParsingState lastState) {
        changeLastParsingState(lastState);
        if(commentBuffer == null) {
            commentBuffer = new CharacterBuffer();
        }
        commentType = CommentType.Wait;
        isEnd = false;
        commentBuffer.reset();
        return ParsingState.Comment;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public CommentState commentParsingState() {
        return commentParsingState;
    }

    public ParsingState lastParsingState() {
        return lastState;
    }

    void changeLastParsingState(ParsingState lastState) {
        this.lastState = lastState;
        switch (lastState) {
            case WaitKey:
                commentParsingState = CommentState.BeforeKey;
                break;
            case WaitKeyEndSeparator:
                commentParsingState = CommentState.AfterKey;
                break;
            case WaitValue:
                commentParsingState = CommentState.BeforeValue;
                break;
            case WaitNextStoreSeparatorInArray:
            case WaitNextStoreSeparatorInObject:
                commentParsingState = CommentState.AfterValue;
                break;
        }
    }

    AppendResult append(char c) {
        switch (commentType) {
            case Wait:
                if(c == '/') {
                    commentType = CommentType.Line;
                    return AppendResult.InComment;
                } else if(c == '*') {
                    commentType = CommentType.Block;
                    return AppendResult.InComment;
                } else {
                    return AppendResult.Fail;
                }
            case Line:
                if(c == '\n') {
                    isEnd = true;
                    return AppendResult.End;
                } else {
                    commentBuffer.append(c);
                }
                break;
            case Block:
                if(c == '/') {
                    int length = commentBuffer.length();
                    if(length > 0 && commentBuffer.charAt(length - 1) == '*') {
                        commentBuffer.setLength(length - 1);
                        isEnd = true;
                        return AppendResult.End;
                    }
                    break;
                }
                commentBuffer.append(c);
                break;

        }
        return AppendResult.Continue;
    }

    public String getComment() {
        return commentBuffer.toString();
    }

    @Override
    public String toString() {
        if(commentBuffer == null) {
            return "";
        }
        return commentBuffer.toString();
    }






}
