package com.hancomins.cson.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSONValue {

    boolean ignore() default false;
    String value() default "";

    String key() default "";
    String comment() default "";
    String commentAfterKey() default "";


}