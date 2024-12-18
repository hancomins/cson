package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.util.GenericTypeAnalyzer;
import java.lang.reflect.Method;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

class CollectionItem {

    // 중첩 순서 값
    private int nestedLevel;
    private final CollectionFactory collectionFactory;
    protected final Class<?> collectionType;
    private Class<?> valueClass;
    private boolean isGeneric = false;
    private boolean isAbstractObject = false;
    private String genericTypeName;
    private CollectionItem parent;
    private CollectionItem child;



    static List<CollectionItem> buildCollectionItemsByField(Field field) {
        List<Class<?>> genericTypes = GenericTypeAnalyzer.analyzeField(field);
        String sourcePath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        return buildCollectionItems(genericTypes, sourcePath);
    }

    static  List<CollectionItem> buildCollectionItemsByParameter(Method method, int parameterIndex) {
        Parameter parameter = method.getParameters()[parameterIndex];
        List<Class<?>> genericTypes = GenericTypeAnalyzer.analyzeParameter(parameter);
        String sourcePath = method.getDeclaringClass().getName() + "." + method.getName()  + "(parameter index: " + parameterIndex  +  ") <type: " + parameter.getType().getName() + ">";
        return buildCollectionItems(genericTypes, sourcePath);
    }

    static List<CollectionItem> buildCollectionItemsByMethodReturn(Method method) {
        List<Class<?>> genericTypes = GenericTypeAnalyzer.analyzeReturnType(method);
        String sourcePath = method.getDeclaringClass().getName() + "." + method.getName() + "<type: " + method.getReturnType().getName() + ">";
        return buildCollectionItems(genericTypes, sourcePath);
    }


    private static List<CollectionItem> buildCollectionItems(List<Class<?>> genericTypes, String sourcePath) {
        Class<?> valueType = genericTypes.get(genericTypes.size() - 1);
        if(Collection.class.isAssignableFrom(valueType)) {
            throw new IllegalArgumentException("Raw Collection type is not allowed. Use generic type. (" + sourcePath+ ")");
        }
        CollectionItem prev;
        List<CollectionItem> collectionItems = new ArrayList<>();
        for(int i = 0, n = genericTypes.size() - 1; i < n; i++) {
            Class<?> type = genericTypes.get(i);
            CollectionItem collectionItem = new CollectionItem(type, valueType, i);
            collectionItems.add(collectionItem);
            if(i > 0) {
                prev = collectionItems.get(i - 1);
                prev.child = collectionItem;
                collectionItem.parent = prev;
            }

        }
        return collectionItems;
    }




    private CollectionItem(Class<?> collectionType, Class<?> valueClass,int nestedLevel) {
        this.collectionType = collectionType;
        this.valueClass = valueClass;
        this.nestedLevel = nestedLevel;
        this.collectionFactory = collectionFactoryOfCollection(collectionType);
    }

    public int getNestedLevel() {
        return nestedLevel;
    }



    public void setValueClass(Class<?> valueClass) {
        isAbstractObject = valueClass.isInterface() || Modifier.isAbstract(valueClass.getModifiers());
        this.valueClass = valueClass;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    public CollectionItem getParent() {
        return parent;
    }

    public CollectionItem getChild() {
        return child;
    }



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
        return collectionFactory.create();
    }


    private static CollectionFactory collectionFactoryOfCollection(Class<?> type) {
        boolean isInterface = type.isInterface();

        if( (isInterface && (Collection.class.equals(type) || Iterable.class.equals(type) || List.class.equals(type)))  ||
                ArrayList.class.equals(type) || AbstractList.class.equals(type)) {
            return ArrayListFactory;
        } else if ( Vector.class.equals(type)) {
            return VectorFactory;
        } else if ( LinkedList.class.equals(type)) {
            return LinkedListFactory;
        } else if ( CopyOnWriteArrayList.class.equals(type)) {
            return CopyOnWriteArrayListFactory;
        }

        else if ( (isInterface && Set.class.equals(type) ) || HashSet.class.equals(type) || AbstractSet.class.equals(type)) {
            return HashSetFactory;
        } else if ( LinkedHashSet.class.equals(type)) {
            return LinkedHashSetFactory;
        } else if ( TreeSet.class.equals(type) || (isInterface && SortedSet.class.equals(type))) {
            return TreeSetFactory;
        } else if ( CopyOnWriteArraySet.class.equals(type) ) {
            return CopyOnWriteArraySetFactory;
        }

        else if ( (isInterface && Queue.class.equals(type) ) || ArrayDeque.class.equals(type) || AbstractQueue.class.equals(type)) {
            return ArrayDequeFactory;
        } else if ( PriorityQueue.class.equals(type)) {
            return PriorityQueueFactory;
        } else if ( ConcurrentLinkedQueue.class.equals(type)) {
            return ConcurrentLinkedQueueFactory;
        } else if ( ConcurrentLinkedDeque.class.equals(type)) {
            return ConcurrentLinkedDequeFactory;
        } else if ( Stack.class.equals(type)) {
            return StackFactory;
        }

        return new CustomCollectionFactory(getDefualtCollectionConstructor(type));
    }


    private static Constructor<? extends Collection<?>> getDefualtCollectionConstructor(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            //noinspection unchecked
            return (Constructor<? extends Collection<?>>) constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Collection field '" + type.getName() + "' has no default constructor");
        }
    }

    /**
     * 변환 가능한 Collection 타입인지 확인
     */
    public boolean compatibleCollectionType(CollectionItem other) {
        return collectionFactory.equals(other.collectionFactory);
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
                throw new CSONMapperException("Collection field has no default constructor");
            }
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this) {
                return true;
            }
            if(obj == null || obj.getClass() != CustomCollectionFactory.class) {
                return false;
            }
            CustomCollectionFactory other = (CustomCollectionFactory) obj;
            return collectionConstructor.equals(other.collectionConstructor);
        }



    }



    // Define the CollectionFactory interface with generics
    private interface CollectionFactory {
        <D> Collection<D> create();
    }




}
