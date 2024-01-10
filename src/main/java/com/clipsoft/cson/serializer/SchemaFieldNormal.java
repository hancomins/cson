package com.clipsoft.cson.serializer;


import com.clipsoft.cson.CSONElement;
import com.clipsoft.cson.CSONObject;

import java.lang.reflect.Field;

public class SchemaFieldNormal extends SchemaField {


    protected SchemaFieldNormal(TypeElement typeElement, Field field, String path) {
        super(typeElement, field, path);

        if(this.types() != Types.CSONObject && this.types() != Types.CSONArray &&  this.types() == Types.Object && getField().getType().getAnnotation(CSON.class) == null)  {
            throw new CSONSerializerException("Object type " + this.field.getType().getName() + " is not annotated with @CSON");
        }
    }


    public SchemaFieldNormal copy() {
        SchemaFieldNormal fieldRack = new SchemaFieldNormal(parentsTypeElement, field, path);
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
        return obtainTypeValueInvoker != null && obtainTypeValueInvoker.ignoreError;
    }

    /*

    @Override
    public Object newInstance(CSONElement csonElement) {
        String fieldName = getField().getName();
        /*if(type == Types.Object) {
            TypeElement.ObtainTypeValueInvoker rack = parentsTypeElement.findObjectObrainorRack(fieldName);
            if(rack != null) {
                rack.obtain(csonElement);

            }
        }*/




}
