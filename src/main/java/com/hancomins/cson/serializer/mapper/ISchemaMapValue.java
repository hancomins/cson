package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public interface ISchemaMapValue {



    Class<?> getElementType();

    Object newInstance();

    boolean isGenericValue();
    boolean isAbstractType();
    ObtainTypeValueInvoker getObtainTypeValueInvoker();


    static Constructor<?> constructorOfMap(Class<?> type) {
        try {
            if (type.isInterface() && Map.class.isAssignableFrom(type)) {
                return HashMap.class.getConstructor();
            } else if(type.isInterface() && SortedMap.class.isAssignableFrom(type)) {
                return TreeMap.class.getConstructor();
            } else if(type.isInterface() && NavigableMap.class.isAssignableFrom(type)) {
                return TreeMap.class.getConstructor();
            } else if(type.isInterface() && ConcurrentMap.class.isAssignableFrom(type)) {
                return ConcurrentHashMap.class.getConstructor();
            } else if(type.isInterface() && ConcurrentNavigableMap.class.isAssignableFrom(type)) {
                return ConcurrentSkipListMap.class.getConstructor();
            }
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CSONObjectException("Map type " + type.getName() + " has no default constructor.");
        }
    }


    static void assertCollectionOrMapValue(Class<?> type, String path) {
        if(type == null) return;
        if(Map.class.isAssignableFrom(type)) {
            throw new CSONObjectException("The java.util.Map type cannot be directly used as a value element of a Map. Please create a class that wraps your Map and use it as a value element of the Map. (path: " + path + ")");
        } else if(Collection.class.isAssignableFrom(type)) {
            throw new CSONObjectException("The java.util.Map type cannot be directly used as a value element of a java.util.Map. Please create a class that wraps your Collection and use it as a value element of the Map  of field. (path: " + path + ")");
        }
    }


    static Map.Entry<Class<?>, Type> readKeyValueGenericType(Type genericType, String path) {
        if (genericType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType aType = (java.lang.reflect.ParameterizedType) genericType;
            Type[] fieldArgTypes = aType.getActualTypeArguments();
            if(fieldArgTypes.length != 2) {
                throw new CSONObjectException("Map must use <generic> types. (path: " + path + ")");
            }
            if(fieldArgTypes[0] instanceof Class<?> && fieldArgTypes[1] instanceof Class<?>) {
                return new AbstractMap.SimpleEntry<>((Class<?>)fieldArgTypes[0], (Class<?>)fieldArgTypes[1]);
            } else if(fieldArgTypes[1] instanceof  java.lang.reflect.ParameterizedType) {
                assert fieldArgTypes[0] instanceof Class<?>;
                return new AbstractMap.SimpleEntry<>((Class<?>)fieldArgTypes[0], (Class<?>)((java.lang.reflect.ParameterizedType)fieldArgTypes[1]).getRawType());
            }  else if(fieldArgTypes[1] instanceof  java.lang.reflect.TypeVariable) {
                return new AbstractMap.SimpleEntry<>((Class<?>)fieldArgTypes[0],fieldArgTypes[1]);
            }
            else {
                throw new CSONObjectException("Map must use <generic> types. (path: " + path + ")");
            }
        } else  {
            throw new CSONObjectException("Invalid Map or RAW type. Collections must use <generic> types. (path: " + path + ")");
        }
    }

}
