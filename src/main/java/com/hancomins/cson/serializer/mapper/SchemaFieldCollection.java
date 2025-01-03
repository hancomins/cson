package com.hancomins.cson.serializer.mapper;


import java.lang.reflect.Field;
import java.util.List;

class SchemaFieldCollection extends SchemaField implements ISchemaArrayValue {

    private final List<GenericItem> genericItems;
    protected final SchemaType valueSchemaType;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;
    private final Class<?> endpointValueTypeClass;

    protected SchemaFieldCollection(ClassSchema parentsTypeSchema, Field field, String path) {
        super(parentsTypeSchema, field, path);
        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        this.genericItems = GenericItem.analyzeField(field);
        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(field.getName());
        endpointValueTypeClass = this.genericItems.get(this.genericItems.size() - 1).getValueType();
        valueSchemaType = SchemaType.of(endpointValueTypeClass);
        if (valueSchemaType == SchemaType.Object || valueSchemaType == SchemaType.AbstractObject ) {
            ClassSchema valueClassSchema = ClassSchemaMap.getInstance().getClassSchema(endpointValueTypeClass);
            setObjectTypeSchema(valueClassSchema);
        }
    }



    @Override
    public List<GenericItem> getCollectionItems() {
        return genericItems;
    }

    @Override
    public boolean isAbstractType() {
        return valueSchemaType == SchemaType.AbstractObject;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }


    @Override
    public SchemaType getEndpointValueType() {
        return valueSchemaType;
    }

    @Override
    public Class<?> getEndpointValueTypeClass() {
        return endpointValueTypeClass;
    }

    @Override
    public ISchemaNode copyNode() {
        return new SchemaFieldCollection(parentsTypeSchema, field, path);
    }

    @Override
    public Object newInstance() {
        return genericItems.get(0).newInstance();
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
