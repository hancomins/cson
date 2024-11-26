package com.hancomins.cson.serializer.mapper;


import java.lang.reflect.Field;
import java.util.List;

class SchemaFieldArray extends SchemaField implements ISchemaArrayValue {

    //private final Types valueType;

    private final List<CollectionItems> collectionBundles;
    protected final SchemaType ValueType;
    //private final ClassSchema objectValueTypeSchema;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;



    protected SchemaFieldArray(ClassSchema typeSchema, Field field, String path) {
        super(typeSchema, field, path);
        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        this.collectionBundles = ISchemaArrayValue.getGenericType(field.getGenericType(), fieldPath);


        obtainTypeValueInvoker = typeSchema.findObtainTypeValueInvoker(field.getName());

        CollectionItems collectionItems = this.collectionBundles.get(collectionBundles.size() - 1);
        Class<?> valueClass = collectionItems.getValueClass();
        SchemaType valueType = SchemaType.of(valueClass);

        if(collectionItems.isGeneric()) {
            if( !typeSchema.containsGenericType(collectionItems.getGenericTypeName())) {
                throw new CSONSerializerException("Collection generic type is already defined. (path: " + fieldPath + ")");
            }
            valueType = SchemaType.GenericType;
        } else if(collectionItems.isAbstractType()) {
            valueType = SchemaType.AbstractObject;
        }
        ValueType = valueType;

        if (ValueType == SchemaType.Object || valueType == SchemaType.AbstractObject ) {
            setObjectTypeSchema(ClassSchemaMap.getInstance().getClassSchema(valueClass));
        } else {
            setObjectTypeSchema(null);
        }

    }



    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }

    @Override
    public boolean isAbstractType() {
        return ValueType == SchemaType.AbstractObject;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }


    @Override
    public SchemaType getEndpointValueType() {
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

    @Override
    public _SchemaType getNodeType() {
        return _SchemaType.ARRAY_FIELD;
    }

}
