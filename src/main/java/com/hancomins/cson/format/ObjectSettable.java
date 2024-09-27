package com.hancomins.cson.format;

public interface ObjectSettable {
    void set(String key, Object value);
    void setCommentForKey(String key, String comment);
    void setCommentForValue(String key,String comment);
    void setCommentAfterValue(String key,String comment);
    void setCommentAfterKey(String key,String comment);
    String getLastKey();
}
