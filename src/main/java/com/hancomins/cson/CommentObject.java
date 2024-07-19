package com.hancomins.cson;

public class CommentObject implements  Cloneable {
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

    public void setBeforeComment(String beforeComment) {
        this.beforeComment = beforeComment;
    }

    public String getAfterComment() {
        return afterComment;
    }

    public void setAfterComment(String afterComment) {
        this.afterComment = afterComment;
    }


    public String getComment() {
        if(beforeComment == null && afterComment == null) {
            return null;
        } else if(beforeComment == null) {
            return afterComment;
        } else if(afterComment == null) {
            return beforeComment;
        } else {
            return beforeComment + "\n" + afterComment;
        }
    }

    public String toString() {
        return getComment();
    }


    public boolean isCommented() {
        return beforeComment != null || afterComment != null;
    }


    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public CommentObject clone() {
        CommentObject commentObject = new CommentObject();
        commentObject.beforeComment =  beforeComment;
        commentObject.afterComment = afterComment;
        return commentObject;
    }

}
