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

    TypeElement getTypeInfo(Class<?> type) {
        return typeInfoMap.computeIfAbsent(type, TypeElement::create);
    }


}
