package com.hancomins.cson;

public enum CommentParsingState {
    None,
    Header,
    Tail,
    BeforeKey,
    AfterKey,
    BeforeValue,
    AfterValue
}
