package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.container.*;
import com.hancomins.cson.util.ArrayMap;
import com.hancomins.cson.util.DataConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContainerOfObjectSchema implements KeyValueDataContainer {

    private _ObjectNode objectNode;
    private Object rootObject;

    private List<Integer> whildObjectIdList = null;

    private ArrayMap<Object> parentMap = null;

    private List<Runnable> setValueExecutorList = new ArrayList<>();

    public ContainerOfObjectSchema(Class<?> classType) {
        ClassSchema classSchema = ClassSchemaMap.getInstance().getClassSchema(classType);
        initRootNode(classSchema, null);
    }

    public ContainerOfObjectSchema(Object rootValue) {
        ClassSchema classSchema = ClassSchemaMap.getInstance().getClassSchema(rootValue.getClass());
        initRootNode(classSchema, rootValue);
    }

    private void initRootNode(ClassSchema classSchema, Object rootValue) {
        this.objectNode = new _NodeBuilder().makeNode(classSchema);
        this.parentMap = new ArrayMap<>(this.objectNode.getMaxSchemaId());
        this.rootObject = rootValue != null ? rootValue : classSchema.newInstance();
        parentMap.put(1, rootObject);
    }


    Object getRootObject() {
        return rootObject;
    }


    private ContainerOfObjectSchema(ArrayMap<Object> parentMap, _ObjectNode objectNode) {
        this.parentMap = parentMap;
        this.objectNode = objectNode;
    }

    // todo: Setter에서 문제가 발생할 수 있다.
    // 빈 오브젝트나 비 컬렉션을 먼저 Setter로 넣고, 그 다음에 컬렉션을 넣으면 의미가 없음.

    @Override
    public void put(String key, Object value) {

        if(this.whildObjectIdList != null) {
            for(int id : whildObjectIdList) {
                Object object = parentMap.get(id);
                _SchemaPointer schemaPointer = this.objectNode.getClassSchemaPointer(id);
                if(schemaPointer == null) {
                    continue;
                }
                SchemaValueAbs schemaValueAbs = schemaPointer.getSchema();

                if(object instanceof Map) {
                        Object convertedValue = schemaValueAbs.convertValue(value);
                        //noinspection unchecked
                        ((Map<String, Object>) object).put(key, convertedValue);
                }

            }
        }
        _AbsNode childNode = this.objectNode.getNode(key);
        if(childNode == null) {
            return;
        }
        _NodeType nodeType = childNode.getType();
        _ObjectNode childObjectNode;

        switch (nodeType) {
            case OBJECT:
                List<Integer> mapIdList = null;
                childObjectNode = (_ObjectNode)childNode;
                List<_SchemaPointer> nodeSchemaPointers = childObjectNode.getNodeSchemaPointerList();
                if(nodeSchemaPointers != null) {
                    for(_SchemaPointer schemaPointer : nodeSchemaPointers) {
                        int id = schemaPointer.getId();
                        int parentId = schemaPointer.getParentId();
                        Object object = parentMap.get(id);
                        if(object == null) {
                            SchemaValueAbs schemaValue = schemaPointer.getSchema();
                            SchemaType schemaType = schemaValue.getSchemaType();
                            if(schemaType == SchemaType.Map) {
                                object = schemaValue.newInstance();
                                if(mapIdList == null) {
                                    mapIdList = new ArrayList<>();
                                }
                                mapIdList.add(id);
                            } else {
                                ClassSchema classSchema = schemaValue.getClassSchema();
                                object = classSchema.newInstance();
                            }
                            parentMap.put(id, object);
                            Object parent = parentMap.get(parentId);
                            final Object finalObject = object;
                            setValueExecutorList.add(() -> schemaValue.setValue(parent, finalObject));

                        }
                    }
                }
                if(value instanceof KeyValueDataContainerWrapper) {
                    KeyValueDataContainerWrapper wrapper = (KeyValueDataContainerWrapper) value;
                    ContainerOfObjectSchema childContainer = new ContainerOfObjectSchema(parentMap, childObjectNode);
                    wrapper.setContainer(childContainer);
                    if(mapIdList != null) {
                        childContainer.whildObjectIdList = mapIdList;
                    }
                }
                break;
            case COLLECTION_OBJECT:
                _CollectionNode collectionNode = (_CollectionNode)childNode;
                if(!(value instanceof ArrayDataContainerWrapper)) {
                    return;
                }
                ArrayDataContainerWrapper arrayDataContainerWrapper = (ArrayDataContainerWrapper)value;
                ContainerOfCollectionMapping collectionMappingContainer = new ContainerOfCollectionMapping(collectionNode, parentMap);
                arrayDataContainerWrapper.setContainer(collectionMappingContainer);

                // 필드에 컬렉션 추가.





                break;
            case VALUE:
                childObjectNode = (_ObjectNode)childNode;
                childObjectNode.getFieldSchemedPointerList();
                List<_SchemaPointer> fieldSchemedPointerList = childObjectNode.getFieldSchemedPointerList();
                for(_SchemaPointer schemaPointer : fieldSchemedPointerList) {
                    int parentId = schemaPointer.getParentId();
                    Object parent = parentMap.get(parentId);
                    if(parent == null) {
                        continue;
                    }
                    ISchemaNode schemaNode = schemaPointer.getSchema();
                    SchemaType schemaType = schemaNode.getSchemaType();
                    switch (schemaType) {
                        case Map:
                            setMapField(parent, (SchemaFieldMap) schemaNode, (KeyValueDataContainerWrapper)value);
                            break;
                        default:
                            SchemaValueAbs schemaFieldNormal = (SchemaValueAbs)schemaNode;
                            schemaFieldNormal.setValue(parent, value);
                    }
                }
                break;
            /*case NORMAL_FIELD:

                assert iSchemaNode instanceof SchemaFieldNormal;
                SchemaFieldNormal schemaNode = (SchemaFieldNormal) iSchemaNode;

                int id = schemaNode.getId();

                /*Map<Integer, Object> map = mapThreadLocal.get();
                Object parent = map.get(id);
                if(parent == null) {
                    parent = getRootValue();
                    if(!schemaNode.isDeclaredType(parent.getClass())) {
                        break;
                    }
                }*/
                //((SchemaFieldNormal)iSchemaNode).setValue(parentMap,value);
                //break;
            default:
                break;
        }
    }

    @Override
    public void end() {
        setValueExecutorList.forEach(Runnable::run);
    }

    private void setMapField(Object parent, SchemaFieldMap schemaNode, KeyValueDataContainerWrapper value) {
        ContainerOfStringMapKeyValue stringMapKeyValueContainer = new ContainerOfStringMapKeyValue();
        value.setContainer(stringMapKeyValueContainer);
        Map<String, Object> map = stringMapKeyValueContainer.getMap();
        schemaNode.setValue(parent, map);
    }


    @Override
    public Object get(String key) {




        return null;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void setComment(String key, String comment, CommentPosition type) {

    }

    @Override
    public String getComment(String key, CommentPosition type) {
        return "";
    }

    @Override
    public CommentObject<String> getCommentObject(String key) {
        return null;
    }

    @Override
    public Set<String> keySet() {
        //return Set.of();
        return null;
    }

    @Override
    public String getLastAccessedKey() {
        return "";
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setSourceFormat(FormatType formatType) {

    }

    @Override
    public void setComment(CommentObject<?> commentObject) {

    }

    @Override
    public DataIterator<?> iterator() {
        return null;
    }


    public static class ObjectSchemaContainerFactory implements KeyValueDataContainerFactory {

        private Object refrenceObject;



        ObjectSchemaContainerFactory(Object object) {
            this.refrenceObject = object;
        }



        @Override
        public KeyValueDataContainer create() {
            if(refrenceObject != null) {
                Object arg = refrenceObject;
                refrenceObject = null;
                return new ContainerOfObjectSchema(arg);
            }
            return new KeyValueDataContainerWrapper();

        }
    }

}
