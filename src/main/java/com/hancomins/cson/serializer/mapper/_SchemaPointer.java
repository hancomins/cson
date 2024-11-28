package com.hancomins.cson.serializer.mapper;

import java.util.List;

public class _SchemaPointer {
    public static final int NO_ID = 0;

    private ISchemaNode typeSchema;
    private ISchemaNode schemaNode;
    private int id;
    private int parentId;
    private List<Integer> indexList;
    private boolean collectionItem;


    _SchemaPointer(ISchemaNode schemaValueAbs, int id, int parentId) {
        this.schemaNode = schemaValueAbs;
        this.typeSchema = null;
        this.id = id;
        this.parentId = parentId;
    }


    _SchemaPointer(SchemaValueAbs schemaValueAbs, int id, int parentId) {
        this.schemaNode = schemaValueAbs;
        this.typeSchema = schemaValueAbs.getClassSchema();
        this.id = id;
        this.parentId = parentId;
    }

    _SchemaPointer(ClassSchema classSchema, int id, int parentId) {
        this.typeSchema = classSchema;
        this.id = id;
        this.parentId = parentId;
    }

    void setSchemaValue(SchemaValueAbs schemaValueAbs) {
        this.schemaNode = schemaValueAbs;
    }


    void setIndexList(List<Integer> indexList) {
        this.indexList = indexList;
    }

    List<Integer> getIndexList() {
        return indexList;
    }

    void setTypeSchema(ClassSchema classSchema) {
        this.typeSchema = classSchema;
    }


    ISchemaNode getTypeSchema() {
        return typeSchema;
    }

    public <T extends ISchemaNode> T getSchema() {
        //noinspection unchecked
        return (T)schemaNode;
    }


    public boolean isCollectionItem() {
        return collectionItem;
    }

    public void setCollectionItem(boolean collectionItem) {
        this.collectionItem = collectionItem;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof _SchemaPointer) {
            _SchemaPointer other = (_SchemaPointer)obj;
            return other.schemaNode.equals(schemaNode) && other.id == id && other.parentId == parentId;

        }
        return false;
    }

    @Override
    public String toString() {
        return "{id:" + id + ",parentId:" + parentId + ",schema:\"" + schemaNode + "\"}";
    }
}
