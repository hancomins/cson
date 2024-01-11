package com.clipsoft.cson.serializer;


import com.clipsoft.cson.CSONObject;

import java.lang.reflect.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SchemaValueAbs implements ISchemaNode, ISchemaValue {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    final TypeElement parentsTypeElement;
    final TypeElement objectTypeElement;

    final String path;
    private Types type;

    private final boolean isPrimitive;

    //private final boolean isMapField;

    private SchemaValueAbs parentFieldRack;
    final Class<?> valueTypeClass;

    private final ArrayList<SchemaValueAbs> allSchemaValueAbsList = new ArrayList<>();


    static SchemaValueAbs of(TypeElement typeElement, Field field) {
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        int modifiers = field.getModifiers();
        if(csonValue == null) return null;
        if(Modifier.isFinal(modifiers)) {
            throw new CSONSerializerException("@CSONValue field cannot be final. (path: " + typeElement.getType().getName() + "." + field.getName() + ")");
        }
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
                // TODO 이건 좀 고민중...
                //return false;
            }
        }
        this.allSchemaValueAbsList.add(node);
        return true;
    }

    @SuppressWarnings("unchecked")
    <T extends SchemaValueAbs> List<T> getAllSchemaValueList() {

        return (List<T>) this.allSchemaValueAbsList;
    }




    Object newInstance() {
        if(objectTypeElement == null) return null;
        return objectTypeElement.newInstance();
    }



    SchemaValueAbs(TypeElement parentsTypeElement, String path, Class<?> valueTypeClass, Type genericType) {

        this.path = path;
        this.valueTypeClass = valueTypeClass;
        this.parentsTypeElement = parentsTypeElement;


        Types type = Types.Object;
        if(genericType instanceof TypeVariable && parentsTypeElement != null) {
            TypeVariable typeVariable = (TypeVariable)genericType;
            if(parentsTypeElement.containsGenericType(typeVariable.getName())) {
                type = Types.GenericType;
            }
        } else {
            type = Types.of(valueTypeClass);
        }
        this.type = type;

        if(this.type == Types.Object) {
            try {
                this.objectTypeElement = TypeElements.getInstance().getTypeInfo(valueTypeClass);
            } catch (CSONSerializerException e) {
                throw new CSONSerializerException("A type that cannot be used as a serialization object : " + valueTypeClass.getName() + ". (path: " + parentsTypeElement.getType().getName() + "." + path + ")", e);
            }
        }
        else {
            this.objectTypeElement = null;
        }

        this.isPrimitive = valueTypeClass.isPrimitive();
        this.allSchemaValueAbsList.add(this);
    }




    final Types types() {
        return type;
    }

    void changeType(Types type) {
        this.type = type;
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

        int index = 0;
        int size = this.allSchemaValueAbsList.size();

        while(value == null && index < size) {
            SchemaValueAbs duplicatedSchemaValueAbs = this.allSchemaValueAbsList.get(index);

            value = duplicatedSchemaValueAbs.onGetValue(parent);
            if(value != null && duplicatedSchemaValueAbs.getType() == Types.GenericType) {
                Types inType = Types.of(value.getClass());
                if(Types.isSingleType(inType)) {
                    return value;
                } else {
                    return CSONObject.fromObject(value);
                }
            }

            if(value == null) {
                ++index;
                continue;
            }
            if(!this.equalsValueType(duplicatedSchemaValueAbs)) {
                if(this instanceof ISchemaArrayValue || this instanceof ISchemaMapValue) {
                    return value;
                } else {
                    value = Utils.convertValue(value, duplicatedSchemaValueAbs.type);
                }
            }
            ++index;

        }
        return value;


        /*int index = this.allSchemaValueAbsList.size() - 1;

        while(value == null && index > -1) {
            SchemaValueAbs duplicatedSchemaValueAbs = this.allSchemaValueAbsList.get(index);

            value = duplicatedSchemaValueAbs.onGetValue(parent);
            if(value != null && duplicatedSchemaValueAbs.getType() == Types.GenericType) {
                Types inType = Types.of(value.getClass());
                if(Types.isSingleType(inType)) {
                    return value;
                } else {
                    return CSONObject.fromObject(value);
                }
            }

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
        return value;*/
    }


    /*
    2024.01.08 동일한 path 의 CSONElement 가 여러개 있을 경우 merge 하도록 하는 코드.
     추후 이 것을 구현해야 하는 상황이 생긴다면 주석을 해제하여 사용한다.
    @Override
    public Object getValue(Object parent) {
        Object value = null;
        int index = this.allSchemaValueAbsList.size() - 1;
        boolean doContinue = true;
        CSONElement lastCSONElement = null;
        while(doContinue && index > -1) {
            SchemaValueAbs duplicatedSchemaValueAbs = this.allSchemaValueAbsList.get(index);
            if(type == Types.CSONElement && duplicatedSchemaValueAbs.type != Types.CSONElement) {
                continue;
            }

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
            if(type != Types.CSONElement && value != null) {
                doContinue = false;
            } else if(value instanceof CSONElement) {
                if(lastCSONElement != null) {
                    if(lastCSONElement instanceof  CSONObject && value instanceof  CSONObject) {
                        ((CSONObject) lastCSONElement).merge((CSONObject) value);
                        value = lastCSONElement;
                    }
                    else if(lastCSONElement instanceof CSONArray && value instanceof CSONArray) {
                        ((CSONArray) lastCSONElement).merge((CSONArray) value);
                        value = lastCSONElement;
                    }
                } else {
                    lastCSONElement = (CSONElement) value;
                }
            }
        }
        return value;
    }*/

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
