package com.clipsoft.cson.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSON {
    // 0.9.28
    boolean explicit() default false;
    String comment() default "";
    String commentAfter() default "";


}
