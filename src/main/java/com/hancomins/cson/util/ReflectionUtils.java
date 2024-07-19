package com.hancomins.cson.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

    /**
     * 지정된 클래스의 모든 상속된 메서드를 반환합니다.
     *
     * @param type 메서드를 검색할 클래스
     * @return List<Method> 상속된 모든 메서드 목록
     */
    public static List<Method> getAllInheritedMethods(final Class<?> type) {
        List<Method> methods = new ArrayList<>();
        searchSuperClassAndInterfaces(type, superClass -> {
            methods.addAll(Arrays.asList(superClass.getDeclaredMethods()));
            return true;
        });

        return methods;
    }

    /**
     * 지정된 클래스의 모든 상속된 필드를 반환합니다.
     *
     * @param type 필드를 검색할 클래스
     * @return 상속된 모든 필드 목록
     */
    public static List<Field> getAllInheritedFields(final Class<?> type) {
        List<Field> methods = new ArrayList<>();
        searchSuperClass(type, superClass -> {
            methods.addAll(Arrays.asList(superClass.getDeclaredFields()));
            return true;
        });
        return methods;
    }


    /**
     * 주어진 클래스 및 인터페이스의 상속 구조를 검색하고, 각 상위 클래스 또는 인터페이스에 대해 주어진 작업을 수행합니다.
     *
     * @param type        상속 구조를 검색할 클래스
     * @param onSuperClass 각 상위 클래스 또는 인터페이스에 대해 수행할 작업을 정의하는 콜백 인터페이스
     */
    public static void searchSuperClassAndInterfaces(Class<?> type, OnSuperClass onSuperClass) {
        searchSuperClass(type, onSuperClass);
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if(!onSuperClass.onSuperClass(anInterface)) {
                break;
            }
        }
    }


    /**
     * 주어진 클래스의 상속 구조를 검색하고, 각 상위 클래스에 대해 주어진 작업을 수행합니다.
     *
     * @param type        상속 구조를 검색할 클래스
     * @param onSuperClass 각 상위 클래스에 대해 수행할 작업을 정의하는 콜백 인터페이스
     */
    public static void searchSuperClass(Class<?> type, OnSuperClass onSuperClass) {
        Class<?> currentType = type;
        while (currentType != Object.class && currentType != null) {
            if(!onSuperClass.onSuperClass(currentType)) {
                break;
            }
            currentType = currentType.getSuperclass();
        }

    }

    /**
     * 상속 구조를 검색할 때 사용할 콜백 인터페이스입니다.
     */
    public interface OnSuperClass {
        /**
         * 상속 검색에 대한 결과값을 반환합니다.
         * @param superClass 상속 검색 중인 클래스
         * @return true 값을 반환할 경우 상속 검색을 계속합니다. false 값을 반환할 경우 상속 검색을 중단합니다.
         */
        boolean onSuperClass(Class<?> superClass);
    }

}
