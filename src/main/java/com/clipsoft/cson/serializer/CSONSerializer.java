package com.clipsoft.cson.serializer;

import com.clipsoft.cson.*;
import com.clipsoft.cson.util.DataConverter;

import java.util.*;

public class CSONSerializer {

    private CSONSerializer() {}

    public static boolean serializable(Class<?> clazz) {
        if(TypeElements.getInstance().hasTypeInfo(clazz)) {
            return true;
        }
        return clazz.getAnnotation(CSON.class) != null;
    }

    public static CSONObject toCSONObject(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        Class<?> clazz = obj.getClass();
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(clazz);
        return serializeTypeElement(typeElement,obj);
    }

    private static CSONObject serializeTypeElement(TypeElement typeElement, final Object rootObject) {
        Class<?> type = typeElement.getType();
        if(rootObject.getClass() != type) {
            throw new CSONSerializerException("Type mismatch error. " + type.getName() + "!=" + rootObject.getClass().getName());
        }
        else if(rootObject == null) {
            return null;
        }
        SchemaObjectNode schemaRoot = typeElement.getSchema();

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        CSONElement csonElement = new CSONObject();
        String comment = typeElement.getComment();
        String commentAfter = typeElement.getCommentAfter();
        if(comment != null) {
            csonElement.setCommentThis(comment);
        }
        if(commentAfter != null) {
            csonElement.setCommentAfterThis(commentAfter);
        }
        CSONObject root = (CSONObject) csonElement;
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

                CSONElement csonElementValue = null;

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
                    if(parentObj instanceof CSONElement) {
                        if(csonElementValue instanceof CSONObject && parentObj instanceof CSONObject) {
                            ((CSONObject) csonElementValue).merge((CSONObject) parentObj);
                        } else if(csonElementValue instanceof CSONArray && parentObj instanceof CSONArray) {
                            ((CSONArray) csonElementValue).merge((CSONArray) parentObj);
                        }  else {
                            csonElementValue = (CSONElement) parentObj;
                        }
                        nullCount--;
                    }
                    else if(parentObj != null) {
                        parentObjMap.put(id, parentObj);
                        nullCount--;
                    }
                }

