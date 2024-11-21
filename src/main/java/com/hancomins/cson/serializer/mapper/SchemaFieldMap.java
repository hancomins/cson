package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.*;
import java.util.Map;

;


class SchemaFieldMap extends SchemaField implements ISchemaMapValue {

    private final Constructor<?> constructorMap;
    private final Class<?> elementClass;
    private final boolean isGenericTypeValue;
    private final boolean isAbstractValue;
    private ObtainTypeValueInvoker obtainTypeValueInvoker;
    SchemaFieldMap(ClassSchema parentsTypeSchema, Field field, String path) {
        super(parentsTypeSchema, field, path);

        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        Type genericType = field.getGenericType();
        Map.Entry<Class<?>, Type> entry = ISchemaMapValue.readKeyValueGenericType(genericType, fieldPath);
        Class<?> keyClass = entry.getKey();
        Type valueType = entry.getValue();
        boolean isGenericValue = false;
        if(valueType instanceof Class<?>) {
            this.elementClass = (Class<?>)valueType;
        } else if(valueType instanceof TypeVariable) {
            this.elementClass = Object.class;
            isGenericValue = true;
        } else {
            this.elementClass = null;
        }
        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(field.getName());
        isGenericTypeValue = isGenericValue;
        if(elementClass != null && !isGenericValue) {
            ISchemaValue.assertValueType(elementClass, fieldPath);
        }
        ISchemaMapValue.assertCollectionOrMapValue(elementClass,fieldPath);


        isAbstractValue = elementClass != null && elementClass.isInterface() || Modifier.isAbstract(elementClass.getModifiers());
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

    @Override
    public boolean isGenericValue() {
        return isGenericTypeValue;
    }

    @Override
    public boolean isAbstractType() {
        return isAbstractValue;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }

    @Override
    public String targetPath() {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    @Override
    public boolean isIgnoreError() {
        ObtainTypeValueInvoker obtainTypeValueInvoker = getObtainTypeValueInvoker();
        return obtainTypeValueInvoker != null && obtainTypeValueInvoker.isIgnoreError();
    }

    @SuppressWarnings("unchecked")



    @Override
    public ISchemaNode copyNode() {
        return new SchemaFieldMap(parentsTypeSchema, field, path);
    }

    @Override
    public _SchemaType getNodeType() {
        return _SchemaType.MAP_FIELD;
    }
}
