package com.hancomins.cson.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@SuppressWarnings("unchecked")
public class GenericTypeAnalyzerTest {

    // Inner 테스트 클래스
    public static class TestClass {
        // 필드 정의
        public List<String> stringList;
        public Map<String, Integer> stringIntegerMap;
        public List<Map<String, List<Double>>> nestedGeneric;

        @SuppressWarnings("rawtypes")
        public List rawList;

        // 메서드 정의
        public List<String> getStringList() {
            return null;
        }

        public void setStringIntegerMap(Map<String, List<Integer>> map) {
        }

        public <T extends Number> void processGenericList(List<Collection<Set<T>>> list) {
        }


    }




    @Test
    public void testAnalyzeFieldForFields() throws NoSuchFieldException {
        // 필드 가져오기
        Field stringListField = TestClass.class.getDeclaredField("stringList");
        Field stringIntegerMapField = TestClass.class.getDeclaredField("stringIntegerMap");
        Field nestedGenericField = TestClass.class.getDeclaredField("nestedGeneric");

        // 필드 분석 및 검증
        assertEquals(
                listOf(List.class, String.class),
                GenericTypeAnalyzer.analyzeField(stringListField),
                "Field 'stringList' generic types do not match."
        );

        assertEquals(
                listOf(Map.class, String.class, Integer.class),
                GenericTypeAnalyzer.analyzeField(stringIntegerMapField),
                "Field 'stringIntegerMap' generic types do not match."
        );

        assertEquals(
                listOf(List.class, Map.class, String.class, List.class, Double.class),
                GenericTypeAnalyzer.analyzeField(nestedGenericField),
                "Field 'nestedGeneric' generic types do not match."
        );
    }

    @Test
    public void testAnalyzeReturnType() throws NoSuchMethodException {
        // 메서드 가져오기
        Method getStringListMethod = TestClass.class.getDeclaredMethod("getStringList");

        // 메서드 리턴 타입 분석 및 검증
        assertEquals(

                listOf(List.class, String.class),
                GenericTypeAnalyzer.analyzeReturnType(getStringListMethod),
                "Return type of 'getStringList' generic types do not match."
        );
    }

    @Test
    public void testAnalyzeParameter() throws NoSuchMethodException {
        // 파라미터가 있는 메서드 가져오기
        Method setStringIntegerMapMethod = TestClass.class.getDeclaredMethod("setStringIntegerMap", Map.class);
        Method processGenericListMethod = TestClass.class.getDeclaredMethod("processGenericList", List.class);

        // 파라미터 분석
        Parameter mapParameter = setStringIntegerMapMethod.getParameters()[0];
        Parameter genericParameter = processGenericListMethod.getParameters()[0];

        // 검증
        assertEquals(
                listOf(Map.class, String.class,List.class, Integer.class),
                GenericTypeAnalyzer.analyzeParameter(mapParameter),
                "Parameter type of 'setStringIntegerMap' generic types do not match."
        );

        assertEquals(
                listOf(List.class,Collection.class,Set.class, Number.class),
                GenericTypeAnalyzer.analyzeParameter(genericParameter),
                "Parameter type of 'processGenericList' generic types do not match."
        );
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }


}