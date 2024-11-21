package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.CSONObject;
import com.hancomins.cson.serializer.CSONValue;
import com.hancomins.cson.serializer.CSONValueGetter;
import com.hancomins.cson.serializer.CSONValueSetter;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

abstract class SchemaValueAbs implements ISchemaNode, ISchemaValue {

    private final int id = LAST_ID.getAndIncrement();
    protected int parentID = -1;

    final ClassSchema parentsTypeSchema;
    final ClassSchema objectTypeSchema;

    final String path;
    private SchemaType type;

    private final boolean isPrimitive;
    final boolean isEnum;

    //private final boolean isMapField;

    private SchemaValueAbs parentFieldRack;
    final Class<?> valueTypeClass;

    private final ArrayList<SchemaValueAbs> allSchemaValueAbsList = new ArrayList<>();


    static SchemaValueAbs of(ClassSchema typeSchema, Field field) {
        int modifiers = field.getModifiers();
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        // 0.9.29 /////////
        if(Modifier.isFinal(modifiers)) {
            if(csonValue == null) {
                return null;
            }
            throw new CSONSerializerException("@CSONValue field cannot be final. (path: " + typeSchema.getType().getName() + "." + field.getName() + ")");
        }
        // 0.9.29 /////////
        String key = field.getName();
        if(csonValue != null) {
            if(csonValue.ignore()) return null;
            key = csonValue.key();
            if (key == null || key.isEmpty()) key = csonValue.value();
            if (key == null || key.isEmpty()) key = field.getName();
        } else if(typeSchema.isExplicit() || !ISchemaValue.serializable(field.getType())) {
            return null;
        }

        SchemaValueAbs schemaValue;
        if(Collection.class.isAssignableFrom(field.getType())) {
            schemaValue = new SchemaFieldArray(typeSchema, field, key);
        } else if(Map.class.isAssignableFrom(field.getType())) {
            schemaValue = new SchemaFieldMap(typeSchema, field, key);
        }
        else {
            schemaValue = new SchemaFieldNormal(typeSchema, field, key);
        }

        return schemaValue;
    }

    static SchemaValueAbs of(ClassSchema typeSchema, Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);
        if(setter == null && getter == null) return null;
        if(SetterGetterSchemaUseCollection.isCollectionTypeParameterOrReturns(method)) {
            return new SetterGetterSchemaUseCollection(typeSchema, method);
        }
        else if(SetterGetterSchemaUseMap.isMapTypeParameterOrReturns(method)) {
            return new SetterGetterSchemaUseMap(typeSchema, method);
        }
        return new SchemaMethod(typeSchema, method);
    }


    public boolean isDeclaredType(ClassSchema typeSchema) {
        return typeSchema == parentsTypeSchema;
    }


    boolean appendDuplicatedSchemaValue(SchemaValueAbs node) {

        if(node instanceof ISchemaArrayValue && !(this instanceof ISchemaArrayValue) ||
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


    ClassSchema getClassSchema() {
        return objectTypeSchema;
    }


    Object newInstance() {
        if(objectTypeSchema == null) return null;
        return objectTypeSchema.newInstance();
    }



    SchemaValueAbs(ClassSchema parentsTypeSchema, String path, Class<?> valueTypeClass, Type genericType) {

        this.path = path;
        this.valueTypeClass = valueTypeClass;
        this.parentsTypeSchema = parentsTypeSchema;
        this.isEnum = valueTypeClass.isEnum();

        SchemaType type = SchemaType.Object;
        if(genericType instanceof TypeVariable && parentsTypeSchema != null) {
            TypeVariable typeVariable = (TypeVariable)genericType;
            if(parentsTypeSchema.containsGenericType(typeVariable.getName())) {
                type = SchemaType.GenericType;
            }
        } else {
            type = SchemaType.of(valueTypeClass);
        }
        this.type = type;

        if(this.type == SchemaType.Object || this.type == SchemaType.AbstractObject) {
            try {
                this.objectTypeSchema = ClassSchemaMap.getInstance().getTypeInfo(valueTypeClass);
            } catch (CSONSerializerException e) {
                throw new CSONSerializerException("A type that cannot be used as a serialization object : " + valueTypeClass.getName() + ". (path: " + parentsTypeSchema.getType().getName() + "." + path + ")", e);
            }
        }
        else {
            this.objectTypeSchema = null;
        }

        this.isPrimitive = valueTypeClass.isPrimitive();
        this.allSchemaValueAbsList.add(this);
    }




    final SchemaType types() {
        return type;
    }

    void changeType(SchemaType type) {
        this.type = type;
    }

    boolean isPrimitive() {
        return isPrimitive;
    }



    final SchemaType getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getParentId() {
        return parentID;
    }

    @Override
    public void setParentId(int parentId) {
        this.parentID = parentId;
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
    public Object getValue(Map<Integer, Object> parentMap) {
        Object value = null;

        int index = 0;
        int size = this.allSchemaValueAbsList.size();

        while(value == null && index < size) {
            SchemaValueAbs duplicatedSchemaValueAbs = this.allSchemaValueAbsList.get(index);

            value = duplicatedSchemaValueAbs.onGetValue(parentMap);
            if(value != null && duplicatedSchemaValueAbs.getType() == SchemaType.GenericType) {
                SchemaType inType = SchemaType.of(value.getClass());
                if(SchemaType.isSingleType(inType) || SchemaType.isCsonType(inType)) {
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
    public void setValue(Map<Integer, Object> parentMap, Object value) {
        allSchemaValueAbsList.forEach(schemaValueAbs -> schemaValueAbs.onSetValue(parentMap, value));


        //onSetValue(parent, value);


    }


    abstract Object onGetValue(Map<Integer, Object> parentsMap);

    abstract void onSetValue(Map<Integer, Object> parentsMap, Object value);



    /*
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
    }*/


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
