package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectSchemaContainer implements KeyValueDataContainer {



    private static ThreadLocal<Map<Integer, Object>> mapThreadLocal = new ThreadLocal<>();

    private final Class<?> classType;
    private Map<String, Mappable> schemaMap = new HashMap<>();

    private TypeSchema typeSchema;
    private SchemaObjectNode schemaRoot;

    /*
    *  가지 노드.
     */
    private SchemaObjectNode branchNode;

    private Object value;

    //private ThreadLocal<Map<Integer, Schema>>

    public ObjectSchemaContainer(Class<?> classType) {
        this.classType = classType;
        this.typeSchema = TypeSchemaMap.getInstance().getTypeInfo(classType);
        this.schemaRoot = typeSchema.getSchemaObjectNode();
        this.value = typeSchema.newInstance();
        mapThreadLocal.set(new HashMap<>());
    }

    private ObjectSchemaContainer() {
        this.classType = null;
        this.typeSchema = null;
        this.schemaRoot = null;
        this.value = null;
        mapThreadLocal.set(new HashMap<>());
    }


    public ObjectSchemaContainer(Object value) {
        this.classType = value.getClass();
        this.typeSchema = TypeSchemaMap.getInstance().getTypeInfo(classType);
        this.schemaRoot = typeSchema.getSchemaObjectNode();
        this.value = value;
        mapThreadLocal.set(new HashMap<>());
        //mapThreadLocal.get().put(this)
    }


    private void setBranchNode(SchemaObjectNode schemaObjectNode) {
        this.branchNode = schemaObjectNode;
        this.schemaRoot = schemaObjectNode;
    }




    @Override
    public void put(String key, Object value) {




        ISchemaNode iSchemaNode = this.schemaRoot.get(key);
        NodeType nodeType = iSchemaNode.getNodeType();
        switch (nodeType) {
            case OBJECT:
                if(value instanceof KeyValueDataContainerWrapper) {
                    KeyValueDataContainerWrapper keyValueDataContainerWrapper = (KeyValueDataContainerWrapper)value;
                    SchemaObjectNode schemaObjectNode = (SchemaObjectNode)iSchemaNode;
                    List<SchemaValueAbs> schemaFieldList = schemaObjectNode.getParentSchemaFieldList();


                    ObjectSchemaContainer objectSchemaContainer = null;
                    for(SchemaValueAbs schemaValueAbs : schemaFieldList) {
                        if(schemaValueAbs instanceof SchemaFieldNormal) {
                            SchemaFieldNormal schemaFieldNormal = (SchemaFieldNormal)schemaValueAbs;
                            Object instance = schemaFieldNormal.newInstance();
                            objectSchemaContainer = new ObjectSchemaContainer(instance);
                            keyValueDataContainerWrapper.setContainer(objectSchemaContainer);
                            schemaFieldNormal.setValue(this.value,instance);
                            int id = schemaFieldNormal.getId();
                            mapThreadLocal.get().put(id,instance);

                        }
                    }
                    if(schemaObjectNode.isBranchNode()) {
                        if(objectSchemaContainer == null) {
                            objectSchemaContainer = new ObjectSchemaContainer();
                            keyValueDataContainerWrapper.setContainer(objectSchemaContainer);
                        }
                        objectSchemaContainer.setBranchNode(schemaObjectNode);
                    }


                }

                break;


            case NORMAL_FIELD:

                assert iSchemaNode instanceof SchemaFieldNormal;
                SchemaFieldNormal schemaNode = (SchemaFieldNormal) iSchemaNode;

                int id = schemaNode.getId();

                Map<Integer, Object> map = mapThreadLocal.get();
                Object parent = map.get(id);
                if(parent == null) parent = this.value;

                ((SchemaFieldNormal)iSchemaNode).setValue(parent,value);
                break;
            default:
                break;
        }
    }

    @Override
    public Object get(String key) {
        ISchemaNode iSchemaNode = this.schemaRoot.get(key);
        NodeType nodeType = iSchemaNode.getNodeType();
        switch (nodeType) {
            case OBJECT:
                SchemaObjectNode schemaObjectNode = (SchemaObjectNode)iSchemaNode;
                List<SchemaValueAbs> schemaFieldList = schemaObjectNode.getParentSchemaFieldList();

            case NORMAL_FIELD:
                return ((SchemaFieldNormal)iSchemaNode).getValue(value);
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
                return new ObjectSchemaContainer(arg instanceof Class<?> ? (Class<?>)arg : arg);

            }
            return new KeyValueDataContainerWrapper();

        }
    }

}
