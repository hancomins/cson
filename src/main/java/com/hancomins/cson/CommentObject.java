package com.hancomins.cson;

public class CommentObject {
    private String beforeComment;
    private String afterComment;

    public CommentObject() {
    }

    public CommentObject(String beforeComment, String afterComment) {
        this.beforeComment = beforeComment;
        this.afterComment = afterComment;
    }

    public String getBeforeComment() {
        return beforeComment;
    }

    public void setHeadComment(String beforeComment) {
        this.beforeComment = beforeComment;
    }

    public String getAfterComment() {
        return afterComment;
    }

    public void setTailComment(String afterComment) {
        this.afterComment = afterComment;
    }

    void appendBeforeComment(String comment) {
        if(beforeComment == null) {
            beforeComment = comment;
        } else {
            beforeComment += "\n" + comment;
        }
    }

    void appendAfterComment(String comment) {
        if(afterComment == null) {
            afterComment = comment;
        } else {
            afterComment += "\n" + comment;
        }
    }


    public String getComment() {
        if(beforeComment == null && afterComment == null) {
            return null;
        } else if(beforeComment == null) {
            return afterComment;
        } else if(afterComment == null) {
            return beforeComment;
        }
        return beforeComment + "\n" + afterComment;
    }

    public String toString() {
        return getComment();
    }


    public boolean isCommented() {
        return beforeComment != null || afterComment != null;
    }


    public CommentObject copy() {
        CommentObject commentObject = new CommentObject();
        commentObject.beforeComment =  beforeComment;
        commentObject.afterComment = afterComment;
        return commentObject;
    }

}
