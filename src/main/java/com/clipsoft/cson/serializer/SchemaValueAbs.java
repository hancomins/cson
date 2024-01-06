package com.clipsoft.cson.serializer;

import com.clipsoft.cson.CSONElement;
import com.clipsoft.cson.CSONObject;

import java.lang.reflect.Method;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SchemaValueAbs implements ISchemaNode, ISchemaValue {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    final TypeElement parentsTypeElement;
    final TypeElement objectTypeElement;

    final String path;
    final Types type;

    private final boolean isPrimitive;

    //private final boolean isMapField;

    private SchemaValueAbs parentFieldRack;
    final Class<?> valueTypeClass;

    private final ArrayList<SchemaValueAbs> allSchemaValueAbsList = new ArrayList<>();


    static SchemaValueAbs of(TypeElement typeElement, Field field) {
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        if(csonValue == null) return null;
        String key = csonValue.key();
        if(key == null || key.isEmpty()) key = csonValue.value();
        if(key == null || key.isEmpty()) key = field.getName();

        if(Collection.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldArray(typeElement, field, key);
        } else if(Map.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldMap(typeElement, field, key);
        }
        else {
            return new SchemaFieldNormal(typeElement, field, key);
        }
    }

    static SchemaValueAbs of(TypeElement typeElement, Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);
        if(setter == null && getter == null) return null;
        if(SchemaMethodForArrayType.isCollectionTypeParameterOrReturns(method)) {
            return new SchemaMethodForArrayType(typeElement, method);
        }
        else if(SchemaMethodForMapType.isMapTypeParameterOrReturns(method)) {
            return new SchemaMethodForMapType(typeElement, method);
        }
        return new SchemaMethod(typeElement, method);
    }


    boolean appendDuplicatedSchemaValue(SchemaValueAbs node) {
        if(node.parentsTypeElement != this.parentsTypeElement) {
            return false;
        }
        else if(node instanceof ISchemaArrayValue && !(this instanceof ISchemaArrayValue) ||
                !(node instanceof ISchemaArrayValue) && this instanceof ISchemaArrayValue) {
            //TODO 예외 발생 시켜야한다.
            return false;
        }
        else if(node instanceof ISchemaArrayValue && this instanceof ISchemaArrayValue) {
            ISchemaArrayValue nodeArray = (ISchemaArrayValue) node;
            ISchemaArrayValue thisArray = (ISchemaArrayValue) this;
            if(nodeArray.getCollectionItems().size() != thisArray.getCollectionItems().size()) {
                //TODO 예외 발생 시켜야한다.
            }
        }


        this.allSchemaValueAbsList.add(node);
        return true;
    }

    @SuppressWarnings("unchecked")
    <T extends SchemaValueAbs> List<T> getAllSchemaValueList() {

        return (List<T>) this.allSchemaValueAbsList;
    }


    SchemaValueAbs(TypeElement parentsTypeElement, String path, Class<?> valueTypeClass) {

        this.path = path;
        this.valueTypeClass = valueTypeClass;
        this.parentsTypeElement = parentsTypeElement;
        this.type = Types.of(valueTypeClass);


        if(this.type == Types.Object) {
            this.objectTypeElement = TypeElements.getInstance().getTypeInfo(valueTypeClass);
        }
        else {
            this.objectTypeElement = null;
        }

        this.isPrimitive = valueTypeClass.isPrimitive();
        this.allSchemaValueAbsList.add(this);
    }


    Object newInstance() {
        if(objectTypeElement == null) return null;
        return objectTypeElement.newInstance();
    }


    boolean isPrimitive() {
        return isPrimitive;
    }


    final Types getType() {
        return type;
    }

    final int getId() {
        return id;
    }

    final String getPath() {
        return path;
    }

    final Class<?> getValueTypeClass() {
        return valueTypeClass;
    }

    @SuppressWarnings("unchecked")
    final <T extends SchemaValueAbs> T getParentField() {
        return (T) parentFieldRack;
    }



    final void setParentFiled(SchemaValueAbs parent) {
        this.parentFieldRack = parent;
    }


    @Override
    public Object getValue(Object parent) {
        Object value = null;
        int index = this.allSchemaValueAbsList.size() - 1;
        while(value == null && index > -1) {
            SchemaValueAbs duplicatedSchemaValueAbs = this.allSchemaValueAbsList.get(index);
            value = duplicatedSchemaValueAbs.onGetValue(parent);
            if(value == null) {
                index--;
                continue;
            }
            if(!this.equalsValueType(duplicatedSchemaValueAbs)) {
                if(this instanceof ISchemaArrayValue || this instanceof ISchemaMapValue) {
                    return value;
                } else {
                    value = Utils.convertValue(value, duplicatedSchemaValueAbs.type);
                }
            }
            index--;
        }
        return value;
    }

    @Override
    public void setValue(Object parent, Object value) {
        onSetValue(parent, value);
    }


    abstract Object onGetValue(Object parent);

    abstract void onSetValue(Object parent, Object value);




    void onSetValue(Object parent, short value) {
        onSetValue(parent, Short.valueOf(value));
    }

    void onSetValue(Object parent, int value) {
         onSetValue(parent, Integer.valueOf(value));
    }

    void onSetValue(Object parent, long value) {
         onSetValue(parent, Long.valueOf(value));
    }

    void onSetValue(Object parent, float value) {
         setValue(parent, Float.valueOf(value));
    }

    void onSetValue(Object parent, double value) {
         onSetValue(parent,Double.valueOf(value));
    }

    void onSetValue(Object parent, boolean value) {
         onSetValue(parent,Boolean.valueOf(value));
    }

    void onSetValue(Object parent, char value) {
         onSetValue(parent,Character.valueOf(value));
    }

    void onSetValue(Object parent, byte value) {
         onSetValue(parent,Byte.valueOf(value));
    }


    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(this.valueTypeClass == null) return false;
        return this.valueTypeClass.equals(schemaValueAbs.getValueTypeClass());
    }


    @Override
    public String toString() {
        return id + ""; /*"FieldRack{" +
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
