package com.hancomins.cson.format;

import com.hancomins.cson.CommentPosition;

public interface BaseDataContainer {
    void setSourceFormat(FormatType formatType);
    void setComment(String comment, CommentPosition commentPosition);
    String getComment(CommentPosition commentPosition);
}
