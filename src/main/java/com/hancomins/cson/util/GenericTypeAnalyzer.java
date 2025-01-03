package com.hancomins.cson.util;

import java.lang.reflect.*;
import java.util.*;

public class GenericTypeAnalyzer {

    private static List<GenericTypes> extractGenericTypes(Type type) {
        ArrayDeque<GenericTypes> result = new ArrayDeque<>();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                List<Class<?>> types = new ArrayList<>();
                for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                    if (actualTypeArgument instanceof Class<?>) {
                        types.add((Class<?>) actualTypeArgument);
                    } else if(actualTypeArgument instanceof WildcardType) {
                        analyzeWildcardType((WildcardType) actualTypeArgument, types, result);
                    } else {
                        List<GenericTypes> subTypes = extractGenericTypes(actualTypeArgument);
                        result.addAll(subTypes);
                        types.add(subTypes.get(0).getNestClass());
                    }
                }
                result.addFirst(new GenericTypes((Class<?>) rawType, types));
            }
        }
        // todo GenericArrayType 과 TypeVariable 처리
        else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            result.addAll(extractGenericTypes(genericArrayType.getGenericComponentType()));
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            for (Type bound : typeVariable.getBounds()) {
                result.addAll(extractGenericTypes(bound));
            }
        }
        return new ArrayList<>(result);
    }


    private static void analyzeWildcardType(WildcardType wildcardType, List<Class<?>> values, ArrayDeque<GenericTypes> genericTypes) {
        Type[] upperBounds = wildcardType.getUpperBounds();
        Type[] lowerBounds = wildcardType.getLowerBounds();
        Type bound = upperBounds.length > 0 ? upperBounds[0] : lowerBounds[0];
        if (bound instanceof Class<?>) {
            values.add((Class<?>) bound);
        } else {
            List<GenericTypes> subTypes = extractGenericTypes(bound);
            values.add(subTypes.get(0).getNestClass());
            genericTypes.addAll(subTypes);
        }
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
        if(genericType instanceof Class<?>) {
            throw new IllegalArgumentException("Field type is Raw Type. (" + field.getDeclaringClass().getName() + "." + field.getName() + ")");
        }
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
        if(returnType instanceof Class<?>) {
            throw new IllegalArgumentException("Return type is Raw Type. (" + method.getDeclaringClass().getName() + "." + method.getName() + ")");
        }
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
        if(parameterType instanceof Class<?>) {
            throw new IllegalArgumentException("Parameter type is Raw Type. (" + parameter.getName() + ")");
        }
        return extractGenericTypes(parameterType);
    }




    public static class GenericTypes {
        public static byte NEST_TYPE_NORMAL = 0;
        public static byte NEST_TYPE_COLLECTION = 2;
        public static byte NEST_TYPE_MAP = 1;

        private final Class<?> nestClass;
        private List<Class<?>> types;
        private byte nestType = NEST_TYPE_NORMAL;
        private final boolean isRawOrEmpty;

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

        public List<Class<?>> getTypes() {
            return types;
        }


        public GenericTypes setTypes(List<Class<?>> types) {
            this.types = types;
            return this;
        }
    }

}
