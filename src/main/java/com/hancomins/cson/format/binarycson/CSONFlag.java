package com.hancomins.cson.format.binarycson;

/**
 * 이 클래스는 CSON 구조의 각 코드 값에 해당하는 상수 플래그들과
 * 데이터 타입 검사에 필요한 상수들을 정의한 클래스입니다.
 * 상수 값들은 고정값, 정수형, 실수형, 문자열, 객체, 배열, 코멘트 등 다양한 데이터 타입을 나타냅니다.
 * 또한 각 데이터 타입을 구분하기 위한 플래그를 추가로 제공합니다.
 */
class CSONFlags {

    // 고정값
    static final byte NULL = 0x10;
    static final byte EMPTY = 0x11;
    static final byte TRUE = 0x12;
    static final byte FALSE = 0x13;
    static final byte NAN = 0x14;
    static final byte INFINITY = 0x15;
    static final byte NEGATIVE_INFINITY = 0x16;
    static final byte COMMENT = 0x17;
    static final byte STRING_MAP_START = 0x1a;

    // 정수형
    static final byte BIG_INT = 0x20;
    static final byte INT8 = 0x21;
    static final byte INT16 = 0x22;
    static final byte INT32 = 0x23;
    static final byte INT64 = 0x24;

    // 실수형
    static final byte BIG_DEC = 0x30;
    static final byte FLOAT32 = 0x31;
    static final byte DOUBLE64 = 0x32;

    // 문자열
    static final byte STRING_LESS_THAN_16 = 0x40;
    static final byte STRING_UINT8 = 0x50;   // ~uint8 길이 문자열 (최대 254)
    static final byte STRING_UINT16 = 0x51;  // ~uint16 길이 문자열 (최대 65534)
    static final byte STRING_UINT32 = 0x52;  // uint32 길이 문자열
    static final byte BYTE_BUFFER_UINT8 = 0x53;   // ~uint8 길이 바이트 버퍼
    static final byte BYTE_BUFFER_UINT16 = 0x54;  // ~uint16 길이 바이트 버퍼
    static final byte BYTE_BUFFER_UINT32 = 0x55;  // uint32 길이 바이트 버퍼

    // Object
    static final byte OBJECT_LESS_THAN_16 = 0x70;
    static final byte OBJECT_UINT8 = (byte) 0x90;   // ~uint8 길이 오브젝트
    static final byte OBJECT_UINT16 = (byte) 0x91;  // ~uint16 길이 오브젝트
    static final byte OBJECT_UINT32 = (byte) 0x92;  // uint32 길이 오브젝트

    // Array
    static final byte ARRAY_LESS_THAN_16 = (byte) 0x80;
    static final byte ARRAY_UINT8 = (byte) 0x93;   // ~uint8 길이 배열
    static final byte ARRAY_UINT16 = (byte) 0x94;  // ~uint16 길이 배열
    static final byte ARRAY_UINT32 = (byte) 0x95;  // uint32 길이 배열

    // 코멘트
    static final byte HEADER_COMMENT = (byte) 0xa1;
    static final byte TAIL_COMMENT = (byte) 0xa2;
    static final byte OBJECT_COMMENT_UINT8 = (byte) 0xa3;   // ~uint8 길이 오브젝트 코멘트
    static final byte OBJECT_COMMENT_UINT16 = (byte) 0xa4;  // ~uint16 길이 오브젝트 코멘트
    static final byte OBJECT_COMMENT_UINT32 = (byte) 0xa5;  // uint32 길이 오브젝트 코멘트
    static final byte ARRAY_COMMENT_UINT8 = (byte) 0xa6;    // ~uint8 길이 배열 코멘트
    static final byte ARRAY_COMMENT_UINT16 = (byte) 0xa7;   // ~uint16 길이 배열 코멘트
    static final byte ARRAY_COMMENT_UINT32 = (byte) 0xa8;   // uint32 길이 배열 코멘트

    // 타입 플래그 (0xf0 & 연산자로 타입 구분)
    static final byte TYPE_FIXED_VALUE = 0x10;  // 고정값
    static final byte TYPE_INTEGER = 0x20;      // 정수형
    static final byte TYPE_FLOAT = 0x30;        // 실수형
    static final byte TYPE_STRING_SMALL = 0x40;       // 문자열
    static final byte TYPE_BYTE_BUFFER = 0x50;  // 바이트 버퍼
    static final byte TYPE_OBJECT = 0x70;       // 오브젝트
    static final byte TYPE_ARRAY = (byte)0x80;        // 배열
    static final byte TYPE_OBJECT_LARGE = (byte) 0x90; // 큰 오브젝트
    static final byte TYPE_ARRAY_LARGE = (byte) 0x93;  // 큰 배열
    static final byte TYPE_COMMENT = (byte) 0xa0;      // 코멘트

    // private 생성자
    private CSONFlags() {
        // 인스턴스화 방지
    }
}

