package com.clipsoft.cson.serializer;

import com.clipsoft.cson.CSONElement;

public interface ISchemaValue extends ISchemaNode {

    Object getValue(Object parent);

    void setValue(Object parent, Object value);

    String getComment();
    String getAfterComment();

    boolean isAbstractType();

    static void assertValueType(Class<?> valueType, String parentPath) {
        assertValueType(valueType, Types.of(valueType), parentPath);
    }

    // 0.9.29
    static boolean serializable(Class<?> valueType) {
        if(CSONElement.class.isAssignableFrom(valueType)) {
            return true;
        }
        Types type = Types.of(valueType);
        if(valueType.isArray() && type != Types.ByteArray) {
            return false;
        }
        return type != Types.Object || valueType.getAnnotation(CSON.class) != null;
    }

    static void assertValueType(Class<?> valueType,Types type, String parentPath) {
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
