package com.hancomins.cson.serializer.mapper;

class GenericTypeItem<T extends GenericTypeItem<T>> {

    // 중첩 순서 값
    private final int nestedLevel;
    protected final Class<?> collectionType;
    private Class<?> valueClass;
    private boolean isGeneric = false;
    private boolean isAbstractObject = false;
    private String genericTypeName;
    private CollectionItem parent;
    private CollectionItem child;



}
