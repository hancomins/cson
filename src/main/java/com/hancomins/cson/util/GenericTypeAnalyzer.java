package com.hancomins.cson.util;

import java.lang.reflect.*;
import java.util.*;


public class GenericTypeAnalyzer {

    // 분석 결과를 추출하는 공통 메서드 (재귀적 처리)
    private static List<GenericTypes> extractGenericTypes(Type type) {
        List<GenericTypes> result = new ArrayList<>();
        GenericTypes genericTypes = null;
        Class<?> nestClass = null;
        if (type instanceof ParameterizedType) {

            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                result.add((Class<?>) rawType);
            }
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                result.addAll(extractGenericTypes(actualTypeArgument));
            }
        } else if (type instanceof Class<?>) {
            result.add((Class<?>) type);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            result.addAll(extractGenericTypes(genericArrayType.getGenericComponentType()));
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            for (Type bound : typeVariable.getBounds()) {
                result.addAll(extractGenericTypes(bound));
            }
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            for (Type upperBound : wildcardType.getUpperBounds()) {
                result.addAll(extractGenericTypes(upperBound));
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                result.addAll(extractGenericTypes(lowerBound));
            }
        }

        return result;
    }

    /**
     * 주어진 Field의 제네릭 타입을 분석합니다.
     * @param field 분석할 Field 객체
     * @return 제네릭 타입에 관련된 클래스 목록
     */
    public static List<GenericTypes> analyzeField(Field field) {
        if (field == null) {
            return Collections.emptyList();
        }
        Type genericType = field.getGenericType();
        return extractGenericTypes(genericType);
    }

    /**
     * 주어진 Method의 반환 타입을 분석합니다.
     * @param method 분석할 Method 객체
     * @return 제네릭 타입에 관련된 클래스 목록
     */
    public static List<GenericTypes> analyzeReturnType(Method method) {
        if (method == null) {
            return Collections.emptyList();
        }
        Type returnType = method.getGenericReturnType();
        return extractGenericTypes(returnType);
    }

    /**
     * 주어진 Parameter의 제네릭 타입을 분석합니다.
     * @param parameter 분석할 Parameter 객체
     * @return 제네릭 타입에 관련된 클래스 목록
     */
    public static List<GenericTypes> analyzeParameter(Parameter parameter) {
        if (parameter == null) {
            return Collections.emptyList();
        }
        Type parameterType = parameter.getParameterizedType();
        return extractGenericTypes(parameterType);
    }


    public static class GenericTypes {
        public static byte NEST_TYPE_NORMAL = 0;
        public static byte NEST_TYPE_COLLECTION = 2;
        public static byte NEST_TYPE_MAP = 1;

        private final Class<?> nestClass;
        private List<Class<?>> types;
        private byte nestType = NEST_TYPE_NORMAL;
        private boolean isRawOrEmpty = false;

        GenericTypes(Class<?> nestClass, List<Class<?>> types) {
            this.types = types;
            this.nestClass  = nestClass;
            isRawOrEmpty = types.isEmpty();
            if(Collection.class.isAssignableFrom(nestClass)) {
                nestType = NEST_TYPE_COLLECTION;
            } else if(Map.class.isAssignableFrom(nestClass)) {
                nestType = NEST_TYPE_MAP;
            }
        }

        public byte getNestType() {
            return nestType;
        }

        public Class<?> getNestClass() {
            return nestClass;
        }

        public Class<?> getKeyType() {
            if(isRawOrEmpty) return null;
            return types.get(0);
        }

        public Class<?> getValueType() {
            if(isRawOrEmpty) return null;
            return types.get(types.size() - 1);
        }


        public GenericTypes setTypes(List<Class<?>> types) {
            this.types = types;
            return this;
        }
    }

}
