package com.hancomins.cson;

import java.util.EnumMap;

public class CommentObject {


    private final EnumMap<CommentPosition, String> commentPositionMap = new EnumMap<>(CommentPosition.class);
    private final CommentPosition defaultCommentPosition;
    public static final CommentObject EMPTY_COMMENT_OBJECT = new CommentObject(CommentPosition.DEFAULT) {

        @Override
        public CommentObject setComment(CommentPosition commentPosition, String value) {
            return this;
        }

        public CommentObject appendComment(CommentPosition commentPosition, String value) {
            return this;
        }

        @Override
        public CommentObject copy() {
            return this;
        }
    };



    static CommentObject forRootElement() {
        return new CommentObject(CommentPosition.HEADER);
    }

    static CommentObject forKeyValueContainer() {
        return new CommentObject(CommentPosition.BEFORE_KEY);
    }

    static CommentObject forArrayContainer() {
        return new CommentObject(CommentPosition.BEFORE_VALUE);
    }

    CommentObject(CommentPosition defaultCommentPosition) {
        this.defaultCommentPosition = defaultCommentPosition;
    }


    public boolean hasComment(CommentPosition commentPosition) {
        if(commentPosition == CommentPosition.DEFAULT || commentPosition == null) {
            commentPosition = defaultCommentPosition;
        }
        return commentPositionMap.containsKey(commentPosition);
    }


    public CommentObject setComment(CommentPosition commentPosition, String value) {
        if(commentPosition == CommentPosition.DEFAULT || commentPosition == null) {
            commentPosition = defaultCommentPosition;
        }
        if(value == null) {
            commentPositionMap.remove(commentPosition);
            return this;
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
        if(value == null) {
            return this;
        }
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



    public String getComment() {
        StringBuilder comment = new StringBuilder();
        for (String value : commentPositionMap.values()) {
            if(value != null) {
                comment.append(value).append("\n");
            }
        }
        return comment.toString();
    }

    public String toString() {
        return getComment();
    }


    public boolean isCommented() {
        return !commentPositionMap.isEmpty();
    }


    public CommentObject copy() {
        CommentObject commentObject = new CommentObject(defaultCommentPosition);
        commentObject.commentPositionMap.putAll(commentPositionMap);
        return commentObject;
    }




}
