package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONElement;

import java.util.Map;

public interface ISchemaValue extends ISchemaNode {

    Object getValue(Map<Integer, Object> parentMap);

    void setValue(Map<Integer, Object>  parentMap, Object value);

    String getComment();
    String getAfterComment();

    boolean isAbstractType();

    static void assertValueType(Class<?> valueType, String parentPath) {
        assertValueType(valueType, SchemaType.of(valueType), parentPath);
    }

    static void assertValueType(Class<?> valueType, SchemaType type, String parentPath) {
        if(CSONElement.class.isAssignableFrom(valueType)) {
            return;
        }

        if(valueType.isArray() && type != SchemaType.ByteArray) {
            if(parentPath != null) {
                throw new CSONObjectException("Array type '" + valueType.getName() + "' is not supported");
            } else  {
                throw new CSONObjectException("Array type '" + valueType.getName() + "' of field '" + parentPath + "' is not supported");
            }
        }
        /*if(type == Types.Object && valueType.getAnnotation(CSON.class) == null)  {
            if(parentPath != null) {
                throw new CSONObjectException("Object type '" + valueType.getName() + "' is not annotated with @CSON");
            } else  {
                throw new CSONObjectException("Object type '" + valueType.getName() + "' of field '" + parentPath + "' is not annotated with @CSON");
            }
        }*/
    }

    // 0.9.29
    static boolean serializable(Class<?> valueType) {
        if(CSONElement.class.isAssignableFrom(valueType)) {
            return true;
        }
        SchemaType type = SchemaType.of(valueType);
        return !valueType.isArray() || type == SchemaType.ByteArray;
    }

}
