package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GenericItemImpl implements GenericItem {


    protected final Class<?> type;
    //private final Class<?> fistType;
    private final Class<?> valueClass;
    private final int nestedLevel;
    private GenericItem parent;
    private GenericItem child;
    private boolean generic;
    private String genericTypeName;

    static List<GenericItem> analyzeParameter(Parameter parameter) {
        return null;
    }

    static List<GenericItem> analyzeField(Field field) {
        return null;
    }


    static GenericItem GenericItems(String sourcePath, boolean isMap, List<Class<?>> genericTypes) {
        if(isMap && genericTypes.size() != 2) {
            throw new IllegalArgumentException("Map type must have both key and value generic types. (" + sourcePath + ")");
        }
        Class<?> keyType = isMap ? genericTypes.get(0) : null;
        Class<?> valueType = genericTypes.get(genericTypes.size() - 1);
        if(Collection.class.isAssignableFrom(valueType)) {
            throw new IllegalArgumentException("Raw type is not allowed. Use generic type. (" + sourcePath+ ")");
        }
        GenericItemImpl prev;
        List<GenericItemImpl> genericItems = new ArrayList<>();
        for(int i = 0, n = genericTypes.size() - 1; i < n; i++) {
            Class<?> type = genericTypes.get(i);
            GenericItemImpl collectionItem = new GenericItemImpl(type, valueType, i);
            genericItems.add(collectionItem);
            if(i > 0) {
                prev = genericItems.get(i - 1);
                prev.child = collectionItem;
                collectionItem.parent = prev;
            }
        }
        return genericItems.get(0);
    }

    protected GenericItemImpl(Class<?> collectionType, Class<?> valueClass,int nestedLevel) {
        this.type = collectionType;
        this.valueClass = valueClass;
        this.nestedLevel = nestedLevel;
    }


    @Override
    public int getNestedLevel() {
        return nestedLevel;
    }

    @Override
    public void setParent(GenericItem parent) {
        this.parent = parent;
    }

    @Override
    public GenericItem getParent() {
        return parent;
    }

    @Override
    public void setChild(GenericItem child) {
        this.child = child;
    }

    @Override
    public GenericItem getChild() {
        return child;
    }

    @Override
    public boolean isGeneric() {
        return generic;
    }

    @Override
    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    @Override
    public String getGenericTypeName() {
        return genericTypeName;
    }

    @Override
    public void setGenericTypeName(String name) {
        this.genericTypeName = name;
    }

    @Override
    public Class<?> getValueType() {
        return valueClass;
    }
}
