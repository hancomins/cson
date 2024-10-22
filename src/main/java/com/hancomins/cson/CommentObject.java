package com.hancomins.cson;

import java.util.EnumMap;

public class CommentObject<T> {


    private final EnumMap<CommentPosition, String> commentPositionMap = new EnumMap<>(CommentPosition.class);
    private final CommentPosition defaultCommentPosition;
    private final T index;


    static CommentObject<?> forRootElement() {
        return new CommentObject<>(CommentPosition.HEADER, null);
    }

    public static CommentObject<String> forKeyValueContainer(String index) {
        return new CommentObject<>(CommentPosition.BEFORE_KEY, index);
    }

    public static CommentObject<Integer> forArrayContainer(Integer index) {
        return new CommentObject<>(CommentPosition.BEFORE_VALUE, index);
    }

    public T getIndex() {
        return index;
    }

    CommentObject(CommentPosition defaultCommentPosition,T index) {
        this.defaultCommentPosition = defaultCommentPosition;
        this.index = index;
    }


    public boolean hasComment(CommentPosition commentPosition) {
        if(commentPosition == CommentPosition.DEFAULT || commentPosition == null) {
            commentPosition = defaultCommentPosition;
        }
        return commentPositionMap.containsKey(commentPosition);
    }


    public CommentObject<T> setComment(CommentPosition commentPosition, String value) {
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

    public CommentObject<T> appendComment(CommentPosition commentPosition, String value) {
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


    public CommentObject<T> copy() {
        CommentObject<T> commentObject = new CommentObject<>(defaultCommentPosition , index);
        commentObject.commentPositionMap.putAll(commentPositionMap);
        return commentObject;
    }




}
