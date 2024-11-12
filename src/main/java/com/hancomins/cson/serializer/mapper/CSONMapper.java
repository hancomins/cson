package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.*;
import com.hancomins.cson.format.*;
import com.hancomins.cson.options.WritingOptions;
import com.hancomins.cson.serializer.CSON;
import com.hancomins.cson.util.DataConverter;
import com.hancomins.cson.util.NullValue;

import java.util.*;

public class CSONMapper {

    private KeyValueDataContainerFactory keyValueDataContainerFactory = null;
    private ArrayDataContainerFactory arrayDataContainerFactory = null;
    


    private CSONMapper() {}

    public static boolean serializable(Class<?> clazz) {
        if(TypeSchemaMap.getInstance().hasTypeInfo(clazz)) {
            return true;
        }
        return clazz.getAnnotation(CSON.class) != null;
    }

    public KeyValueDataContainer toKeyValueDataContainer(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        return serializeTypeElement(typeSchema,obj);
    }

    private KeyValueDataContainer serializeTypeElement(TypeSchema typeSchema, final Object rootObject) {
        Class<?> type = typeSchema.getType();
        /*if(rootObject.getClass() != type) {
            throw new CSONSerializerException("Type mismatch error. " + type.getName() + "!=" + rootObject.getClass().getName());
        }
        else*/ if(rootObject == null) {
            return null;
        }
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        BaseDataContainer csonElement = keyValueDataContainerFactory.create();
        String comment = typeSchema.getComment();
        String commentAfter = typeSchema.getCommentAfter();
        if(comment != null) {
            csonElement.setComment(comment, CommentPosition.HEADER);
        }
        if(commentAfter != null) {
            csonElement.setComment(commentAfter, CommentPosition.FOOTER);
        }
        KeyValueDataContainer root = (KeyValueDataContainer) csonElement;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonElement);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);

        while(iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaObjectNode) {
                schemaNode = (SchemaObjectNode)node;
                iter = schemaNode.keySet().iterator();
                List<SchemaValueAbs> parentschemaField = schemaNode.getParentSchemaFieldList();
                int nullCount = parentschemaField.size();

                // 부모 필드들의 값을 가져온다.
                for(SchemaValueAbs parentSchemaValueAbs : parentschemaField) {
                    int id = parentSchemaValueAbs.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    // 부모 필드의 부모 필드가 없으면 rootObject 에서 값을 가져온다.
                    SchemaField grandschemaField = parentSchemaValueAbs.getParentField();
                    Object parentObj = null;
                    if (grandschemaField == null) {
                        parentObj = parentSchemaValueAbs.getValue(rootObject);
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandschemaField.getId());
                        if(grandObj != null) {
                            parentObj = parentSchemaValueAbs.getValue(grandObj);
                        }
                    }
                    if(parentObj != null) {
                        parentObjMap.put(id, parentObj);
                        nullCount--;
                    }
                }

                if(!schemaNode.isBranchNode() && nullCount > 0) {
                    if(key instanceof String) {
                        ((KeyValueDataContainer)csonElement).put((String) key,null);
                    } else {
                        assert csonElement instanceof ArrayDataContainer;
                        ((ArrayDataContainer)csonElement).set((Integer) key,null);
                    }
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    if(key instanceof String) {
                        KeyValueDataContainer currentObject = ((KeyValueDataContainer)csonElement);
                        Object value = currentObject.get((String)key);

                        if (!(value instanceof BaseDataContainer)) {
                            BaseDataContainer childElement = (schemaNode instanceof SchemaArrayNode) ? arrayDataContainerFactory.create() : keyValueDataContainerFactory.create();
                            currentObject.put((String) key, childElement);
                            currentObject.setComment((String) key, schemaNode.getComment(), CommentPosition.BEFORE_KEY);
                            currentObject.setComment((String) key, schemaNode.getAfterComment(), CommentPosition.AFTER_KEY);
                            csonElement = childElement;
                        }

                    } else {
                        if(!(csonElement instanceof ArrayDataContainer)) {
                            throw new CSONSerializerException("Invalide path. '" + key + "' is not array index." +  "(csonElement is not ArrayDataContainer. csonElement=" + csonElement +  ")");
                        }
                        ArrayDataContainer currentObject = ((ArrayDataContainer)csonElement);
                        ArrayDataContainer currentArray = ((ArrayDataContainer)csonElement);
                        BaseDataContainer childElement = (BaseDataContainer) currentArray.get((Integer) key);
                        if(childElement == null) {
                            childElement = (schemaNode instanceof SchemaArrayNode) ? arrayDataContainerFactory.create() : keyValueDataContainerFactory.create();
                            currentObject.set((int) key, childElement);
                            csonElement = childElement;

                        }
                    }
                    objectSerializeDequeueItems.add(new ObjectSerializeDequeueItem(iter, schemaNode, csonElement));
                }
            }
            else if(node instanceof SchemaFieldNormal || SchemaMethod.isSchemaMethodGetter(node)) {
                SchemaValueAbs schemaValueAbs = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaValueAbs, rootObject);
                if(parent != null) {
                    Object value = schemaValueAbs.getValue(parent);
                    putValueInBaseDataContainer(csonElement, schemaValueAbs, key, value);
                }
            } else if(node instanceof ISchemaMapValue) {
                SchemaValueAbs schemaMap = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaMap, rootObject);
                if(parent != null) {
                    Object value = schemaMap.getValue(parent);
                    if(value != null) {
                        @SuppressWarnings("unchecked")
                        KeyValueDataContainer csonObject = mapObjectToKeyValueDataContainer((Map<String, ?>) value, ((ISchemaMapValue)schemaMap).getElementType());
                        putValueInBaseDataContainer(csonElement, schemaMap, key, csonObject);
                    } else {
                        putValueInBaseDataContainer(csonElement, schemaMap, key, null);
                    }
                }

            }
            else if(node instanceof ISchemaArrayValue) {
                ISchemaArrayValue ISchemaArrayValue = (com.hancomins.cson.serializer.mapper.ISchemaArrayValue)node;
                Object parent = obtainParentObjects(parentObjMap, (SchemaValueAbs) ISchemaArrayValue, rootObject);
                if(parent != null) {
                    Object value = ISchemaArrayValue.getValue(parent);
                    if(value != null) {
                        ArrayDataContainer csonArray = collectionObjectToSONArrayKnownSchema((Collection<?>)value, ISchemaArrayValue);
                        putValueInBaseDataContainer(csonElement, ISchemaArrayValue, key, csonArray);
                    } else {
                        putValueInBaseDataContainer(csonElement, ISchemaArrayValue, key, null);
                    }
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.ISchemaNode;
                csonElement = objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return root;
    }


    private void putValueInBaseDataContainer(BaseDataContainer csonElement, ISchemaValue ISchemaValueAbs, Object key, Object value) {
        if(key instanceof String) {
            ((KeyValueDataContainer) csonElement).put((String) key, value);
            ((KeyValueDataContainer) csonElement).setComment((String) key, ISchemaValueAbs.getComment(), CommentPosition.BEFORE_KEY);
            ((KeyValueDataContainer) csonElement).setComment((String) key, ISchemaValueAbs.getAfterComment(), CommentPosition.AFTER_KEY);
        }
        else {
            if(!(csonElement instanceof ArrayDataContainer)) {
                throw new CSONSerializerException("Invalide path. '" + key + "' is not array index." +  "(csonElement is not ArrayDataContainer. csonElement=" + csonElement +  ")");
            }
            ((ArrayDataContainer)csonElement).set((int)key, value);
            ((ArrayDataContainer)csonElement).setComment((int)key, ISchemaValueAbs.getComment(), CommentPosition.BEFORE_VALUE);
            ((ArrayDataContainer)csonElement).setComment((int)key, ISchemaValueAbs.getAfterComment(), CommentPosition.AFTER_VALUE);
        }
    }

    public KeyValueDataContainer mapToKeyValueDataContainer(Map<String, ?> map) {
        return mapObjectToKeyValueDataContainer(map, null);
    }

    public ArrayDataContainer collectionToArrayDataContainer(Collection<?> collection) {
        return collectionObjectToArrayDataContainer(collection, null);
    }



    public <T> List<T> csonArrayToList(ArrayDataContainer csonArray, Class<T> valueType) {
        return csonArrayToList(csonArray, valueType, null, false, null);
    }

    public <T> List<T> csonArrayToList(ArrayDataContainer csonArray, Class<T> valueType, boolean ignoreError) {
        return csonArrayToList(csonArray, valueType, null, ignoreError, null);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> csonArrayToList(ArrayDataContainer csonArray, Class<T> valueType, WritingOptions<?> writingOptions, boolean ignoreError, T defaultValue) {
        Types types = Types.of(valueType);
        if(valueType.isPrimitive()) {
            if(ignoreError) {
                return null;
            }
            throw new CSONSerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } else if(Collection.class.isAssignableFrom(valueType)) {
            if(ignoreError) {
                return null;
            }
            throw new CSONSerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection.  valueType=" + valueType.getName());
        }  else if(Map.class.isAssignableFrom(valueType)) {
            if(ignoreError) {
                return null;
            }
            throw new CSONSerializerException("valueType is java.util.Map type. Use a class that wraps your Map.  valueType=" + valueType.getName());
        } else if(valueType.isArray() && Types.ByteArray != types) {
            if(ignoreError) {
                return null;
            }
            throw new CSONSerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
        ArrayList<T> result = new ArrayList<T>();
        for(int i = 0, n = csonArray.size(); i < n; ++i) {
            Object value = csonArray.get(i);
            if(value == null) {
                result.add(defaultValue);
            }
            else if(Number.class.isAssignableFrom(valueType)) {
                try {
                    Number no = DataConverter.toBoxingNumberOfType(value, (Class<? extends Number>) valueType);
                    result.add((T) no);
                } catch (NumberFormatException e) {
                    if(ignoreError) {
                        result.add(defaultValue);
                        continue;
                    }
                    throw new CSONSerializerException("valueType is Number type. But value is not Number type. valueType=" + valueType.getName());
                }
            } else if(Boolean.class == valueType) {
                if(value.getClass() == Boolean.class) {
                    result.add((T)value);
                } else {
                    result.add("true".equals(value.toString()) ? (T)Boolean.TRUE : (T)Boolean.FALSE);
                }
            } else if(Character.class == valueType) {
                try {
                    if (value.getClass() == Character.class) {
                        result.add((T) value);
                    } else {
                        result.add((T) (Character) DataConverter.toChar(value));
                    }
                } catch (NumberFormatException e) {
                    if(ignoreError) {
                        result.add(defaultValue);
                        continue;
                    }
                    throw new CSONSerializerException("valueType is Character type. But value is not Character type. valueType=" + valueType.getName());
                }
            } else if(valueType == String.class) {
                if(writingOptions != null && value instanceof BaseDataContainer) {
                    CSONElement csonElement = toCSONElement(value);
                    result.add((T)(csonElement.toString(  writingOptions)));
                } else {
                    result.add((T) value.toString());
                }
            } else if(value instanceof KeyValueDataContainer && CSONMapper.serializable(valueType)) {
                try {
                    value = fromKeyValueDataContainer((KeyValueDataContainer)value, valueType);
                } catch (CSONException e) {
                    if(ignoreError) {
                        result.add(defaultValue);
                        continue;
                    }
                    throw e;
                }
                result.add((T)value);
            }
        }
        return result;
    }


    private KeyValueDataContainer mapObjectToKeyValueDataContainer(Map<String, ?> map, Class<?> valueType) {
        KeyValueDataContainer csonObject = keyValueDataContainerFactory.create();
        Set<? extends Map.Entry<String, ?>> entries = map.entrySet();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Map.Entry<String, ?> entry : entries) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if(value != null && valueType == null) {
                valueType = value.getClass();
                ISchemaValue.assertValueType(valueType, null);
                types = Types.of(valueType);
                //noinspection DataFlowIssue
                if(!(key instanceof String)) {
                    throw new CSONSerializerException("Map key type is not String. Please use String key.");
                }
            }
            if(value instanceof Collection<?>) {
                ArrayDataContainer csonArray = collectionObjectToArrayDataContainer((Collection<?>)value, null);
                csonObject.put(key, csonArray);
            } else if(value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                KeyValueDataContainer childObject = mapObjectToKeyValueDataContainer((Map<String, ?>)value, null);
                csonObject.put(key, childObject);
            } else if(types == Types.Object) {
                if(value == null) {
                    csonObject.put(key, null);
                }
                else {
                    Types type = Types.of(value.getClass());
                    if(Types.isSingleType(type)) {
                        csonObject.put(key, value);
                    } else {
                        KeyValueDataContainer childObject = toKeyValueDataContainer(value);
                        csonObject.put(key, childObject);
                    }
                }
            }
            else {
                csonObject.put(entry.getKey(), value);
            }
        }
        return csonObject;
    }




    private ArrayDataContainer collectionObjectToArrayDataContainer(Collection<?> collection, Class<?> valueType) {
        ArrayDataContainer csonArray = arrayDataContainerFactory.create();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Object object : collection) {
            if(object instanceof Collection<?>) {
                ArrayDataContainer childArray = collectionObjectToArrayDataContainer((Collection<?>)object, null);
                csonArray.add(childArray);
            } else if(object instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                KeyValueDataContainer childObject = mapObjectToKeyValueDataContainer((Map<String, ?>)object, null);
                csonArray.add(childObject);
            } else if(types == Types.Object) {
                KeyValueDataContainer childObject = toKeyValueDataContainer(object);
                csonArray.add(childObject);
            }
            else {
                csonArray.add(object);
            }
        }
        return csonArray;

    }



    private ArrayDataContainer collectionObjectToSONArrayKnownSchema(Collection<?> collection, ISchemaArrayValue ISchemaArrayValue) {
        ArrayDataContainer resultCsonArray  = arrayDataContainerFactory.create();
        ArrayDataContainer csonArray = resultCsonArray;
        Iterator<?> iter = collection.iterator();
        TypeSchema objectValueTypeSchema = ISchemaArrayValue.getObjectValueTypeElement();
        Deque<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayDeque<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, csonArray);
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
        boolean isGeneric = ISchemaArrayValue.isGenericTypeValue();
        boolean isAbstractObject = ISchemaArrayValue.getEndpointValueType() == Types.AbstractObject;
        while(iter.hasNext()) {
            Object object = iter.next();
            if(object instanceof Collection<?>) {
                ArrayDataContainer childArray = arrayDataContainerFactory.create();
                csonArray.add(childArray);
                csonArray = childArray;
                iter = ((Collection<?>)object).iterator();
                currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, csonArray);
                arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
            } else if(objectValueTypeSchema == null) {
                if(isGeneric || isAbstractObject) {
                    object = object == null ? null :  toKeyValueDataContainer(object);
                }
                csonArray.add(object);
            } else {
                if(object == null)  {
                    csonArray.add(null);
                } else {
                    KeyValueDataContainer childObject = serializeTypeElement(objectValueTypeSchema, object);
                    csonArray.add(childObject);
                }
            }
            while(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                ArraySerializeDequeueItem arraySerializeDequeueItem = arraySerializeDequeueItems.getFirst();
                iter = arraySerializeDequeueItem.iterator;
                csonArray = arraySerializeDequeueItem.csonArray;
                if(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                    arraySerializeDequeueItems.removeFirst();
                }
            }
        }
        return resultCsonArray;

    }


    private Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaValueAbs schemaField, Object rootObject) {
        SchemaField parentschemaField = schemaField.getParentField();
        if(parentschemaField == null) {
            return rootObject;
        }
        int parentId = parentschemaField.getId();
        return parentsMap.get(parentId);
    }


    /**
     * KeyValueDataContainer 를 Map<String, T> 로 변환한다.
     * @param csonObject 변환할 KeyValueDataContainer
     * @param valueType Map 의 value 타입 클래스
     * @return 변환된 Map
     * @param <T> Map 의 value 타입
     */
    @SuppressWarnings({"unchecked", "unused"})
    public <T> Map<String, T> fromKeyValueDataContainerToMap(KeyValueDataContainer csonObject, Class<T> valueType) {
        Types types = Types.of(valueType);
        if(valueType.isPrimitive()) {
            throw new CSONSerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } else if(Collection.class.isAssignableFrom(valueType)) {
            throw new CSONSerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection.  valueType=" + valueType.getName());
        }  else if(Collection.class.isAssignableFrom(valueType)) {
            throw new CSONSerializerException("valueType is java.util.Map type. Use a class that wraps your Map.  valueType=" + valueType.getName());
        } else if(valueType.isArray() && Types.ByteArray != types) {
            throw new CSONSerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
        return (Map<String, T>) fromKeyValueDataContainerToMap(null, csonObject, valueType,null);

    }

    private interface OnObtainTypeValue {
        Object obtain(Object target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private  Map<?, ?> fromKeyValueDataContainerToMap(Map target, KeyValueDataContainer csonObject, Class valueType, OnObtainTypeValue onObtainTypeValue) {
        Types types = Types.of(valueType);
        if(target == null) {
            target = new HashMap<>();
        }

        Map finalTarget = target;
        if(onObtainTypeValue != null) {
            csonObject.keySet().forEach(key -> {
                Object childInCsonObject = csonObject.get(key);
                if(childInCsonObject == null) {
                    finalTarget.put(key, null);
                    return;
                }
                Object targetChild = onObtainTypeValue.obtain(childInCsonObject);
                if(targetChild == null) {
                    finalTarget.put(key, null);
                    return;
                }
                Types targetChildTypes = Types.of(targetChild.getClass());
                if(childInCsonObject instanceof KeyValueDataContainer && !Types.isSingleType(targetChildTypes)) {
                    fromKeyValueDataContainer((KeyValueDataContainer) childInCsonObject, targetChild);
                }
                finalTarget.put(key, targetChild);

            });
        }
        else if(Types.isSingleType(types)) {
            csonObject.keySet().forEach(key -> {
                Object value = optFrom(csonObject, key, types);
                finalTarget.put(key, value);
            });
        } else if(types == Types.Object) {
            csonObject.keySet().forEach(key -> {
                Object child = csonObject.get(key);
                if(child instanceof KeyValueDataContainer) {
                    Object targetChild = fromKeyValueDataContainer((KeyValueDataContainer)child, valueType);
                    finalTarget.put(key, targetChild);
                } else {
                    finalTarget.put(key, null);
                }
            });
        } else if(types == Types.CSONObject) {
            csonObject.keySet().forEach(key -> {
                Object child = csonObject.get(key);
                if(child instanceof KeyValueDataContainer) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        } else if(types == Types.CSONArray) {
            csonObject.keySet().forEach(key -> {
                Object child = csonObject.get(key);
                if(child instanceof ArrayDataContainer) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        } else if(types == Types.CSONElement) {
            csonObject.keySet().forEach(key -> {
                Object child = csonObject.get(key);
                if(child instanceof BaseDataContainer) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        }
        return target;

    }




    @SuppressWarnings("unchecked")
    public<T> T fromKeyValueDataContainer(KeyValueDataContainer csonObject, Class<T> clazz) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object object = typeSchema.newInstance();
        return (T) fromKeyValueDataContainer(csonObject, object);
    }


    private BaseDataContainer getChildElement(SchemaElementNode schemaElementNode, BaseDataContainer csonElement, Object key) {
        if(key instanceof String) {
            KeyValueDataContainer csonObject = (KeyValueDataContainer)csonElement;
            Object value = csonObject.get((String) key);
            if(schemaElementNode instanceof SchemaArrayNode && value instanceof ArrayDataContainer) {
                return (ArrayDataContainer) value;
            } else if(value instanceof KeyValueDataContainer) {
                return (KeyValueDataContainer) value;
            }
            return null;
        } else {
            ArrayDataContainer csonArray = (ArrayDataContainer) csonElement;
            Object value = csonArray.get((int) key);
            if(schemaElementNode instanceof SchemaArrayNode &&  value instanceof ArrayDataContainer) {
                return (ArrayDataContainer) value;
            } else if(value instanceof KeyValueDataContainer) {
                return (KeyValueDataContainer) value;
            }
            return null;
        }

    }

    public<T> T fromKeyValueDataContainer(final KeyValueDataContainer csonObject, T targetObject) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();
        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        BaseDataContainer csonElement = csonObject;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonObject);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
        while(iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            BaseDataContainer parentsCSON = csonElement;
            if(node instanceof SchemaElementNode) {
                boolean nullValue = false;
                BaseDataContainer childElement = getChildElement((SchemaElementNode) node, csonElement, key);
                if(key instanceof String) {
                    KeyValueDataContainer parentObject = (KeyValueDataContainer) csonElement;
                    if(childElement == null && parentObject != null) {
                        Object v = parentObject.get((String) key);
                        if(v == null || v == NullValue.Instance) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                } else {
                    assert csonElement instanceof ArrayDataContainer;
                    ArrayDataContainer parentArray = (ArrayDataContainer)csonElement;
                    int index = (Integer)key;
                    if(childElement == null) {
                        if(parentArray.size() <= index) {
                            nullValue = true;
                        } else {
                            Object v = parentArray.get(index);
                            if(v == null || v == NullValue.Instance) {
                                nullValue = true;
                            } else {
                                continue;
                            }
                        }
                    }
                }

                csonElement = childElement;
                schemaNode = (SchemaObjectNode)node;
                List<SchemaValueAbs> parentSchemaFieldList = schemaNode.getParentSchemaFieldList();
                for(SchemaValueAbs parentSchemaField : parentSchemaFieldList) {
                    getOrCreateParentObject(parentSchemaField, parentObjMap, targetObject, nullValue, parentsCSON, csonObject);
                }
                iter = schemaNode.keySet().iterator();
                currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonElement);
                objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
            }
            else if(node instanceof SchemaValueAbs && ((SchemaValueAbs)node).types() != Types.Object) {
                SchemaValueAbs schemaField = (SchemaValueAbs) node;
                SchemaValueAbs parentField = schemaField.getParentField();
                if(csonElement != null) {
                    Object obj = getOrCreateParentObject(parentField, parentObjMap, targetObject, parentsCSON, csonObject);
                    setValueTargetFromKeyValueDataContainers(obj, schemaField, csonElement, key, csonObject);
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.ISchemaNode;
                csonElement = objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return targetObject;
    }

    private Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, BaseDataContainer csonElement, KeyValueDataContainer rootCSON) {
        return getOrCreateParentObject(parentSchemaField, parentObjMap, root, false, csonElement, rootCSON);
    }

    private Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, boolean setNull, BaseDataContainer csonElement, KeyValueDataContainer rootCSON) {
        if(parentSchemaField == null) return root;

        int id = parentSchemaField.getId();
        Object parent = parentObjMap.get(id);
        if (parent != null) {
            return parent;
        }
        ArrayList<SchemaValueAbs>  pedigreeList = new ArrayList<>();
        while(parentSchemaField != null) {
            pedigreeList.add(parentSchemaField);
            parentSchemaField = parentSchemaField.getParentField();
        }
        Collections.reverse(pedigreeList);
        parent = root;
        SchemaValueAbs last = pedigreeList.get(pedigreeList.size() - 1);
        for(SchemaValueAbs schemaField : pedigreeList) {
           int parentId = schemaField.getId();
           Object child = parentObjMap.get(parentId);
           if(setNull && child == null && schemaField == last) {
                schemaField.setValue(parent, null);
           }
           else if(!setNull && child == null) {
                if(schemaField instanceof ObtainTypeValueInvokerGetter) {
                    // TODO 앞으로 제네릭 또는 interface 나 추상 클래스로만 사용 가능하도록 변경할 것.
                    ObtainTypeValueInvoker obtainTypeValueInvoker = ((ObtainTypeValueInvokerGetter)schemaField).getObtainTypeValueInvoker();
                    if(obtainTypeValueInvoker != null) {
                        OnObtainTypeValue onObtainTypeValue = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parent, rootCSON);
                        child = onObtainTypeValue.obtain(csonElement);
                    }
                }
                if(child == null) {
                   child = schemaField.newInstance();
                }
                parentObjMap.put(parentId, child);
                schemaField.setValue(parent, child);
           }
           parent = child;
        }
        return parent;

    }


    private void setValueTargetFromKeyValueDataContainers(Object parents, SchemaValueAbs schemaField, BaseDataContainer cson, Object key, KeyValueDataContainer root) {
        List<SchemaValueAbs> schemaValueAbsList = schemaField.getAllSchemaValueList();
        for(SchemaValueAbs schemaValueAbs : schemaValueAbsList) {
            setValueTargetFromKeyValueDataContainer(parents, schemaValueAbs, cson, key,root);
        }
    }

    /**
     * Object의 타입을 읽어서 실제 타입으로 캐스팅한다.
     * @param value Object 타입의 값
     * @param realValue 실제 타입의 값
     * @return
     */
    private Object dynamicCasting(Object value, Object realValue) {
        if(value == null) return null;
        Class<?> valueClas = value.getClass();
        Types valueType = Types.of(value.getClass());
        if(valueClas.isEnum()) {
            try {
                //noinspection unchecked
                return Enum.valueOf((Class<? extends Enum>) valueClas,realValue.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if(Types.isSingleType(valueType) || Types.isCsonType(valueType)) {
            return DataConverter.convertValue(valueClas, realValue);
        } else if(Types.Object == valueType) {
            KeyValueDataContainer csonObj = realValue instanceof KeyValueDataContainer ? (KeyValueDataContainer) realValue : null;
            if(csonObj != null) {
                fromKeyValueDataContainer(csonObj, value);
                return value;
            }
        }
        return null;
    }

    private void setValueTargetFromKeyValueDataContainer(Object parents, SchemaValueAbs schemaField, final BaseDataContainer cson, Object key, KeyValueDataContainer root) {
        boolean isArrayType = cson instanceof ArrayDataContainer;

        /*Object value = isArrayType ? ((ArrayDataContainer) cson).opt((int)key) : ((KeyValueDataContainer)cson).opt((String)key);
        //todo null 값에 대하여 어떻게 할 것인지 고민해봐야함.
        if(value == null) {
            boolean isNull = isArrayType ? ((ArrayDataContainer) cson).isNull((int)key) : ((KeyValueDataContainer)cson).isNull((String)key);
            if(isNull && !schemaField.isPrimitive()) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
            return;
        }*/
        Types valueType = schemaField.getType();
        if(Types.isSingleType(valueType)) {
            Object valueObj = optFrom(cson, key, valueType);
            schemaField.setValue(parents, valueObj);
        } else if((Types.AbstractObject == valueType || Types.GenericType == valueType) && schemaField instanceof ObtainTypeValueInvokerGetter) {
            Object val = optFrom(cson, key, valueType);

            Object obj = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parents, root).obtain(val) ;//on == null ? null : onObtainTypeValue.obtain(cson instanceof KeyValueDataContainer ? (KeyValueDataContainer) cson : null);
            if(obj == null) {
                obj = schemaField.newInstance();
                obj = dynamicCasting(obj, val);
            }
            schemaField.setValue(parents, obj);
        }
        else if(Types.Collection == valueType) {
            Object value;
            if(isArrayType) {
                value = ((ArrayDataContainer) cson).get((int)key);
            } else {
                value = ((KeyValueDataContainer)cson).get((String)key);
            }
            if(value instanceof ArrayDataContainer) {
                OnObtainTypeValue onObtainTypeValue = null;
                boolean isGenericOrAbsType = ((ISchemaArrayValue)schemaField).isGenericTypeValue() || ((ISchemaArrayValue)schemaField).isAbstractType();
                if(isGenericOrAbsType) {
                    onObtainTypeValue = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parents, root);
                }
                csonArrayToCollectionObject((ArrayDataContainer)value, (ISchemaArrayValue)schemaField, parents, onObtainTypeValue);
            } else if(isNull(cson,key)) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
        } else if(Types.Object == valueType) {
            Object value;
            if(isArrayType) {
                value = ((ArrayDataContainer) cson).get((int)key);
            } else {
                value = ((KeyValueDataContainer)cson).get((String)key);
            }

            if(value instanceof KeyValueDataContainer) {
                Object target = schemaField.newInstance();
                fromKeyValueDataContainer((KeyValueDataContainer) value, target);
                schemaField.setValue(parents, target);
            } else if(isNull(cson,key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.Map == valueType) {
            Object value;
            if(isArrayType) {
                value = ((ArrayDataContainer) cson).get((int)key);
            } else {
                value = ((KeyValueDataContainer)cson).get((String)key);
            }

            if(value instanceof KeyValueDataContainer) {
                Object target = schemaField.newInstance();
                Class<?> type = ((ISchemaMapValue)schemaField).getElementType();
                boolean isGenericOrAbstract = ((ISchemaMapValue)schemaField).isGenericValue() || ((ISchemaMapValue)schemaField).isAbstractType();
                OnObtainTypeValue onObtainTypeValue = null;
                if(isGenericOrAbstract) {
                    onObtainTypeValue = makeOnObtainTypeValue( (ObtainTypeValueInvokerGetter)schemaField, parents, root);
                }
                fromKeyValueDataContainerToMap((Map<?, ?>) target, (KeyValueDataContainer)value, type, onObtainTypeValue);
                schemaField.setValue(parents, target);
            } else if(isNull(cson,key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.CSONArray == valueType || Types.CSONElement == valueType || Types.CSONObject == valueType) {
            Object value = optValue(cson, key);
            CSONElement csonElement = toCSONElement(value);
            schemaField.setValue(parents, csonElement);
        }
        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
        }
    }

    private static CSONElement toCSONElement(Object object) {
        if(object instanceof KeyValueDataContainer) {
            return toCSONObject((KeyValueDataContainer)object);
        } else if(object instanceof ArrayDataContainer) {
            return toCSONArray((ArrayDataContainer)object);
        }
        return null;
    }

    private static CSONArray toCSONArray(ArrayDataContainer arrayDataContainer) {
        CSONArray csonArray = new CSONArray();
        for(int i = 0, n = arrayDataContainer.size(); i < n; ++i) {
            Object value = arrayDataContainer.get(i);
            if(value instanceof KeyValueDataContainer) {
                csonArray.add(toCSONObject((KeyValueDataContainer)value));
            } else if(value instanceof ArrayDataContainer) {
                csonArray.add(toCSONArray((ArrayDataContainer)value));
            } else {
                csonArray.add(value);
            }
        }
        return csonArray;
    }

    private static CSONObject toCSONObject(KeyValueDataContainer keyValueDataContainer) {
        CSONObject csonObject = new CSONObject();
        for(String key : keyValueDataContainer.keySet()) {
            Object value = keyValueDataContainer.get(key);
            if(value instanceof KeyValueDataContainer) {
                csonObject.put(key, toCSONObject((KeyValueDataContainer)value));
            } else if(value instanceof ArrayDataContainer) {
                csonObject.put(key, toCSONArray((ArrayDataContainer)value));
            } else {
                csonObject.put(key, value);
            }
        }
        return csonObject;
    }

    private static Object optValue(BaseDataContainer cson, Object key) {
        Object value = null;
        if(key instanceof String && cson instanceof KeyValueDataContainer) {
            value = ((KeyValueDataContainer)cson).get((String)key);
        } else if(cson instanceof ArrayDataContainer) {
            value = ((ArrayDataContainer)cson).get((int)key);
        }
        return value;
    }

    private static ArrayDataContainer optArrayDataContainer(BaseDataContainer cson, Object key) {
        Object value = optValue(cson, key);
        return value instanceof ArrayDataContainer ? (ArrayDataContainer)value : null;
    }

    private static KeyValueDataContainer optKeyValueDataContainer(BaseDataContainer cson, Object key) {
        Object value = optValue(cson, key);
        return value instanceof KeyValueDataContainer ? (KeyValueDataContainer)value : null;
    }


    private OnObtainTypeValue makeOnObtainTypeValue(ObtainTypeValueInvokerGetter obtainTypeValueInvokerGetter, Object parents, KeyValueDataContainer root) {
        return (csonObjectOrValue) -> {
            ObtainTypeValueInvoker invoker = obtainTypeValueInvokerGetter.getObtainTypeValueInvoker();
            if(invoker == null ) {
                if(obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw new CSONSerializerException("To deserialize a generic, abstract or interface type you must have a @ObtainTypeValue annotated method. target=" + obtainTypeValueInvokerGetter.targetPath());
            }
            try {
                Object cson;
                if(csonObjectOrValue instanceof KeyValueDataContainer) {
                    cson = csonObjectOrValue;
                } else  {
                    cson =  keyValueDataContainerFactory.create();
                    ((KeyValueDataContainer)cson).put("$value",  root);
                }


                Object obj = invoker.obtain(parents, toCSONObject((KeyValueDataContainer)cson), toCSONObject(root));
                if (obj != null && invoker.isDeserializeAfter()) {
                    obj = dynamicCasting(obj, csonObjectOrValue);
                }
                return obj;
            } catch (RuntimeException e) {
                if(obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw e;
            }
        };
    }



    private static boolean isNull(BaseDataContainer csonElement, Object key) {
        Object value;
        if(key instanceof String && csonElement instanceof KeyValueDataContainer) {
            value = ((KeyValueDataContainer)csonElement).get((String) key);
        } else if(csonElement instanceof ArrayDataContainer) {
            value = ((ArrayDataContainer)csonElement).get((int) key);
        } else {
            return false;
        }
        return value == null || value == NullValue.Instance;

    }



    private ArrayDataContainer optValueInArrayDataContainer(ArrayDataContainer csonArray, int index) {
        Object value = csonArray.get(index);
        return value instanceof ArrayDataContainer ? (ArrayDataContainer)value : null;
    }


    private Object optValueInArrayDataContainer(ArrayDataContainer csonArray, int index, ISchemaArrayValue ISchemaArrayValue) {
        Types types = ISchemaArrayValue.getEndpointValueType();
        if(types == Types.Object) {
            Object value = csonArray.get(index);
            if(value instanceof KeyValueDataContainer) {
                Object target = ISchemaArrayValue.getObjectValueTypeElement().newInstance();
                fromKeyValueDataContainer((KeyValueDataContainer) value, target);
                return target;
            }
        }
        return optFrom(csonArray, index, types);
    }


    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    private void csonArrayToCollectionObject(ArrayDataContainer csonArray, ISchemaArrayValue ISchemaArrayValue, Object parent, OnObtainTypeValue onObtainTypeValue) {
        List<CollectionItems> collectionItems = ISchemaArrayValue.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        if(collectionItemSize == 0) {
            return;
        }
        CollectionItems collectionItem = collectionItems.get(collectionItemIndex);
        ArrayList<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayList<>();
        ArraySerializeDequeueItem objectItem = new ArraySerializeDequeueItem(csonArray,collectionItem.newInstance());
        int end = objectItem.getEndIndex();
        arraySerializeDequeueItems.add(objectItem);

        for(int index = 0; index <= end; ++index) {
            objectItem.setArrayIndex(index);
            if(collectionItem.isGeneric() || collectionItem.isAbstractType()) {
                KeyValueDataContainer csonObject =  optKeyValueDataContainer(objectItem.csonArray,index);
                Object object = onObtainTypeValue.obtain(csonObject);
                objectItem.collectionObject.add(object);
            }
            else if (collectionItem.getValueClass() != null) {
                Object value = optValueInArrayDataContainer(objectItem.csonArray, index, ISchemaArrayValue);
                objectItem.collectionObject.add(value);
            } else {
                ArrayDataContainer inArray = optValueInArrayDataContainer(objectItem.csonArray, index);
                if (inArray == null) {
                    objectItem.collectionObject.add(null);
                } else {
                    collectionItem = collectionItems.get(++collectionItemIndex);
                    Collection newCollection = collectionItem.newInstance();
                    objectItem.collectionObject.add(newCollection);
                    ArraySerializeDequeueItem newArraySerializeDequeueItem = new ArraySerializeDequeueItem(inArray, newCollection);
                    arraySerializeDequeueItems.add(newArraySerializeDequeueItem);
                    index = -1;
                    end = newArraySerializeDequeueItem.getEndIndex();
                    objectItem = newArraySerializeDequeueItem;
                }
            }
            while (index == end) {
                arraySerializeDequeueItems.remove(arraySerializeDequeueItems.size() - 1);
                if (arraySerializeDequeueItems.isEmpty()) {
                    break;
                }
                objectItem = arraySerializeDequeueItems.get(arraySerializeDequeueItems.size() - 1);
                index = objectItem.index;
                end = objectItem.arraySize - 1;
                collectionItem = collectionItems.get(--collectionItemIndex);
            }
        }
        ISchemaArrayValue.setValue(parent, objectItem.collectionObject);
    }





    @SuppressWarnings("rawtypes")
    private class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        ArrayDataContainer csonArray;

        Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator,ArrayDataContainer csonArray) {
            this.iterator = iterator;
            this.csonArray = csonArray;
        }

        private ArraySerializeDequeueItem(ArrayDataContainer csonArray, Collection collection) {
            this.csonArray = csonArray;
            this.arraySize = csonArray.size();
            this.collectionObject = collection;
        }

        private int getEndIndex() {
            return arraySize - 1;
        }



        /**
         * 역직렬화에서 사용됨.
         *
         */
        private void setArrayIndex(int index) {
            this.index = index;
        }


    }

    private static class ObjectSerializeDequeueItem {
        Iterator<Object> keyIterator;
        com.hancomins.cson.serializer.mapper.ISchemaNode ISchemaNode;
        BaseDataContainer resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, com.hancomins.cson.serializer.mapper.ISchemaNode ISchemaNode, BaseDataContainer resultElement) {
            this.keyIterator = keyIterator;
            this.ISchemaNode = ISchemaNode;
            this.resultElement = resultElement;
        }
    }



    static Object optFrom(BaseDataContainer baseDataContainer, Object key, Types valueType) {

        Object value = optValue(baseDataContainer, key);
        if(value == null) {
            return null;
        }


        if(Types.Boolean == valueType) {
            return DataConverter.toBoolean(value);
        } else if(Types.Byte == valueType) {
            return DataConverter.toByte(value);
        } else if(Types.Character == valueType) {
            return DataConverter.toChar(value);
        } else if(Types.Long == valueType) {
            return DataConverter.toLong(value);
        } else if(Types.Short == valueType) {
            return DataConverter.toShort(value);
        } else if(Types.Integer == valueType) {
            return DataConverter.toInteger(value);
        } else if(Types.Float == valueType) {
            return DataConverter.toFloat(value);
        } else if(Types.Double == valueType) {
            return DataConverter.toDouble(value);
        } else if(Types.String == valueType) {
            return DataConverter.toString(value);
        }  else if(Types.ByteArray == valueType) {
            return DataConverter.toByteArray(value);
        } else {
            return value;
        }
    }


}
