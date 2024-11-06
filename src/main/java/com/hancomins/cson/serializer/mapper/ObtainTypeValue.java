package com.hancomins.cson.serializer.mapper;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObtainTypeValue {

    String[] fieldNames() default  {};
    String[] setterMethodNames() default  {};

    boolean ignoreError() default false;

    boolean deserializeAfter() default true;


}
