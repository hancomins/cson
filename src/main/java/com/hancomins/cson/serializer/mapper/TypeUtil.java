package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class TypeUtil {

    @SafeVarargs
    private static  <T> Set<T> setOf(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }


    private static final Set<Class<?>> SupportedTypes =  setOf(
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            boolean.class, Boolean.class,
            char.class, Character.class,
            byte.class, Byte.class,
            short.class, Short.class,
            byte[].class, Byte[].class,
            String.class,
            /*Date.class, Calendar.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, ZonedDateTime.class, Instant.class,*/
            List.class, Set.class, Map.class,
            Collection.class,
            Optional.class
    );



    private static boolean isJavaBean(Class<?> clazz) {
        // 기본적으로 JavaBean 객체는 Serializable을 구현하고, 인자가 없는 기본 생성자가 있으며,
        // getter 및 setter 메서드를 가질 것으로 가정합니다.
        try {
            clazz.getDeclaredConstructor(); // 기본 생성자 확인
            // 필요한 추가 확인이 있다면, 여기서 추가합니다.
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static boolean isSupportedType(Class<?> fieldType) {
        if (SupportedTypes.contains(fieldType) ||
                fieldType.isEnum() ||
                isJavaBean(fieldType)) {
            return true;
        }
        for(Class<?> type : SupportedTypes) {
            if(type.isAssignableFrom(fieldType)) {
                return true;
            }
        }
        return false;

    }


    static List<Field> filterSupportedTypes(List<Field> allFields) {
        return allFields.stream().filter(field -> isSupportedType(field.getType())).collect(Collectors.toList());
    }



}
