package com.clipsoft.cson.serializer;

import java.lang.reflect.Field;

public abstract class SchemaField extends SchemaValueAbs {

    final Field field;
    final String fieldName;

    final String comment;
    final String afterComment;

    //private final boolean isMapField;


    SchemaField(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, path, field.getType());
        this.field = field;
        this.fieldName = field.getName();
        field.setAccessible(true);


        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        String comment = csonValue.comment();
        String afterComment = csonValue.commentAfterKey();
        this.comment = comment.isEmpty() ? null : comment;
        this.afterComment = afterComment.isEmpty() ? null : afterComment;

        ISchemaValue.assertValueType(field.getType(), field.getDeclaringClass().getName() + "." + field.getName() );
    }





    @Override
    public String getComment() {
        return comment;
    }
    @Override
    public String getAfterComment() {
        return afterComment;
    }


    Field getField() {
        return field;
    }


    @Override
    Object onGetValue(Object parent) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    void onSetValue(Object parent, Object value) {
        try {
            field.set(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    void onSetValue(Object parent, short value) {
        try {
            field.setShort(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    void onSetValue(Object parent, int value) {
        try {
            field.setInt(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    void onSetValue(Object parent, long value) {
        try {
            field.setLong(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    void onSetValue(Object parent, float value) {
        try {
            field.setFloat(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    void onSetValue(Object parent, double value) {
        try {
            field.setDouble(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    void onSetValue(Object parent, boolean value) {
        try {
            field.setBoolean(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    void onSetValue(Object parent, char value) {
        try {
            field.setChar(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    void onSetValue(Object parent, byte value) {
        try {
            field.setByte(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    @Override
    public String toString() {
        return getId() + ""; /*"FieldRack{" +
                "id=" + id +
                ", field=" + field +
                ", path='" + path + '\'' +
                ", isPrimitive=" + isPrimitive +
                ", isByteArray=" + isByteArray +
                ", typeElement=" + typeElement +
                ", fieldType=" + fieldType +
                ", type=" + type +
                ", parentFieldRack=" + parentFieldRack +
                '}';*/
    }



}
