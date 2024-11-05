package com.hancomins.cson.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSON {
    String comment() default "";
    String commentAfter() default "";
    boolean explicit() default false;

}
