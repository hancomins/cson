package com.clipsoft.cson.serializer;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface CSONValidation {
    /**
     * null 허용 여부<br>
     * 기본값은 false
     * @return null 허용 여부
     */
    boolean notNull() default false;

    /**
     * 필수 여부<br>
     * 기본값은 false
     * @return 필수 여부
     */
    boolean required() default false;

    /**
     * Number 타입일 경우 최소값<br>
     * String 타입일 경우 최소 길이<br>
     * 만약, -1 이면 체크하지 않음. 기본값은 -1
     * @return 최소값.
     */
    double min() default -1;

    /**
     * Number 타입일 경우 최대값<br>
     * String 타입일 경우 최대 길이<br>
     * 만약, -1 이면 체크하지 않음. 기본값은 -1
     * @return 최대값.
     */
    double max() default -1;

    /**
     * 정규식 패턴<br>
     * 기본값은 "" 이며, 정규식 패턴을 체크하지 않음.<br>
     * 문자열이 아닌 경우 toString() 으로 문자열로 변환하여 체크한다.
     * @return 정규식 패턴
     */
    String pattern() default "";
    /**
     * 유효성 검사 실패 시 반환할 메시지<br>
     * 유효성 검사 실패시 IllegalArgumentException 를 던지며 메시지로 반환한다.<br>
     * 기본값은 "Invalid value"
     * @return 유효성 검사 실패 메시지.
     */
    String message() default "Invalid value";

    /**
     * 타입 체크 여부<br>
     * 타입이 일치하지 않으면 IllegalArgumentException 를 던진다.<br>
     * 기본값은 true
     * @return 타입 체크 여부
     */
    boolean typeMatch() default true;
}