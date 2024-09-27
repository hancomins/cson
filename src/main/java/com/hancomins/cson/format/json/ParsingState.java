package com.hancomins.cson.format.json;

public enum ParsingState {
    /**
     * 시작 상태: 아직 { 또는 [ 가 나오지 않은 상태
     */
    Open,
    /**
     * JSON 이 닫힌 상태. 이 상태에서는 주석만 파싱할 수 있다.
     */
    Close,
    /**
     * 문자열을 읽는 상태. WaitValue 이후로 " 또는 ' 가 나온 상태이다.
     */
    String,
    /**
     * 값을 파싱하는 상태. WaitKey 이후로 숫자 혹은 문자가 나온 상태이다.
     * '\n', '}', ']', ',' 이 나오기 전까지 값이 어떤 타입인지 알 수 없다.
     */
    Value,
    /**
     * 값을 기다리는 상태. Object 에서  ':' 이 나오거나 Array 에서 ',' 이후에 이 상태로 전환된다.
     */
    WaitValue,
    /**
     * 키 이후에 ':' 이 나온 상태. 이 상태에서는 값이 나와야 한다.
     * ' 또는 " 로 시작하고 끝나는 문자열에만 해당한다.
     * 키 값에 " 또는 ' 가 없다면, 또 이경우 키 마지막에 \n 이 없다면 이 상태 모드는 만날 수 없다.
     */
    WaitKeyEndSeparator,
    /**
     * 다음 키 또는 값을 기다리는 상태. ',' 이 나오면 이 상태로 전환된다.
     * 또는 root element 가 아닌 상황에서 또는 '}' 또는 ']' 이 나오면 이 상태로 전환된다.
     */
    WaitNextStoreSeparator, // , 가 나오기를 기다림
    /**
     * ' 또는 " 가 있는 키를 읽고 있는 상태.
     */
    InKey,
    /**
     * ' 또는 " 가 없는 키를 읽고 있는 상태. \n 또는 ':' 이 나오기 전까지 키를 읽는다.
     */
    InKeyUnquoted,
    /**
     * 키를 기다리는 상태. 이 상태에서 ' 또는 " 이 나오면 InKey 상태로 전환된다.
     * 만약, ' 또는 " 이 나오지 않고, 문자열이 나오면 InKeyUnquoted 상태로 전환된다.
     */
    WaitKey,
    /**
     * 주석을 처리하는 상태. 주석이 시작되면 이 상태로 전환된다.
     */
    InOpenComment,
    /**
     *
     */
    InCloseComment,



    //Number,

    InValueUnquoted,

    WaitNextStoreSeparatorInArray, // , 가 나오기를 기다림
    WaitNextStoreSeparatorInObject, // , 가 나오기를 기다림

    Comment,

}
