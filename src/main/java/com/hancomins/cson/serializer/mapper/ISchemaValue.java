package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONElement;

public interface ISchemaValue extends ISchemaNode {

    Object getValue(Object parent);

    void setValue(Object parent, Object value);

    String getComment();
    String getAfterComment();

    boolean isAbstractType();

    static void assertValueType(Class<?> valueType, String parentPath) {
        assertValueType(valueType, Types.of(valueType), parentPath);
    }

    static void assertValueType(Class<?> valueType, Types type, String parentPath) {
        if(CSONElement.class.isAssignableFrom(valueType)) {
            return;
        }

        if(valueType.isArray() && type != Types.ByteArray) {
            if(parentPath != null) {
                throw new CSONObjectException("Array type '" + valueType.getName() + "' is not supported");
            } else  {
                throw new CSONObjectException("Array type '" + valueType.getName() + "' of field '" + parentPath + "' is not supported");
            }
        }
        if(type == Types.Object && valueType.getAnnotation(CSON.class) == null)  {
            if(parentPath != null) {
                throw new CSONObjectException("Object type '" + valueType.getName() + "' is not annotated with @CSON");
            } else  {
                throw new CSONObjectException("Object type '" + valueType.getName() + "' of field '" + parentPath + "' is not annotated with @CSON");
            }
        }
    }

}
