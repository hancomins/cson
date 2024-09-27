package com.hancomins.cson.format.json;

enum CommentParsingState {
    None,
    Header,
    Tail,
    BeforeKey,
    AfterKey,
    BeforeValue,
    AfterValue
}
