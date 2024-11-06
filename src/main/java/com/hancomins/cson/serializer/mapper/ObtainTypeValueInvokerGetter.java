package com.hancomins.cson.serializer.mapper;

public interface ObtainTypeValueInvokerGetter {
    ObtainTypeValueInvoker getObtainTypeValueInvoker();


    /**
     * invoker 를 실행하여 값을 넣을 수 있는 Field 혹은 Method 의 전체 경로를 반환한다.
     * @return Field 혹은 Method 의 전체 경로. 예) "a.b.c.d()"
     */
    String targetPath();

    boolean isIgnoreError();

}
