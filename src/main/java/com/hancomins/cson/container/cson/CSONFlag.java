package com.hancomins.cson.container.cson;

/**
 * 이 클래스는 CSON 구조의 각 코드 값에 해당하는 상수 플래그들과
 * 데이터 타입 검사에 필요한 상수들을 정의한 클래스입니다.
 * 상수 값들은 고정값, 정수형, 실수형, 문자열, 객체, 배열, 코멘트 등 다양한 데이터 타입을 나타냅니다.
 * 또한 각 데이터 타입을 구분하기 위한 플래그를 추가로 제공합니다.
 */
class CSONFlag {


    static final short CSON_VERSION = 100;  // CSON 버전
    static final int CSON_HEADER = 'C' << 24 | 'S' << 16 | 'O' << 8 | 'N';  // CSON 헤더

    // 바이트 플래그
    static short ENABLE_COMMENT = 1;  // 코멘트 활성화
    static short ENABLE_STRING_TABLE = 2;  // 문자열 맵 활성화

    // 고정값
    static final int NULL = 0x10;
    static final int EMPTY = 0x11;
    static final int TRUE = 0x12;
    static final int FALSE = 0x13;
    static final int NAN = 0x14;
    static final int INFINITY = 0x15;
    static final int NEGATIVE_INFINITY = 0x16;
    static final int STRING_MAP = 0x17;
    static final int CSON_FOOTER = 0x18;




    // 정수형
    static final int BIG_INT = 0x20;
    static final int INT8 = 0x21;
    static final int INT16 = 0x22;
    static final int INT32 = 0x23;
    static final int INT64 = 0x24;
    static final int INT_CHAR = 0x25;

    // 실수형
    static final int BIG_DEC = 0x30;
    static final int DEC32 = 0x31;
    static final int DEC64 = 0x32;

    // 16 바이트 이하의 문자열
    static final int STRING_LESS_THAN_16 = 0x40;
    // 길이 헤더가 포함된 문자열
    static final int STRING_UINT8 = 0x50;   // ~uint8 길이 문자열 (최대 254)
    static final int STRING_UINT16 = 0x51;  // ~uint16 길이 문자열 (최대 65534)
    static final int STRING_UINT32 = 0x52;  // uint32 길이 문자열
    // 길이 헤더가 포함된 바이트 버퍼
    static final int BYTE_BUFFER_UINT8 = 0x53;   // ~uint8 길이 바이트 버퍼
    static final int BYTE_BUFFER_UINT16 = 0x54;  // ~uint16 길이 바이트 버퍼
    static final int BYTE_BUFFER_UINT32 = 0x55;  // uint32 길이 바이트 버퍼

    // 16개 이하의 오브젝트
    static final int OBJECT_LESS_THAN_16 = 0x70;
    // 길이가 포함된 오브젝트
    static final int OBJECT_UINT8 =  0x90;   // ~uint8 길이 오브젝트
    static final int OBJECT_UINT16 =  0x91;  // ~uint16 길이 오브젝트
    static final int OBJECT_UINT32 =  0x92;  // uint32 길이 오브젝트

    // 16개 이하의 배열
    static final int ARRAY_LESS_THAN_16 =  0x80;
    // 길이 헤더가 포함된 배열
    static final int ARRAY_UINT8 =  0x93;   // ~uint8 길이 배열
    static final int ARRAY_UINT16 =  0x94;  // ~uint16 길이 배열
    static final int ARRAY_UINT32 =  0x95;  // uint32 길이 배열

    // 코멘트
    static final int HEADER_COMMENT =  0xa0; // 헤더 코멘트
    static final int FOOTER_COMMENT =  0xa1; // 푸터 코멘트ㅊ
    static final int COMMENT_ZERO =  0xa2;  // 코멘트 없음
    static final int COMMENT_UINT8 =  0xa3;   // ~uint8 길이 오브젝트 코멘트
    static final int COMMENT_UINT16 =  0xa4;  // ~uint16 길이 오브젝트 코멘트
    static final int COMMENT_UINT32 =  0xa5;  // uint32 길이 오브젝트 코멘트



    // 타입 플래그
    static final int TYPE_FIXED_VALUE = 0x1;  // 고정값
    static final int TYPE_INTEGER = 0x2;      // 정수형
    static final int TYPE_FLOAT = 0x3;        // 실수형
    static final int TYPE_STRING_LESS_THAN_16 = 0x4;       // 문자열
    static final int TYPE_STRING_OR_BYTE_BUFFER = 0x5;  // 바이트 버퍼
    static final int TYPE_OBJECT_LESS_THAN_16 = 0x7;       // 오브젝트
    static final int TYPE_ARRAY_LESS_THAN_16 = 0x8;        // 배열
    static final int TYPE_OBJECT =  0x9; // 큰 오브젝트
    static final int TYPE_ARRAY =  0x9;  // 큰 배열
    static final int TYPE_COMMENT =  0xa;      // 코멘트


    static final byte COMMENT_TYPE_BEFORE_KEY = 1;  // 키 앞에 코멘트
    static final byte COMMENT_TYPE_AFTER_KEY = 2;   //
    static final byte COMMENT_TYPE_BEFORE_VALUE = 4;   //
    static final byte COMMENT_TYPE_AFTER_VALUE = 8;   //




    // private 생성자
    private CSONFlag() {
        // 인스턴스화 방지
    }
}
