package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.util.GenericTypeAnalyzer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
class MapItem {

    private final int nestedLevel;
    private final MapFactory mapFactory;
    protected final Class<?> mapType;
    private Class<?> keyClass;
    private Class<?> valueClass;
    private boolean isGeneric = false;
    private boolean isAbstractObject = false;
    private String genericTypeName;
    private MapItem parent;
    private MapItem child;

    static List<MapItem> buildMapItemsByField(Field field) {
        List<Class<?>> genericTypes = GenericTypeAnalyzer.analyzeField(field);
        String sourcePath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        return buildMapItems(genericTypes, sourcePath);
    }

    static List<MapItem> buildMapItemsByParameter(Method method, int parameterIndex) {
        Parameter parameter = method.getParameters()[parameterIndex];
        List<Class<?>> genericTypes = GenericTypeAnalyzer.analyzeParameter(parameter);
        String sourcePath = method.getDeclaringClass().getName() + "." + method.getName() + "(parameter index: " + parameterIndex + ") <type: " + parameter.getType().getName() + ">";
        return buildMapItems(genericTypes, sourcePath);
    }

    static List<MapItem> buildMapItemsByMethodReturn(Method method) {
        List<Class<?>> genericTypes = GenericTypeAnalyzer.analyzeReturnType(method);
        String sourcePath = method.getDeclaringClass().getName() + "." + method.getName() + "<type: " + method.getReturnType().getName() + ">";
        return buildMapItems(genericTypes, sourcePath);
    }

    private static List<MapItem> buildMapItems(List<Class<?>> genericTypes, String sourcePath) {
        if (genericTypes.size() < 2) {
            throw new IllegalArgumentException("Map type must have both key and value generic types. (" + sourcePath + ")");
        }

        Class<?> keyType = genericTypes.get(0);
        Class<?> valueType = genericTypes.get(1);

        if (Map.class.isAssignableFrom(keyType) || Map.class.isAssignableFrom(valueType)) {
            throw new IllegalArgumentException("Nested raw Map types are not allowed. Use generic types. (" + sourcePath + ")");
        }

        MapItem prev;
        List<MapItem> mapItems = new ArrayList<>();
        for (int i = 0, n = genericTypes.size() - 1; i < n; i++) {
            Class<?> type = genericTypes.get(i);
            MapItem mapItem = new MapItem(type, keyType, valueType, i);
            mapItems.add(mapItem);
            if (i > 0) {
                prev = mapItems.get(i - 1);
                prev.child = mapItem;
                mapItem.parent = prev;
            }
        }
        return mapItems;
    }

    private MapItem(Class<?> mapType, Class<?> keyClass, Class<?> valueClass, int nestedLevel) {
        this.mapType = mapType;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.nestedLevel = nestedLevel;
        this.mapFactory = mapFactoryOfMap(mapType);
    }

    public int getNestedLevel() {
        return nestedLevel;
    }

    public void setKeyClass(Class<?> keyClass) {
        this.keyClass = keyClass;
    }

    public void setValueClass(Class<?> valueClass) {
        isAbstractObject = valueClass.isInterface() || Modifier.isAbstract(valueClass.getModifiers());
        this.valueClass = valueClass;
    }

    public Class<?> getKeyClass() {
        return keyClass;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    public MapItem getParent() {
        return parent;
    }

    public MapItem getChild() {
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

    protected Map<?, ?> newInstance() {
        return mapFactory.create();
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

        return new CustomMapFactory(getDefaultMapConstructor(type));
    }

    private static Constructor<? extends Map<?, ?>> getDefaultMapConstructor(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            //noinspection unchecked
            return (Constructor<? extends Map<?, ?>>) constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Map field '" + type.getName() + "' has no default constructor");
        }
    }

    public boolean compatibleMapType(MapItem other) {
        return mapFactory.equals(other.mapFactory);
    }

    // Map implementations
    private static final MapFactory HashMapFactory = HashMap::new;
    private static final MapFactory LinkedHashMapFactory = LinkedHashMap::new;
    private static final MapFactory TreeMapFactory = TreeMap::new;
    private static final MapFactory ConcurrentHashMapFactory = ConcurrentHashMap::new;
    private static final MapFactory ConcurrentSkipListMapFactory = ConcurrentSkipListMap::new;

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

    private interface MapFactory {
        <T, D> Map<T, D> create();
    }
}