                if(!schemaNode.isBranchNode() && nullCount > 0) {
                    if(key instanceof String) {
                        ((CSONObject)csonElement).put((String) key,null);
                    } else {
                        assert csonElement instanceof CSONArray;
                        ((CSONArray)csonElement).set((Integer) key,null);
                    }
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    if(key instanceof String) {
                        CSONObject currentObject = ((CSONObject)csonElement);
                        CSONElement childElement = currentObject.optCSONObject((String) key);
                        if (childElement == null) {
                            if(csonElementValue != null) {
                                currentObject.put((String) key, csonElementValue);
                                csonElement = csonElementValue;
                            } else {
                                childElement = (schemaNode instanceof SchemaArrayNode) ? new CSONArray() : new CSONObject();
                                currentObject.put((String) key, childElement);
                                currentObject.setCommentForKey((String) key, schemaNode.getComment());
                                currentObject.setCommentAfterKey((String) key, schemaNode.getAfterComment());
                                csonElement = childElement;
                            }
                        }

                    } else {
                        if(!(csonElement instanceof CSONArray)) {
                            throw new CSONSerializerException("Invalide path. '" + key + "' is not array index." +  "(csonElement is not CSONArray. csonElement=" + csonElement +  ")");
                        }
                        CSONArray currentObject = ((CSONArray)csonElement);
                        CSONArray currentArray = ((CSONArray)csonElement);
                        CSONElement childElement = (CSONElement) currentArray.opt((Integer) key);
                        if(childElement == null) {
                            if(csonElementValue != null) {
                                currentObject.set((int)key, csonElementValue);
                                csonElement = csonElementValue;
                            } else {
                                childElement = (schemaNode instanceof SchemaArrayNode) ? new CSONArray() : new CSONObject();
                                currentObject.set((int) key, childElement);
                                csonElement = childElement;
                            }
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
                    putValueInCSONElement(csonElement, schemaValueAbs, key, value);
                }
            } else if(node instanceof ISchemaMapValue) {
                SchemaValueAbs schemaMap = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaMap, rootObject);
                if(parent != null) {
                    Object value = schemaMap.getValue(parent);
                    if(value != null) {
                        @SuppressWarnings("unchecked")
                        CSONObject csonObject = mapObjectToCSONObject((Map<String, ?>) value, ((ISchemaMapValue)schemaMap).getElementType());
                        putValueInCSONElement(csonElement, schemaMap, key, csonObject);
                    } else {
                        putValueInCSONElement(csonElement, schemaMap, key, null);
                    }
                }

            }
            else if(node instanceof ISchemaArrayValue) {
                ISchemaArrayValue ISchemaArrayValue = (ISchemaArrayValue)node;
                Object parent = obtainParentObjects(parentObjMap, (SchemaValueAbs) ISchemaArrayValue, rootObject);
                if(parent != null) {
                    Object value = ISchemaArrayValue.getValue(parent);
                    if(value != null) {
                        CSONArray csonArray = collectionObjectToSONArrayKnownSchema((Collection<?>)value, ISchemaArrayValue);
                        putValueInCSONElement(csonElement, ISchemaArrayValue, key, csonArray);
                    } else {
                        putValueInCSONElement(csonElement, ISchemaArrayValue, key, null);
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

    private static void putValueInCSONElement(CSONElement csonElement, ISchemaValue ISchemaValueAbs, Object key, Object value) {
        if(key instanceof String) {
            ((CSONObject) csonElement).put((String) key, value);
            ((CSONObject) csonElement).setCommentForKey((String) key, ISchemaValueAbs.getComment());
            ((CSONObject) csonElement).setCommentAfterKey((String) key, ISchemaValueAbs.getAfterComment());
        }
        else {
            assert csonElement instanceof CSONArray;
            ((CSONArray)csonElement).set((int)key, value);
            ((CSONArray)csonElement).setCommentForValue((int)key, ISchemaValueAbs.getComment()) ;
            ((CSONArray)csonElement).setCommentAfterValue((int)key, ISchemaValueAbs.getAfterComment());
        }
    }

    public static CSONObject mapToCSONObject(Map<String, ?> map) {
        return mapObjectToCSONObject(map, null);
    }

    public static CSONArray collectionToCSONArray(Collection<?> collection) {
        return collectionObjectToCSONArray(collection, null);
    }



    public static <T> List<T> csonArrayToList(CSONArray csonArray, Class<T> valueType) {
        return csonArrayToList(csonArray, valueType, null, false, null);
    }

    public static <T> List<T> csonArrayToList(CSONArray csonArray, Class<T> valueType, boolean ignoreError) {
        return csonArrayToList(csonArray, valueType, null, ignoreError, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> csonArrayToList(CSONArray csonArray, Class<T> valueType, StringFormatOption stringFormatOption, boolean ignoreError, T defaultValue) {
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
                if(stringFormatOption != null && value instanceof CSONElement) {
                    result.add((T)((CSONElement) value).toString(stringFormatOption));
                } else {
                    result.add((T) value.toString());
                }
            } else if(value instanceof CSONObject && CSONSerializer.serializable(valueType)) {
                try {
                    value = CSONSerializer.fromCSONObject((CSONObject) value, valueType);
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


    private static CSONObject mapObjectToCSONObject(Map<String, ?> map, Class<?> valueType) {
        CSONObject csonObject = new CSONObject();
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
                CSONArray csonArray = collectionObjectToCSONArray((Collection<?>)value, null);
                csonObject.put(key, csonArray);
            } else if(value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                CSONObject childObject = mapObjectToCSONObject((Map<String, ?>)value, null);
                csonObject.put(key, childObject);
            } else if(types == Types.Object) {
                CSONObject childObject = toCSONObject(value);
                csonObject.put(entry.getKey(), childObject);
            }
            else {
                csonObject.put(entry.getKey(), value);
            }
        }
        return csonObject;
    }




    private static CSONArray collectionObjectToCSONArray(Collection<?> collection, Class<?> valueType) {
        CSONArray csonArray = new CSONArray();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Object object : collection) {
            if(object instanceof Collection<?>) {
                CSONArray childArray = collectionObjectToCSONArray((Collection<?>)object, null);
                csonArray.add(childArray);
            } else if(object instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                CSONObject childObject = mapObjectToCSONObject((Map<String, ?>)object, null);
                csonArray.add(childObject);
            } else if(types == Types.Object) {
                CSONObject childObject = toCSONObject(object);
                csonArray.add(childObject);
            }
            else {
                csonArray.add(object);
            }
        }
        return csonArray;

    }



    private static CSONArray collectionObjectToSONArrayKnownSchema(Collection<?> collection, ISchemaArrayValue ISchemaArrayValue) {
        CSONArray resultCsonArray  = new CSONArray();
        CSONArray csonArray = resultCsonArray;
        Iterator<?> iter = collection.iterator();
        TypeElement objectValueTypeElement = ISchemaArrayValue.getObjectValueTypeElement();
        Deque<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayDeque<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, csonArray);
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
        while(iter.hasNext()) {
            Object object = iter.next();
            if(object instanceof Collection<?>) {
                CSONArray childArray = new CSONArray();
                csonArray.add(childArray);
                csonArray = childArray;
                iter = ((Collection<?>)object).iterator();
                currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, csonArray);
                arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
            } else if(objectValueTypeElement == null) {
                csonArray.add(object);
            } else {
                if(object == null)  {
                    csonArray.add(null);
                } else {
                    CSONObject childObject = serializeTypeElement(objectValueTypeElement, object);
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


    private static Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaValueAbs schemaField, Object rootObject) {
        SchemaField parentschemaField = schemaField.getParentField();
        if(parentschemaField == null) {
            return rootObject;
        }
        int parentId = parentschemaField.getId();
        return parentsMap.get(parentId);
    }


    @SuppressWarnings({"unchecked", "unused"})
    public static <T> Map<String, T> fromCSONObjectToMap(CSONObject csonObject, Class<T> valueType) {
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
        return (Map<String, T>) fromCSONObjectToMap(null, csonObject, valueType);

    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static  Map<?, ?> fromCSONObjectToMap(Map target, CSONObject csonObject, Class valueType) {
        Types types = Types.of(valueType);
        if(target == null) {
            target = new HashMap<>();
        }

        Map finalTarget = target;
        if(Types.isSingleType(types)) {

            csonObject.keySet().forEach(key -> {
                Object value = Utils.optFrom(csonObject, key, types);
                finalTarget.put(key, value);
            });
        } else if(types == Types.Object) {
            csonObject.keySet().forEach(key -> {
                CSONObject child = csonObject.optCSONObject(key, null);
                if(child != null) {
                    Object targetChild = fromCSONObject(child, valueType);
                    finalTarget.put(key, targetChild);
                } else {
                    finalTarget.put(key, null);
                }
            });
        }

        return target;

    }




    @SuppressWarnings("unchecked")
    public static<T> T fromCSONObject(CSONObject csonObject, Class<T> clazz) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(clazz);
        Object object = typeElement.newInstance();
        return (T) fromCSONObject(csonObject, object);
    }


    private static CSONElement getChildElement(SchemaElementNode schemaElementNode,CSONElement csonElement, Object key) {
        if(key instanceof String) {
            CSONObject csonObject = (CSONObject)csonElement;
            return schemaElementNode instanceof  SchemaArrayNode ? csonObject.optCSONArray((String) key) :  csonObject.optCSONObject((String) key) ;
        } else {
            CSONArray csonArray = (CSONArray) csonElement;
            return schemaElementNode instanceof SchemaArrayNode ?  csonArray.optCSONArray((int) key) :  csonArray.optCSONObject((int) key);
        }

    }

    public static<T> T fromCSONObject(final CSONObject csonObject, T targetObject) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeElement.getSchema();
        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        CSONElement csonElement = csonObject;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonObject);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
        while(iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaElementNode) {
                boolean nullValue = false;
                CSONElement childElement = getChildElement((SchemaElementNode) node, csonElement, key);
                if(key instanceof String) {
                    CSONObject parentObject = (CSONObject) csonElement;
                    if(childElement == null) {
                        if(parentObject.isNull((String) key)) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                } else {
                    assert csonElement instanceof CSONArray;
                    CSONArray parentArray = (CSONArray)csonElement;
                    int index = (Integer)key;
                    if(childElement == null) {
                        if(parentArray.size() <= index || parentArray.isNull(index)) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                }
                csonElement = childElement;
                schemaNode = (SchemaObjectNode)node;
                List<SchemaValueAbs> parentSchemaFieldList = schemaNode.getParentSchemaFieldList();
                for(SchemaValueAbs parentSchemaField : parentSchemaFieldList) {
                    getOrCreateParentObject(parentSchemaField, parentObjMap, targetObject, nullValue);
                }
                iter = schemaNode.keySet().iterator();
                currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonElement);
                objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
            }
            else if(node instanceof SchemaValueAbs && ((SchemaValueAbs)node).type != Types.Object) {
                SchemaValueAbs schemaField = (SchemaValueAbs) node;
                SchemaValueAbs parentField = schemaField.getParentField();
                if(csonElement != null) {
                    Object obj = getOrCreateParentObject(parentField, parentObjMap, targetObject);
                    setValueTargetFromCSONObjects(obj, schemaField, csonElement, key);
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

    private static Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root) {
        return getOrCreateParentObject(parentSchemaField, parentObjMap, root, false);
    }

    private static Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, boolean setNull) {
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
                child = schemaField.newInstance();
                parentObjMap.put(parentId, child);
                schemaField.setValue(parent, child);
           }
           parent = child;
        }
        return parent;

    }


    private static void setValueTargetFromCSONObjects(Object parents, SchemaValueAbs schemaField, CSONElement cson, Object key) {
        List<SchemaValueAbs> schemaValueAbsList = schemaField.getAllSchemaValueList();
        for(SchemaValueAbs schemaValueAbs : schemaValueAbsList) {
            setValueTargetFromCSONObject(parents, schemaValueAbs, cson, key);
        }
    }

    private static void setValueTargetFromCSONObject(Object parents, SchemaValueAbs schemaField, CSONElement cson, Object key) {
        boolean isArrayType = cson instanceof CSONArray;

        /*Object value = isArrayType ? ((CSONArray) cson).opt((int)key) : ((CSONObject)cson).opt((String)key);
        //todo null 값에 대하여 어떻게 할 것인지 고민해봐야함.
        if(value == null) {
            boolean isNull = isArrayType ? ((CSONArray) cson).isNull((int)key) : ((CSONObject)cson).isNull((String)key);
            if(isNull && !schemaField.isPrimitive()) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
            return;
        }*/
        Types valueType = schemaField.getType();
        if(Types.isSingleType(valueType)) {
            Object valueObj = Utils.optFrom(cson, key, valueType);
            schemaField.setValue(parents, valueObj);
        } else if(Types.Collection == valueType) {
            CSONArray csonArray = isArrayType ? ((CSONArray) cson).optCSONArray((int)key) : ((CSONObject)cson).optCSONArray((String)key);
            if(csonArray != null) {
                csonArrayToCollectionObject(csonArray, (ISchemaArrayValue)schemaField, parents);
            } else if(isArrayType ? ((CSONArray) cson).isNull((int)key) : ((CSONObject)cson).isNull((String)key)) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
        } else if(Types.Object == valueType) {
            CSONObject csonObj = isArrayType ? ((CSONArray) cson).optCSONObject((int)key) : ((CSONObject)cson).optCSONObject((String)key);
            if(csonObj != null) {
                Object target = schemaField.newInstance();
                fromCSONObject(csonObj, target);
                schemaField.setValue(parents, target);
            } else if(isArrayType ? ((CSONArray) cson).isNull((int)key) : ((CSONObject)cson).isNull((String)key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.Map == valueType) {
            CSONObject csonObj = isArrayType ? ((CSONArray) cson).optCSONObject((int)key) : ((CSONObject)cson).optCSONObject((String)key);
            if(csonObj != null) {
                Object target = schemaField.newInstance();
                Class<?> type = ((ISchemaMapValue)schemaField).getElementType();
                fromCSONObjectToMap((Map<?,?>)target,csonObj,type);
                schemaField.setValue(parents, target);
            } else if(isArrayType ? ((CSONArray) cson).isNull((int)key) : ((CSONObject)cson).isNull((String)key)) {
                schemaField.setValue(parents, null);
            }
        }
        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
        }

    }


    private static Object optValueInCSONArray(CSONArray csonArray, int index, ISchemaArrayValue ISchemaArrayValue) {



        switch (ISchemaArrayValue.getEndpointValueType()) {
            case Byte:
                return csonArray.optByte(index);
            case Short:
                return csonArray.optShort(index);
            case Integer:
                return csonArray.optInt(index);
            case Long:
                return csonArray.optLong(index);
            case Float:
                return csonArray.optFloat(index);
            case Double:
                return csonArray.optDouble(index);
            case Boolean:
                return csonArray.optBoolean(index);
            case Character:
                return csonArray.optChar(index, '\0');
            case String:
                return csonArray.optString(index);
            case Object:
                CSONObject csonObject = csonArray.optCSONObject(index);
                if(csonObject != null) {
                    Object target = ISchemaArrayValue.getObjectValueTypeElement().newInstance();
                    fromCSONObject(csonObject, target);
                    return target;
                }
        }
        return null;
    }




    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    private static void csonArrayToCollectionObject(CSONArray csonArray, ISchemaArrayValue ISchemaArrayValue, Object parent) {
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
            if (collectionItem.valueClass != null) {
                Object value = optValueInCSONArray(objectItem.csonArray, index, ISchemaArrayValue);
                objectItem.collectionObject.add(value);
            } else {
                CSONArray inArray = objectItem.csonArray.optCSONArray(index);
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
    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        CSONArray csonArray;

        Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator,CSONArray csonArray) {
            this.iterator = iterator;
            this.csonArray = csonArray;
        }

        private ArraySerializeDequeueItem(CSONArray csonArray, Collection collection) {
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
        ISchemaNode ISchemaNode;
        CSONElement resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, ISchemaNode ISchemaNode, CSONElement resultElement) {
            this.keyIterator = keyIterator;
            this.ISchemaNode = ISchemaNode;
            this.resultElement = resultElement;
        }
    }




}
