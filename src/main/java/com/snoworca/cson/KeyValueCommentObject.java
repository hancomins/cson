package com.snoworca.cson;

class KeyValueCommentObject {
    CommentObject keyCommentObject;
    CommentObject valueCommentObject;

    @Override
    public String toString() {
        if(keyCommentObject == null && valueCommentObject == null) {
            return "";
        }
        else if(keyCommentObject == null) {
            return valueCommentObject.toString();
        }
        else if(valueCommentObject == null) {
            return keyCommentObject.toString();
        }
        else {
            return  keyCommentObject + "\n" + valueCommentObject;
        }
    }
}
