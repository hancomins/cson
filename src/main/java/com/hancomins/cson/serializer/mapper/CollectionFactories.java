package com.hancomins.cson.serializer.mapper;


import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

class CollectionFactories {


    private static final ConcurrentHashMap<Class<?>, GenericFactory> genericFactories = new ConcurrentHashMap<>();

    private Object newInstance(Class<?> type) {
       Factory factory = getFactory(type);
         if(factory instanceof GenericFactory) {
              return ((GenericFactory) factory).create();
         }
         else if(factory instanceof CollectionFactory) {
             return ((CollectionFactory) factory).create();
         }
         else if(factory instanceof MapFactory) {
             return ((MapFactory) factory).create();
         }
            return null;
    }


    private CollectionFactories() {

    }

    public static Factory getFactory(Class<?> type) {
        if(Collection.class.isAssignableFrom(type)) {
            return collectionFactoryOfCollection(type);
        } else if(Map.class.isAssignableFrom(type)) {
            return mapFactoryOfMap(type);
        }

        return genericFactories.computeIfAbsent(type, CollectionFactories::genericFactoryOf);
    }

    private static GenericFactory genericFactoryOf(Class<?> type) {
        return new GenericFactory() {
            final Constructor<?> constructor = getDefaultConstructor(type);
            @Override
            public Object create() {
                try {
                    return constructor.newInstance();
                } catch (Exception e) {
                    throw new CSONMapperException("Field has no default constructor");
                }
            }
        };
    }


    private static MapFactory mapFactoryOfMap(Class<?> type) {
        boolean isInterface = type.isInterface();

        if ((isInterface && Map.class.equals(type)) || HashMap.class.equals(type) || AbstractMap.class.equals(type)) {
            return HashMapFactory;
        } else if (LinkedHashMap.class.equals(type)) {
            return LinkedHashMapFactory;
        } else if (TreeMap.class.equals(type) || (isInterface && SortedMap.class.equals(type))) {
            return TreeMapFactory;
        } else if (ConcurrentHashMap.class.equals(type)) {
            return ConcurrentHashMapFactory;
        } else if (ConcurrentSkipListMap.class.equals(type)) {
            return ConcurrentSkipListMapFactory;
        }

        //noinspection unchecked
        return new CustomMapFactory((Constructor<? extends Map<?, ?>>) getDefaultConstructor(type));
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

        //noinspection unchecked
        return new CustomCollectionFactory((Constructor<? extends Collection<?>>) getDefaultConstructor(type));
    }



    private static Constructor<?> getDefaultConstructor(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            //noinspection unchecked
            return (Constructor<? extends Map<?, ?>>) constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type.getSimpleName() +  " field '" + type.getName() + "' has no default constructor");
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

    private static final MapFactory HashMapFactory = HashMap::new;
    private static final MapFactory LinkedHashMapFactory = LinkedHashMap::new;
    private static final MapFactory TreeMapFactory = TreeMap::new;
    private static final MapFactory ConcurrentHashMapFactory = ConcurrentHashMap::new;
    private static final MapFactory ConcurrentSkipListMapFactory = ConcurrentSkipListMap::new;



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



    private static class CustomMapFactory implements MapFactory {

        final Constructor<? extends Map<?, ?>> mapConstructor;

        CustomMapFactory(Constructor<? extends Map<?, ?>> mapConstructor) {
            this.mapConstructor = mapConstructor;
        }

        @Override
        public <T, D> Map<T, D> create() {
            try {
                //noinspection unchecked
                return (Map<T, D>) mapConstructor.newInstance();
            } catch (Exception e) {
                throw new CSONMapperException("Map field has no default constructor");
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != CustomMapFactory.class) {
                return false;
            }
            CustomMapFactory other = (CustomMapFactory) obj;
            return mapConstructor.equals(other.mapConstructor);
        }
    }

    public interface Factory {
    }

    public interface MapFactory extends Factory {
        <T, D> Map<T, D> create();
    }

    public interface GenericFactory extends Factory {
        Object create();
    }


    // Define the CollectionFactory interface with generics
    public interface CollectionFactory extends Factory {
        <D> Collection<D> create();
    }




}
