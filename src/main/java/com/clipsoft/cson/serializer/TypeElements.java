package com.clipsoft.cson.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TypeElements {

    private static final TypeElements instance = new TypeElements();

    private final Map<Class<?>, TypeElement> typeInfoMap = new ConcurrentHashMap<>();

    private TypeElements() {
    }

    static TypeElements getInstance() {
        return instance;
    }

    boolean hasTypeInfo(Class<?> type) {
        return typeInfoMap.containsKey(type);
    }

    private Class<?> getSuperClassIfAnonymous(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class && type.getAnnotation(CSON.class) != null) {
            return superClass;
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            if (interfaceClass.getAnnotation(CSON.class) != null) {
                return interfaceClass;
            }
        }
        return type;
    }



    TypeElement getTypeInfo(Class<?> type) {
        if(type.isAnonymousClass()) {
            type = getSuperClassIfAnonymous(type);
        }
        return typeInfoMap.computeIfAbsent(type, TypeElement::create);
    }


}
