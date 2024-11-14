package com.hancomins.cson.serializer.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class RootObjectNodeMap {

    private static final RootObjectNodeMap instance = new RootObjectNodeMap();

    private final Map<Class<?>, SchemaObjectNode> rootObjectNodeMap = new ConcurrentHashMap<>();

    private RootObjectNodeMap() {
    }

    static RootObjectNodeMap getInstance() {
        return instance;
    }

    boolean hasOf(Class<?> type) {
        return rootObjectNodeMap.containsKey(type);
    }



    private Class<?> getSuperClassIfAnonymous(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class && TypeUtil.isSupportedBean(superClass)) {
            return superClass;
        }
        // todo : 인터페이스 처리에 대하여 고민해본다.
        /*Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            //if (interfaceClass.getAnnotation(CSON.class) != null) {
            if(TypeUtil.isSupportedBean(interfaceClass)) {
                return interfaceClass;
            }
        }*/
        return type;
    }




    SchemaObjectNode getObjectNode(Class<?> type) {
        if(type.isAnonymousClass()) {
            type = getSuperClassIfAnonymous(type);
        }
        Class<?> finalType = type;
        return rootObjectNodeMap.computeIfAbsent(type, (key) -> {
            ClassSchema typeSchema = ClassSchemaMap.getInstance().getTypeInfo(finalType);
            return NodePath.makeNode(typeSchema, null, -1);
        });
    }


}
