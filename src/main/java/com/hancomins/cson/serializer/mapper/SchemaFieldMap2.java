package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.util.DataConverter;

import java.lang.reflect.*;
import java.util.List;


class SchemaFieldMap2 extends SchemaField implements ISchemaMapValue {

    private final List<MapItem> mapBundles;
    private final SchemaType valueType;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;


    SchemaFieldMap2(ClassSchema parentsTypeSchema, Field field, String path) {
        super(parentsTypeSchema, field, path);
        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        this.mapBundles = MapItem.buildMapItemsByField(field);

        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(field.getName());

    }


    static SchemaFieldArray.SchemaArrayInitializeResult initialize(List<MapItem> collectionBundles, ClassSchema classSchema, String path) {
        CollectionItem collectionItem = collectionBundles.get(clectionBundles.size() - 1);
        Class<?> valueClass = collectionItem.getValueClass();
        SchemaType schemaValueType = SchemaType.of(valueClass);

        if(collectionItem.isGeneric()) {
            if( !classSchema.containsGenericType(collectionItem.getGenericTypeName())) {
                throw new CSONMapperException("Collection generic type is already defined. (path: " + path + ")");
            }
            schemaValueType = SchemaType.GenericType;
        } else if(collectionItem.isAbstractType()) {
            schemaValueType = SchemaType.AbstractObject;
        }
        SchemaType valueType = schemaValueType;
        ClassSchema objectTypeSchema = null;



        if (valueType == SchemaType.Object || schemaValueType == SchemaType.AbstractObject ) {
            objectTypeSchema = ClassSchemaMap.getInstance().getClassSchema(valueClass);
        }

        return new SchemaFieldArray.SchemaArrayInitializeResult(valueType, objectTypeSchema);

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
            throw new CSONMapperException("Map type " + field.getDeclaringClass().getName() + "." + field.getType().getName() + " has no default constructor.", e);
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
    public Object convertValue(Object value) {
        Class<?> elementType = getElementType();
        return DataConverter.convertValue(elementType, value);
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





    @Override
    public ISchemaNode copyNode() {
        return new SchemaFieldMap2(parentsTypeSchema, field, path);
    }


}
