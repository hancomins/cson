package com.hancomins.cson.serializer.mapper;


import java.lang.reflect.Field;
import java.util.List;

class SchemaFieldArray extends SchemaField implements ISchemaArrayValue {

    private final List<CollectionItem> collectionBundles;
    protected final SchemaType valueType;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;



    protected SchemaFieldArray(ClassSchema classSchema, Field field, String path) {
        super(classSchema, field, path);
        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        this.collectionBundles = CollectionItem.buildCollectionItemsByField(field);


        obtainTypeValueInvoker = classSchema.findObtainTypeValueInvoker(field.getName());

        SchemaArrayInitializeResult result = initialize(collectionBundles, classSchema, fieldPath);
        valueType = result.getValueType();
        setObjectTypeSchema(result.getObjectTypeSchema());


    }

    static SchemaArrayInitializeResult initialize(List<CollectionItem> collectionBundles, ClassSchema classSchema, String path) {
        CollectionItem collectionItem = collectionBundles.get(collectionBundles.size() - 1);
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

        return new SchemaArrayInitializeResult(valueType, objectTypeSchema);

    }

    protected static class SchemaArrayInitializeResult  {
        private final SchemaType valueType;
        private final ClassSchema objectTypeSchema;

        SchemaArrayInitializeResult(SchemaType valueType, ClassSchema objectTypeSchema) {
            this.valueType = valueType;
            this.objectTypeSchema = objectTypeSchema;
        }

        SchemaType getValueType() {
            return valueType;
        }

        ClassSchema getObjectTypeSchema() {
            return objectTypeSchema;
        }
    }



    @Override
    public List<CollectionItem> getCollectionItems() {
        return collectionBundles;
    }

    @Override
    public boolean isAbstractType() {
        return valueType == SchemaType.AbstractObject;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }


    @Override
    public SchemaType getEndpointValueType() {
        return valueType;
    }

    @Override
    public Class<?> getEndpointValueTypeClass() {
        return collectionBundles.get(collectionBundles.size() - 1).getValueClass();
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
