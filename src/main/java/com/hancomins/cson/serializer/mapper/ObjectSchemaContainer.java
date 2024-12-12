package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.container.*;
import com.hancomins.cson.util.ArrayMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectSchemaContainer implements KeyValueDataContainer {

    private _ObjectNode objectNode;
    private Object rootObject;

    private ArrayMap<Object> parentMap = null;

    public ObjectSchemaContainer(Class<?> classType) {
        ClassSchema classSchema = ClassSchemaMap.getInstance().getClassSchema(classType);
        initRootNode(classSchema, null);
    }

    public ObjectSchemaContainer(Object rootValue) {
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


    private ObjectSchemaContainer(ArrayMap<Object> parentMap, _ObjectNode objectNode) {
        this.parentMap = parentMap;
        this.objectNode = objectNode;
    }




    @Override
    public void put(String key, Object value) {
        _AbsNode childNode = this.objectNode.getNode(key);
        if(childNode == null) {
            return;
        }
        _NodeType nodeType = childNode.getType();
        _ObjectNode childObjectNode;
        switch (nodeType) {
            case OBJECT:
                childObjectNode = (_ObjectNode)childNode;
                List<_SchemaPointer> nodeSchemaPointers = childObjectNode.getNodeSchemaPointerList();
                if(nodeSchemaPointers != null) {
                    for(_SchemaPointer schemaPointer : nodeSchemaPointers) {
                        int id = schemaPointer.getId();
                        int parentId = schemaPointer.getParentId();
                        Object object = parentMap.get(id);
                        if(object == null) {
                            SchemaValueAbs schemaValue = schemaPointer.getSchema();
                            ClassSchema classSchema = schemaValue.getClassSchema();
                            object = classSchema.newInstance();
                            parentMap.put(id, object);
                            Object parent = parentMap.get(parentId);
                            schemaValue.setValue(parent, object);
                        }
                    }
                }
                if(value instanceof KeyValueDataContainerWrapper) {
                    KeyValueDataContainerWrapper wrapper = (KeyValueDataContainerWrapper) value;
                    wrapper.setContainer(new ObjectSchemaContainer(parentMap, childObjectNode));
                }
                break;
            case COLLECTION_OBJECT:
                _CollectionNode collectionNode = (_CollectionNode)childNode;
                if(!(value instanceof ArrayDataContainerWrapper)) {
                    return;
                }
                ArrayDataContainerWrapper arrayDataContainerWrapper = (ArrayDataContainerWrapper)value;
                CollectionMappingContainer collectionMappingContainer = new CollectionMappingContainer(collectionNode, parentMap);
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
                    if(schemaNode instanceof SchemaFieldMap && value instanceof KeyValueDataContainerWrapper) {
                        SchemaFieldMap schemaFieldMap = (SchemaFieldMap)schemaNode;
                        KeyValueDataContainerWrapper wrapper = (KeyValueDataContainerWrapper)value;
                        StringMapKeyValueContainer stringMapKeyValueContainer = new StringMapKeyValueContainer();
                        wrapper.setContainer(stringMapKeyValueContainer);
                        Map<String, Object> map = stringMapKeyValueContainer.getMap();
                        schemaFieldMap.setValue(parent, map);
                    } else if(schemaNode instanceof SchemaFieldNormal) {
                        SchemaFieldNormal schemaFieldNormal = (SchemaFieldNormal)schemaNode;
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
                return new ObjectSchemaContainer(arg);
            }
            return new KeyValueDataContainerWrapper();

        }
    }

}
