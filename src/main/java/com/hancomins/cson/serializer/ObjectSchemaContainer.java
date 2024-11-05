package com.hancomins.cson.serializer;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.DataIterator;
import com.hancomins.cson.format.FormatType;
import com.hancomins.cson.format.KeyValueDataContainer;
import com.hancomins.cson.serializer.mapper.Mappable;
import org.w3c.dom.Notation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectSchemaContainer implements KeyValueDataContainer {

    

    private final Class<?> classType;
    private Map<String, Mappable> schemaMap = new HashMap<>();

    private TypeSchema typeSchema;
    private SchemaObjectNode schemaRoot;

    private Object value;

    //private ThreadLocal<Map<Integer, Schema>>

    public ObjectSchemaContainer(Class<?> classType) {
        this(classType,null);
    }


    public <T> ObjectSchemaContainer(Class<T> classType,T value) {
        this.classType = classType;
        this.typeSchema = TypeSchemaMap.getInstance().getTypeInfo(classType);
        this.schemaRoot = typeSchema.getSchemaObjectNode();
        if(value == null) {
            this.value = typeSchema.newInstance();
        } else {
            this.value = value;
        }
    }



    @Override
    public void put(String key, Object value) {
        ISchemaNode iSchemaNode = this.schemaRoot.get(key);
        NodeType nodeType = iSchemaNode.getNodeType();
        switch (nodeType) {
            case OBJECT:
                SchemaObjectNode schemaObjectNode = (SchemaObjectNode)iSchemaNode;
                List<SchemaValueAbs> schemaFieldList = schemaObjectNode.getParentSchemaFieldList();

            case NORMAL_FIELD:
                assert iSchemaNode instanceof SchemaFieldNormal;
                ((SchemaFieldNormal)iSchemaNode).setValue(this.value,value);
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
}
