package com.hancomins.cson.internal.serializer;


import java.lang.reflect.Field;
import java.util.*;

class SchemaFieldArray extends SchemaField implements ISchemaArrayValue {

    //private final Types valueType;

    private final List<CollectionItems> collectionBundles;
    protected final Types ValueType;
    private final TypeSchema objectValueTypeSchema;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;



    protected SchemaFieldArray(TypeSchema typeSchema, Field field, String path) {
        super(typeSchema, field, path);
        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        this.collectionBundles = ISchemaArrayValue.getGenericType(field.getGenericType(), fieldPath);


        obtainTypeValueInvoker = typeSchema.findObtainTypeValueInvoker(field.getName());

        CollectionItems collectionItems = this.collectionBundles.get(collectionBundles.size() - 1);
        Class<?> valueClass = collectionItems.getValueClass();
        Types valueType = Types.of(valueClass);

        if(collectionItems.isGeneric()) {
            if( !typeSchema.containsGenericType(collectionItems.getGenericTypeName())) {
                throw new CSONSerializerException("Collection generic type is already defined. (path: " + fieldPath + ")");
            }
            valueType = Types.GenericType;
        } else if(collectionItems.isAbstractType()) {
            valueType = Types.AbstractObject;
        }
        ValueType = valueType;

        if (ValueType == Types.Object || valueType == Types.AbstractObject ) {
            objectValueTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeSchema = null;
        }

    }


    @Override
    public TypeSchema getObjectValueTypeElement() {
        return objectValueTypeSchema;
    }

    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }

    @Override
    public boolean isAbstractType() {
        return ValueType == Types.AbstractObject;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }


    @Override
    public Types getEndpointValueType() {
        return ValueType;
    }

    @Override
    public ISchemaNode copyNode() {
        return new SchemaFieldArray(parentsTypeSchema, field, path);
    }

    @Override
    public Object newInstance() {
        return collectionBundles.get(0).newInstance();
    }


    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaArrayValue)) {
            return false;
        }
        if(!ISchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((ISchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        if(this.getEndpointValueType() != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }


    @Override
    public String targetPath() {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    @Override
    public boolean isIgnoreError() {
        return obtainTypeValueInvoker != null && obtainTypeValueInvoker.isIgnoreError();
    }


}
