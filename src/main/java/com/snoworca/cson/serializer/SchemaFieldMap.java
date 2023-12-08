package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

class SchemaFieldMap extends SchemaField implements ISchemaMapValue {

    private final Constructor<?> constructorMap;
    private final Class<?> elementClass;
    SchemaFieldMap(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, field, path);

        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        Type genericType = field.getGenericType();
        Map.Entry<Class<?>, Class<?>> entry = ISchemaMapValue.readKeyValueGenericType(genericType, fieldPath);
        Class<?> keyClass = entry.getKey();
        this.elementClass = entry.getValue();
        if(elementClass != null) {
            ISchemaValue.assertValueType(elementClass, fieldPath);
        }
        ISchemaMapValue.assertCollectionOrMapValue(elementClass,fieldPath);


        if(!String.class.isAssignableFrom(keyClass)) {
            throw new CSONSerializerException("Map field '" + fieldPath + "' is not String key. Please use String key.");
        }
        constructorMap = ISchemaMapValue.constructorOfMap(field.getType());
    }


    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaMapValue)) {
            return false;
        }
        ISchemaMapValue mapValue = (ISchemaMapValue)schemaValueAbs;
        if(elementClass != null && !elementClass.equals( mapValue.getElementType())) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }



    @Override
    public Class<?> getElementType() {
        return elementClass;
    }

    @Override
    public Object newInstance() {
        try {
            return constructorMap.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CSONSerializerException("Map type " + field.getDeclaringClass().getName() + "." + field.getType().getName() + " has no default constructor.", e);
        }
    }


    @SuppressWarnings("unchecked")



    @Override
    public ISchemaNode copyNode() {
        return new SchemaFieldMap(parentsTypeElement, field, path);
    }
}
