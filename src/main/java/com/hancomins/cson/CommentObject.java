package com.hancomins.cson;

import javax.xml.stream.events.Comment;
import java.util.EnumMap;

public class CommentObject {


    private final EnumMap<CommentPosition, String> commentPositionMap = new EnumMap<>(CommentPosition.class);
    private final CommentPosition defaultCommentPosition;



    static CommentObject forKeyValueContainer() {
        return new CommentObject(CommentPosition.BEFORE_KEY);
    }

    static CommentObject forArrayContainer() {
        return new CommentObject(CommentPosition.BEFORE_VALUE);
    }

    CommentObject(CommentPosition defaultCommentPosition) {
        this.defaultCommentPosition = defaultCommentPosition;
    }



    public CommentObject setComment(CommentPosition commentPosition, String value) {
        if(commentPosition == CommentPosition.DEFAULT || commentPosition == null) {
            commentPosition = defaultCommentPosition;
        }
        commentPositionMap.put(commentPosition, value);
        return this;
    }

    public String getComment(CommentPosition commentPosition) {
        if(commentPosition == CommentPosition.DEFAULT || commentPosition == null) {
            commentPosition = defaultCommentPosition;
        }
        return commentPositionMap.get(commentPosition);
    }

    public CommentObject appendComment(CommentPosition commentPosition, String value) {
        if(commentPosition == CommentPosition.DEFAULT || commentPosition == null) {
            commentPosition = defaultCommentPosition;
        }
        String comment = commentPositionMap.get(commentPosition);
        if(comment == null) {
            comment = value;
        } else {
            comment += "\n" + value;
        }
        commentPositionMap.put(commentPosition, comment);
        return this;
    }


    void appendLeadingComment(String comment) {
        if(leadingComment == null) {
            leadingComment = comment;
        } else {
            leadingComment += "\n" + comment;
        }
    }

    void appendTrailingComment(String comment) {
        if(trailingComment == null) {
            trailingComment = comment;
        } else {
            trailingComment += "\n" + comment;
        }
    }


    public String getComment() {
        if(leadingComment == null && trailingComment == null) {
            return null;
        } else if(leadingComment == null) {
            return trailingComment;
        } else if(trailingComment == null) {
            return leadingComment;
        }
        return leadingComment + "\n" + trailingComment;
    }

    public String toString() {
        return getComment();
    }


    public boolean isCommented() {
        return leadingComment != null || trailingComment != null;
    }


    public CommentObject copy() {
        CommentObject commentObject = new CommentObject();
        commentObject.leadingComment = leadingComment;
        commentObject.trailingComment = trailingComment;
        return commentObject;
    }

}
