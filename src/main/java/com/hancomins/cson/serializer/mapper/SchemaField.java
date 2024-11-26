package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.serializer.CSONValue;
import com.hancomins.cson.util.DataConverter;

import java.lang.reflect.Field;
import java.util.Map;

public abstract class SchemaField extends SchemaValueAbs implements ObtainTypeValueInvokerGetter {

    final Field field;
    final String fieldName;

    final String comment;
    final String afterComment;
    private final boolean isStatic;

    final ObtainTypeValueInvoker obtainTypeValueInvoker;

    //private final boolean isMapField;


    SchemaField(ClassSchema parentsTypeSchema, Field field, String path) {
        super(parentsTypeSchema, path, field.getType(), field.getGenericType());
        this.field = field;
        this.fieldName = field.getName();
        field.setAccessible(true);
        this.isStatic = java.lang.reflect.Modifier.isStatic(field.getModifiers());

        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(fieldName);


        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        if(csonValue != null) {
            String comment = csonValue.comment();
            String afterComment = csonValue.commentAfterKey();
            this.comment = comment.isEmpty() ? null : comment;
            this.afterComment = afterComment.isEmpty() ? null : afterComment;
            ISchemaValue.assertValueType(field.getType(), this.getType(), field.getDeclaringClass().getName() + "." + field.getName() );
        } else {
            this.comment = null;
            this.afterComment = null;
        }


    }

    @Override
    public void setParentId(int parentId) {
        if(parentId == 11) {
            System.out.println("SchemaField.setParentId");
        }
        super.setParentId(parentId);
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
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
    Object onGetValue(Object parentValue) {
        Object parent = null;
        if(!isStatic) parent = parentValue;
        try {
            Object value = field.get(parent);
            if(isEnum && value != null) {
                return value.toString();
            }
            return value;
        } catch (IllegalAccessException e) {
            // todo: 상황에 따라서 예외처리를 다르게 해야할 수도 있음.
            e.printStackTrace();
            return null;
        }
    }



    @SuppressWarnings("unchecked")
    @Override
    void onSetValue(Object parentValue, Object value) {
        Object parent = null;
        if(!isStatic) parent = parentValue;

        try {
            if(isEnum) {
                try {
                    //noinspection rawtypes
                    value = Enum.valueOf((Class<Enum>) valueTypeClass, value.toString());
                } catch (Exception e) {
                    value = null;
                }
            }
            if(value != null && !valueTypeClass.isAssignableFrom(value.getClass()) ) {
                value = DataConverter.convertValue(valueTypeClass, value);
                field.set(parent, value);
            } else {
                field.set(parent, value);
            }
        } catch (IllegalAccessException e) {
            // todo: 상황에 따라서 예외처리를 다르게 해야할 수도 있음.
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }


    /*



    @Override
    void onSetValue(Object parent, short value) {
        if(isStatic) parent = null;
        try {
            field.setShort(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, int value) {
        if(isStatic) parent = null;
        try {
            field.setInt(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, long value) {
        if(isStatic) parent = null;
        try {
            field.setLong(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, float value) {
        if(isStatic) parent = null;
        try {
            field.setFloat(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, double value) {
        if(isStatic) parent = null;
        try {
            field.setDouble(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, boolean value) {
        if(isStatic) parent = null;
        try {
            field.setBoolean(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, char value) {
        if(isStatic) parent = null;
        try {
            field.setChar(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }

    @Override
    void onSetValue(Object parent, byte value) {
        if(isStatic) parent = null;
        try {
            field.setByte(parent, value);
        } catch (IllegalAccessException e) {
            throw new CSONSerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }

    */



    @Override
    public String toString() {
        return getId() + "<" + getType() + ">"; /*"FieldRack{" +
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