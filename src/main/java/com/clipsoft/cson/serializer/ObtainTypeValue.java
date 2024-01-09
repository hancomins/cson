package com.clipsoft.cson.serializer;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObtainTypeValue {

    String value() default "";
    String[] fieldNames() default  {};

    boolean ignoreError() default false;
}
