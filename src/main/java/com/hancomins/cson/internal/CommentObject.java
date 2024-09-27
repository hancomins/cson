package com.hancomins.cson.internal;

public class CommentObject {
    private String leadingComment;
    private String trailingComment;

    public CommentObject() {
    }

    public CommentObject(String leadingComment, String trailingComment) {
        this.leadingComment = leadingComment;
        this.trailingComment = trailingComment;
    }

    public String getLeadingComment() {
        return leadingComment;
    }

    public void setLeadingComment(String leadingComment) {
        this.leadingComment = leadingComment;
    }

    public String getTrailingComment() {
        return trailingComment;
    }



    public void setTrailingComment(String trailingComment) {
        this.trailingComment = trailingComment;
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
