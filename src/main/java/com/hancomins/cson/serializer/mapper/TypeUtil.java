package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.serializer.CSON;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class TypeUtil {


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
            BigDecimal.class, BigInteger.class,
            String.class,
            /*Date.class, Calendar.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, ZonedDateTime.class, Instant.class,*/
            List.class, Set.class, Map.class,
            Collection.class,
            Optional.class

    );

    private static final List<Class<?>> SupportedJavaBeanTypes = listOf(
            Map.class, List.class, Set.class, Collection.class, Queue.class
    );


    private static boolean hasConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean isSupportedJavaBeanType(Class<?> clazz) {
        try {
            for (Class<?> type : SupportedJavaBeanTypes) {
                if (type.isAssignableFrom(clazz)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    static boolean isSupportedBean(Class<?> clazz) {
        // Java의 기본 클래스는 JavaBean이 아닙니다.
        if(clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.")) {
            return isSupportedJavaBeanType(clazz);
        }
        // 기본적으로 JavaBean 객체는 Serializable을 구현하고, 인자가 없는 기본 생성자가 있으며,
        // getter 및 setter 메서드를 가질 것으로 가정합니다.
        return hasConstructor(clazz);
    }

    static boolean isSupportedType(Class<?> fieldType) {
        if (SupportedTypes.contains(fieldType) ||
                fieldType.isEnum() ||
                isSupportedBean(fieldType)) {
            return true;
        }
        for(Class<?> type : SupportedTypes) {
            if(type.isAssignableFrom(fieldType)) {
                return true;
            }
        }
        return false;

    }

    static Class<?> getSuperClassIfAnonymous(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class /*&& type.getAnnotation(CSON.class) != null*/) {
            return superClass;
        }
        // todo : 인터페이스 처리에 대하여 고민해본다.
        /*Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            if (interfaceClass.getAnnotation(CSON.class) != null) {
                return interfaceClass;
            }
        }*/
        return type;
    }



    static List<Field> filterSupportedTypes(List<Field> allFields) {
        return allFields.stream().filter(field -> isSupportedType(field.getType())).collect(Collectors.toList());
    }



    @SafeVarargs
    private static  <T> Set<T> setOf(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }



    @SafeVarargs
    private static  <T> List<T> listOf(T... values) {
        return Arrays.asList(values);
    }



}
