package com.hancomins.cson.serializer;

import com.hancomins.cson.CSONArray;
import com.hancomins.cson.CSONElement;
import com.hancomins.cson.CSONException;
import com.hancomins.cson.CSONObject;
import com.hancomins.cson.options.WritingOptions;
import com.hancomins.cson.util.DataConverter;


import java.util.*;

public class CSONSerializer {

    private CSONSerializer() {}

    public static boolean serializable(Class<?> clazz) {
        return false;
    }

    public static CSONObject toCSONObject(Object obj) {
        return null;
    }


    public static CSONObject mapToCSONObject(Map<String, ?> map) {
        return null;
    }

    public static CSONArray collectionToCSONArray(Collection<?> collection) {
        return null;
    }



    public static <T> List<T> csonArrayToList(CSONArray csonArray, Class<T> valueType) {
        return null;
    }

    public static <T> List<T> csonArrayToList(CSONArray csonArray, Class<T> valueType, boolean ignoreError) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> csonArrayToList(CSONArray csonArray, Class<T> valueType, WritingOptions<?> writingOptions, boolean ignoreError, T defaultValue) {
        return null;
    }

    /**
     * CSONObject 를 Map<String, T> 로 변환한다.
     * @param csonObject 변환할 CSONObject
     * @param valueType Map 의 value 타입 클래스
     * @return 변환된 Map
     * @param <T> Map 의 value 타입
     */
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> Map<String, T> fromCSONObjectToMap(CSONObject csonObject, Class<T> valueType) {
        return null;
    }



    @SuppressWarnings("unchecked")
    public static<T> T fromCSONObject(CSONObject csonObject, Class<T> clazz) {
        return null;
    }



    public static<T> T fromCSONObject(final CSONObject csonObject, T targetObject) {
        return null;
    }
}
