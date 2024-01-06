package com.clipsoft.cson.serializer;


import com.clipsoft.cson.CSONElement;

import java.lang.reflect.Field;

public class SchemaFieldNormal extends SchemaField {


    protected SchemaFieldNormal(TypeElement typeElement, Field field, String path) {
        super(typeElement, field, path);

        if(this.type != Types.CSONElement &&  this.type == Types.Object && getField().getType().getAnnotation(CSON.class) == null)  {
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



}
