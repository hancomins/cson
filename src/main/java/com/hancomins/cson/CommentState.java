package com.hancomins.cson;

public enum CommentState {
    None,
    Header,
    Tail,
    BeforeKey,
    AfterKey,
    BeforeValue,
    AfterValue
}
