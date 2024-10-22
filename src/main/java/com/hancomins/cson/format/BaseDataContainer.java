package com.hancomins.cson.format;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;


public interface BaseDataContainer  {
    int size();
    void setSourceFormat(FormatType formatType);
    void setComment(String comment, CommentPosition commentPosition);
    void setComment(CommentObject<?> commentObject);
    String getComment(CommentPosition commentPosition);
    DataIterator<?> iterator();

}
