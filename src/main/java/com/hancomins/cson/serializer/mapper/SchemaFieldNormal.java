package com.hancomins.cson.serializer.mapper;



import java.lang.reflect.Field;

public class SchemaFieldNormal extends SchemaField {


    protected SchemaFieldNormal(ClassSchema typeSchema, Field field, String path) {
        super(typeSchema, field, path);
    }


    public SchemaFieldNormal copy() {
        SchemaFieldNormal fieldRack = new SchemaFieldNormal(parentsTypeSchema, field, path);
        fieldRack.setParentFiled(getParentField());
        return fieldRack;
    }



    @Override
    public ISchemaNode copyNode() {
        SchemaFieldNormal fieldRack = copy();
        return fieldRack;
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
    public boolean isAbstractType() {
        return types() == SchemaType.AbstractObject;
    }

    /*

    @Override
    public Object newInstance(CSONElement csonElement) {
        String fieldName = getField().getName();
        /*if(type == Types.Object) {
            ObtainTypeValueInvoker rack = parentsTypeSchema.findObjectObrainorRack(fieldName);
            if(rack != null) {
                rack.obtain(csonElement);

            }
        }*/




}
