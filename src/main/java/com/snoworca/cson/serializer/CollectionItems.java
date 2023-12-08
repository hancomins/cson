package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class CollectionItems {



    private static Constructor<?> constructorOfCollection(Class<?> type) {
        try {
            if (type.isInterface() && SortedSet.class.isAssignableFrom(type)) {
                return TreeSet.class.getConstructor();
            } else if (type.isInterface() && Set.class.isAssignableFrom(type)) {
                return HashSet.class.getConstructor();
            } else if (type.isInterface() && (AbstractQueue.class.isAssignableFrom(type) || Deque.class.isAssignableFrom(type) || Queue.class.isAssignableFrom(type))) {
                return ArrayDeque.class.getConstructor();
            } else if (type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) || type == Collection.class) {
                return ArrayList.class.getConstructor();
            } else if (type.isInterface() && (NavigableSet.class.isAssignableFrom(type) || SortedSet.class.isAssignableFrom(type))) {
                return TreeSet.class.getConstructor();
            }
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CSONSerializerException("Collection field '" + type.getName() + "' has no default constructor");
        }
    }

    CollectionItems(ParameterizedType type) {
        this.collectionType = (Class<?>) type.getRawType();
        //noinspection unchecked
        this.collectionConstructor = (Constructor<? extends Collection<?>>) constructorOfCollection(collectionType);
        Type[] actualTypes =  type.getActualTypeArguments();
        if(actualTypes.length > 0 && actualTypes[0] instanceof Class<?>) {
            this.valueClass = (Class<?>) type.getActualTypeArguments()[0];
        } else {
            this.valueClass = null;
        }
    }
    protected final Constructor<? extends Collection<?>> collectionConstructor;
    protected final Class<?> collectionType;
    protected Class<?> valueClass;



    protected Collection<?> newInstance() {
        try {
            return collectionConstructor.newInstance();
        } catch (Exception e) {
            throw new CSONSerializerException("Collection field '" + collectionType.getName() + "' has no default constructor");
        }
    }
}
