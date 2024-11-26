package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

class CollectionItem {

    // 중첩 순서 값
    private int nestedLevel = 0;

    private CollectionFactory collectionFactory;


    CollectionItem(ParameterizedType type) {
        this.collectionType = (Class<?>) type.getRawType();
        //noinspection unchecked
        this.collectionConstructor = (Constructor<? extends Collection<?>>) collectionFactoryOfCollection(collectionType);
        Type[] actualTypes =  type.getActualTypeArguments();
        if(actualTypes.length > 0 && actualTypes[0] instanceof Class<?>) {
            this.setValueClass((Class<?>) type.getActualTypeArguments()[0]);
        } else {
            this.valueClass = null;
        }
        this.genericTypeName = "";
    }


    public void setValueClass(Class<?> valueClass) {
        isAbstractObject = valueClass.isInterface() || Modifier.isAbstract(valueClass.getModifiers());
        this.valueClass = valueClass;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }



    protected final Constructor<? extends Collection<?>> collectionConstructor;
    protected final Class<?> collectionType;
    private Class<?> valueClass;
    private boolean isGeneric = false;
    private boolean isAbstractObject = false;
    private String genericTypeName;


    public boolean isGeneric() {
        return isGeneric;
    }

    public void setGeneric(boolean generic) {
        isGeneric = generic;
    }

    public boolean isAbstractType() {
        return isAbstractObject;
    }

    public String getGenericTypeName() {
        return genericTypeName;
    }

    public void setGenericTypeName(String name) {
        this.isGeneric = true;
        this.genericTypeName = name;
    }





    protected Collection<?> newInstance() {
        try {
            return collectionConstructor.newInstance();
        } catch (Exception e) {
            throw new CSONSerializerException("Collection field '" + collectionType.getName() + "' has no default constructor");
        }
    }


    private static CollectionFactory collectionFactoryOfCollection(Class<?> type) {
        try {
            if (type.isInterface() && SortedSet.class.isAssignableFrom(type)) {
                return TreeSetFactory;
            } else if (type.isInterface() && Set.class.isAssignableFrom(type)) {
                return HashSetFactory;
            } else if (type.isInterface() && (AbstractQueue.class.isAssignableFrom(type) || Deque.class.isAssignableFrom(type) || Queue.class.isAssignableFrom(type))) {
                return ArrayDequeFactory;
            } else if (type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) || type == Collection.class) {
                return ArrayListFactory;
            } else if (type.isInterface() && (NavigableSet.class.isAssignableFrom(type) || SortedSet.class.isAssignableFrom(type))) {
                return TreeSetFactory;
            }
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CSONSerializerException("Collection field '" + type.getName() + "' has no default constructor");
        }
    }




    // List implementations
    private static final CollectionFactory ArrayListFactory = ArrayList::new;
    private static final CollectionFactory LinkedListFactory = LinkedList::new;
    private static final CollectionFactory VectorFactory = Vector::new; // Legacy vector
    private static final CollectionFactory CopyOnWriteArrayListFactory = CopyOnWriteArrayList::new; // Thread-safe list

    // Set implementations
    private static final CollectionFactory HashSetFactory = HashSet::new;
    private static final CollectionFactory LinkedHashSetFactory = LinkedHashSet::new;
    private static final CollectionFactory TreeSetFactory = TreeSet::new;
    private static final CollectionFactory CopyOnWriteArraySetFactory = CopyOnWriteArraySet::new; // Thread-safe set

    // Queue and Deque implementations
    private static final CollectionFactory ArrayDequeFactory = ArrayDeque::new;
    private static final CollectionFactory PriorityQueueFactory = PriorityQueue::new;
    private static final CollectionFactory ConcurrentLinkedQueueFactory = ConcurrentLinkedQueue::new; // Thread-safe queue
    private static final CollectionFactory ConcurrentLinkedDequeFactory = ConcurrentLinkedDeque::new; // Thread-safe deque
    private static final CollectionFactory StackFactory = Stack::new; // Legacy stack





    private static class CustomCollectionFactory implements CollectionFactory {

        final Constructor<? extends Collection<?>> collectionConstructor;

        CustomCollectionFactory(Constructor<? extends Collection<?>> collectionConstructor) {
            this.collectionConstructor = collectionConstructor;
        }

        @Override
        public <D> Collection<D> create() {
            try {
                //noinspection unchecked
                return (Collection<D>) collectionConstructor.newInstance();
            } catch (Exception e) {
                throw new CSONSerializerException("Collection field has no default constructor");
            }
        }





    }



    // Define the CollectionFactory interface with generics
    private interface CollectionFactory {
        <D> Collection<D> create();
    }




}
