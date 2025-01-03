package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.util.GenericTypeAnalyzer;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class GenericItem {


    private final Class<?> nestType;
    private final Class<?> fistType;
    private final Class<?> valueClass;
    private final int nestedLevel;
    private GenericItem parent;
    private GenericItem child;
    private boolean generic;
    private String genericTypeName;
    private final String sourcePath;
    private CollectionFactories.Factory factory;



    static List<GenericItem> analyzeMethodReturn(Method method) {
        List<GenericTypeAnalyzer.GenericTypes> genericTypes = GenericTypeAnalyzer.analyzeReturnType(method);
        String sourcePath = method.getDeclaringClass().getName() + "." + method.getName() + "() <return: " + method.getReturnType().getName() + ">";
        return analyze(genericTypes, sourcePath);
    }


    static List<GenericItem> analyzeParameter(Method method, int position) {
        Parameter parameter = method.getParameters()[position];
        String sourcePath = parameter.getDeclaringExecutable().getDeclaringClass().getName() + "." + parameter.getDeclaringExecutable().getName() + "<type: " + parameter.getType().getName() + ", position: " + position + ">";
        List<GenericTypeAnalyzer.GenericTypes> genericTypes = GenericTypeAnalyzer.analyzeParameter(parameter);
        return analyze(genericTypes, sourcePath);
    }

    static List<GenericItem> analyzeParameter(Parameter parameter) {
        String sourcePath = parameter.getDeclaringExecutable().getDeclaringClass().getName() + "." + parameter.getDeclaringExecutable().getName() + "<type: " + parameter.getType().getName() + ">";
        List<GenericTypeAnalyzer.GenericTypes> genericTypes = GenericTypeAnalyzer.analyzeParameter(parameter);
        return analyze(genericTypes, sourcePath);
    }

    static List<GenericItem> analyzeField(Field field) {
        String sourcePath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        List<GenericTypeAnalyzer.GenericTypes> genericTypes = GenericTypeAnalyzer.analyzeField(field);
        return analyze(genericTypes, sourcePath);
    }

    private static List<GenericItem> analyze(List<GenericTypeAnalyzer.GenericTypes> items, String sourcePath) {
        List<GenericItem> result = new ArrayList<>();
        int nestedLevel = 0;
        for (GenericTypeAnalyzer.GenericTypes item : items) {
            GenericItem genericItem = new GenericItem(nestedLevel++, item, sourcePath);
            genericItem.factory = CollectionFactories.getFactory(item.getNestClass());
            result.add(genericItem);
        }
        return result;
    }

    Object newInstance() {
        if(factory instanceof  CollectionFactories.MapFactory) {
            return ((CollectionFactories.MapFactory) factory).create();
        } else if(factory instanceof CollectionFactories.CollectionFactory) {
            return ((CollectionFactories.CollectionFactory) factory).create();
        } else if(factory instanceof CollectionFactories.GenericFactory) {
            return ((CollectionFactories.GenericFactory) factory).create();
        } else {
            return null;
        }
    }



    protected GenericItem(int nestedLevel, GenericTypeAnalyzer.GenericTypes genericTypes, String sourcePath) {
        this.nestedLevel = nestedLevel;
        this.sourcePath = sourcePath;
        this.fistType = genericTypes.getKeyType();
        this.valueClass = genericTypes.getValueType();
        this.nestType = genericTypes.getNestClass();
    }

    public int getNestedLevel() {
        return nestedLevel;
    }

    public void setParent(GenericItem parent) {
        this.parent = parent;
    }

    public GenericItem getParent() {
        return parent;
    }

    public Class<?> getNestType() {
        return nestType;
    }

    public void setChild(GenericItem child) {
        this.child = child;
    }

    public GenericItem getChild() {
        return child;
    }

    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    public String getGenericTypeName() {
        return genericTypeName;
    }

    public void setGenericTypeName(String name) {
        this.genericTypeName = name;
    }

    public Class<?> getValueType() {
        return valueClass;
    }

    public Class<?> getFirstType() {
        return fistType;
    }

    public String getSourceName() {
        return sourcePath;
    }

    public boolean compatibleCollectionType(GenericItem other) {
        return factory.equals(other.factory);
    }
}
