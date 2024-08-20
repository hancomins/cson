package com.hancomins.cson;

class KeyValueCommentObject {
    private CommentObject keyCommentObject;
    private CommentObject valueCommentObject;

    public boolean isNullOrEmptyKeyCommentObject() {
        return keyCommentObject == null || keyCommentObject.getComment() == null;
    }

    public boolean isNullOrEmptyValueCommentObject() {
        return valueCommentObject == null || valueCommentObject.getComment() == null;
    }

    void setKeyCommentObject(CommentObject keyCommentObject) {
        this.keyCommentObject = keyCommentObject == null ? null : keyCommentObject.copy();
    }

    CommentObject getKeyCommentObject() {
        return keyCommentObject;
    }

    void setValueCommentObject(CommentObject valueCommentObject) {
        this.valueCommentObject = valueCommentObject == null ? null:  valueCommentObject.copy();
    }

    CommentObject getValueCommentObject() {
        return valueCommentObject;
    }

    @Override
    public String toString() {
       if(keyCommentObject == null && valueCommentObject == null) {
            return "";
        }
        else if(keyCommentObject == null ) {
            return valueCommentObject.toString();
        }
        else if(valueCommentObject == null) {
            return keyCommentObject.toString();
        }

        return  keyCommentObject + "\n" + valueCommentObject;

    }
}
