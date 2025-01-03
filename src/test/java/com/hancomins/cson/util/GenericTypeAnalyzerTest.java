package com.hancomins.cson.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class GenericTypeAnalyzerTest {

    // Inner 테스트 클래스
    public static class TestClass {
        // 필드 정의
        public List<String> stringList;
        public Map<String, Integer> stringIntegerMap;
        public List<Map<String, List<Double>>> nestedGeneric;
        public AtomicReference<AtomicReference<AtomicReference<String>>> atomicReference;
        public List<? extends List<String>> wildcardList;

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
    public void testAnalyzeFieldForNonGenericFields() throws NoSuchFieldException {
        // 필드 가져오기
        Field wildcardListField = TestClass.class.getDeclaredField("wildcardList");
        Field rawListField = TestClass.class.getDeclaredField("rawList");

        List<GenericTypeAnalyzer.GenericTypes> genericTypes = GenericTypeAnalyzer.analyzeField(wildcardListField);
        // 필드 분석 및 검증
        assertEquals(
                List.class,
                genericTypes.get(0).getNestClass()
        );


        assertEquals(
                listOf(List.class, String.class),
                genericTypes.stream().flatMap(it -> it.getTypes().stream()).collect(Collectors.toList())
        );



        try {
            genericTypes = GenericTypeAnalyzer.analyzeField(rawListField);
            fail();
        } catch (IllegalArgumentException e) {

        }

    }




    @SuppressWarnings("unchecked")
    @Test
    public void testAnalyzeFieldForFields() throws NoSuchFieldException {
        // 필드 가져오기
        Field stringListField = TestClass.class.getDeclaredField("stringList");
        Field stringIntegerMapField = TestClass.class.getDeclaredField("stringIntegerMap");
        Field nestedGenericField = TestClass.class.getDeclaredField("nestedGeneric");
        Field atomicReferenceField = TestClass.class.getDeclaredField("atomicReference");

        List<GenericTypeAnalyzer.GenericTypes> genericTypes;

        // 필드 분석 및 검증
        assertEquals(
                listOf(String.class),
                GenericTypeAnalyzer.analyzeField(stringListField).stream().map(GenericTypeAnalyzer.GenericTypes::getValueType).collect(Collectors.toList()),
                "Field 'stringList' generic types do not match."
        );


        assertEquals(List.class, GenericTypeAnalyzer.analyzeField(stringListField).get(0).getNestClass());

        assertEquals(
                listOf(String.class, Integer.class),
                GenericTypeAnalyzer.analyzeField(stringIntegerMapField).stream().flatMap(it -> it.getTypes().stream()).collect(Collectors.toList()),
                "Field 'stringIntegerMap' generic types do not match."
        );

        assertEquals(Map.class, GenericTypeAnalyzer.analyzeField(stringIntegerMapField).get(0).getNestClass());


        genericTypes = GenericTypeAnalyzer.analyzeField(nestedGenericField);
        assertEquals(
                List.class
                ,genericTypes.get(0).getNestClass()
        );

        assertEquals(
                Map.class,
                genericTypes.get(1).getNestClass()
        );

        assertEquals(
                List.class
                ,genericTypes.get(0).getNestClass()
        );



        assertEquals(
                listOf(Map.class, String.class, List.class, Double.class),
                GenericTypeAnalyzer.analyzeField(nestedGenericField).stream().flatMap(it -> it.getTypes().stream()).collect(Collectors.toList()),
                "Field 'nestedGeneric' generic types do not match."
        );

        genericTypes = GenericTypeAnalyzer.analyzeField(atomicReferenceField);
        assertEquals(
                AtomicReference.class,
                genericTypes.get(0).getNestClass()
        );

        assertEquals(listOf(AtomicReference.class,AtomicReference.class,String.class),genericTypes.stream().flatMap(it -> it.getTypes().stream()).collect(Collectors.toList()));
    }

    @Test
    public void testAnalyzeReturnType() throws NoSuchMethodException {
        // 메서드 가져오기
        Method getStringListMethod = TestClass.class.getDeclaredMethod("getStringList");

        assertEquals(List.class, GenericTypeAnalyzer.analyzeReturnType(getStringListMethod).get(0).getNestClass());

        // 메서드 리턴 타입 분석 및 검증
        assertEquals(

                listOf(String.class),
                GenericTypeAnalyzer.analyzeReturnType(getStringListMethod).get(0).getTypes(),
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

        List<GenericTypeAnalyzer.GenericTypes> genericTypes = GenericTypeAnalyzer.analyzeParameter(mapParameter);

        assertEquals(Map.class, genericTypes.get(0).getNestClass());


        assertEquals(
                listOf(String.class, List.class, Integer.class),
                genericTypes.stream().flatMap(it -> it.getTypes().stream()).collect(Collectors.toList()),
                "Parameter type of 'setStringIntegerMap' generic types do not match."
        );

        genericTypes = GenericTypeAnalyzer.analyzeParameter(genericParameter);
        assertEquals(List.class, genericTypes.get(0).getNestClass());


        assertEquals(
                listOf(Collection.class,Set.class, Number.class),
                genericTypes.stream().flatMap(it -> it.getTypes().stream()).collect(Collectors.toList()),
                "Parameter type of 'processGenericList' generic types do not match."
        );
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listOf(T... values) {
        return new ArrayList<>(Arrays.asList(values));
    }


}