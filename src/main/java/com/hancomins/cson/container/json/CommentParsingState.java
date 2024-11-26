package com.hancomins.cson.container.json;

enum CommentParsingState {
    None,
    Header,
    Tail,
    BeforeKey,
    AfterKey,
    BeforeValue,
    AfterValue
}
