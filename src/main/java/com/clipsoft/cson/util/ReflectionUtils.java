package com.clipsoft.cson.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {
    public static List<Method> allMethods(final Class<?> type) {
        List<Method> methods = new ArrayList<>();
        findSuperClass(type, superClass -> methods.addAll(Arrays.asList(superClass.getDeclaredMethods())));
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            methods.addAll(Arrays.asList(anInterface.getDeclaredMethods()));
        }
        return methods;
    }

    public static List<Field> allFields(final Class<?> type) {
        List<Field> methods = new ArrayList<>();
        findSuperClass(type, superClass -> methods.addAll(Arrays.asList(superClass.getDeclaredFields())));
        return methods;
    }


    public static void findSuperClass(Class<?> type, OnSuperClass onSuperClass) {
        Class<?> currentType = type;
        while (currentType != Object.class && currentType != null) {
            onSuperClass.onSuperClass(currentType);
            currentType = currentType.getSuperclass();
        }
    }

    public static interface OnSuperClass {
        void onSuperClass(Class<?> superClass);
    }

}
