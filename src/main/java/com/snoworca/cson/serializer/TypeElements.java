package com.snoworca.cson.serializer;

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

    TypeElement getTypeInfo(Class<?> type) {
        TypeElement typeInfo = typeInfoMap.get(type);
        if(typeInfo == null) {
            typeInfo = TypeElement.create(type);
            typeInfoMap.put(type, typeInfo);
        }
        return typeInfo;
    }


}
