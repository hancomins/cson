package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectSchemaContainer implements KeyValueDataContainer {

    private Class<?> classType;


    private SchemaObjectNode parentNode;

    /*
    *  가지 노드.
     */
    private SchemaObjectNode branchNode;
    private Object parent;


    private Map<Integer, Object> parentMap = null;

    //private ThreadLocal<Map<Integer, Schema>>

    public ObjectSchemaContainer(Object value) {
        if(value instanceof Class<?>) {
            this.classType = (Class<?>)value;
            this.parent = ClassSchemaMap.getInstance().getTypeInfo(classType).newInstance();
        } else {
            this.classType = value.getClass();
            this.parent = value;
        }
        this.parentNode = RootObjectNodeMap.getInstance().getObjectNode(classType);
        this.parentMap = new HashMap<>();
        this.parentMap.put(this.parentNode.getId(), this.parent);
    }


    private ObjectSchemaContainer(Map<Integer, Object> parentMap) {
        this.classType = null;
        this.parentNode = null;
        this.parent = null;
        this.parentMap = parentMap;
    }


    ObjectSchemaContainer(SchemaObjectNode node, Map<Integer, Object> parentMap) {

        this.parentNode = node;
        this.parentMap = parentMap;
    }


    private void setBranchNode(SchemaObjectNode schemaObjectNode) {
        this.branchNode = schemaObjectNode;
        this.parentNode = schemaObjectNode;
    }


    private void setParent(Object parent) {
        if(parent == null) {
            return;
        }
        this.parent = parent;
        this.classType = parent.getClass();
    }



    @Override
    public void put(String key, Object value) {
        ISchemaNode iSchemaNode = this.parentNode.get(key);
        if(iSchemaNode == null) {
            return;
        }
        _SchemaType nodeType = iSchemaNode.getNodeType();
        switch (nodeType) {
            case OBJECT:
                SchemaObjectNode schemaObjectNode = (SchemaObjectNode) iSchemaNode;
                if(value instanceof KeyValueDataContainerWrapper) {
                    KeyValueDataContainerWrapper keyValueDataContainerWrapper = (KeyValueDataContainerWrapper) value;
                    ObjectSchemaContainer objectSchemaContainer = null;
                    if (!keyValueDataContainerWrapper.hasContainer()) {
                        objectSchemaContainer = new ObjectSchemaContainer(schemaObjectNode, parentMap);
                        keyValueDataContainerWrapper.setContainer(objectSchemaContainer);

                        List<SchemaFieldNormal> schemaFieldList = schemaObjectNode.getSchemaFieldList();
                        if(!schemaFieldList.isEmpty()) {

                            schemaFieldList.forEach(schemaFieldNormal -> {
                                int id = schemaFieldNormal.getId();
                                this.parentMap.computeIfAbsent(id, (id_) -> {
                                    Object instance = schemaFieldNormal.newInstance();
                                    schemaFieldNormal.setValue(this.parentMap, instance);
                                    return instance;
                                });
                            });
                        }


                    }




                }

                break;


            case NORMAL_FIELD:

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

                ((SchemaFieldNormal)iSchemaNode).setValue(parentMap,value);
                break;
            default:
                break;
        }
    }

    @Override
    public Object get(String key) {
        ISchemaNode iSchemaNode = this.parentNode.get(key);
        _SchemaType nodeType = iSchemaNode.getNodeType();
        switch (nodeType) {
            case OBJECT:
                SchemaObjectNode schemaObjectNode = (SchemaObjectNode)iSchemaNode;
                List<SchemaValueAbs> schemaFieldList = schemaObjectNode.getParentSchemaFieldList();

            case NORMAL_FIELD:
                //return ((SchemaFieldNormal)iSchemaNode).getValue(parent);
            default:
                return null;
        }




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
