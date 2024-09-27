package com.hancomins.cson.internal.format.json;

enum CommentParsingState {
    None,
    Header,
    Tail,
    BeforeKey,
    AfterKey,
    BeforeValue,
    AfterValue
}
