package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.util.DataConverter;

import java.lang.reflect.*;

import java.util.List;


class SchemaFieldMap extends SchemaField implements ISchemaMapValue {


    private final List<GenericItem> genericItems;
    protected final SchemaType valueSchemaType;
    private final Class<?> endpointValueTypeClass;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;



    SchemaFieldMap(ClassSchema parentsTypeSchema, Field field, String path) {
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
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaMapValue)) {
            return false;
        }
        ISchemaMapValue mapValue = (ISchemaMapValue)schemaValueAbs;
        if(this.getEndpointValueType() != ((ISchemaMapValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }


    @Override
    public Class<?> getEndpointValueType() {
        return endpointValueTypeClass;
    }

    @Override
    public Object newInstance() {
        return genericItems.get(0).newInstance();
    }

    @Override
    public boolean isAbstractType() {
        return false;
    }

    @Override
    public Object convertValue(Object value) {
        Class<?> elementType = getEndpointValueType();
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
        return new SchemaFieldMap(parentsTypeSchema, field, path);
    }


}
