package com.hancomins.cson.serializer.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ClassSchemaMap {

    private static final ClassSchemaMap instance = new ClassSchemaMap();

    private final Map<Class<?>, ClassSchema> typeInfoMap = new ConcurrentHashMap<>();

    private ClassSchemaMap() {
    }

    static ClassSchemaMap getInstance() {
        return instance;
    }

    boolean hasTypeInfo(Class<?> type) {
        return typeInfoMap.containsKey(type);
    }




    ClassSchema getTypeInfo(Class<?> type) {
        if(type.isAnonymousClass()) {
            type =  TypeUtil.getSuperClassIfAnonymous(type);
        }
        return typeInfoMap.computeIfAbsent(type, ClassSchema::create);
    }


}
