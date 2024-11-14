package com.hancomins.cson.serializer.mapper;

public class _SchemaPointer {
    public static final int NO_ID = 0;

    private ISchemaNode schemaNode;
    private int id;
    private int parentId;

    _SchemaPointer(ISchemaNode schemaNode, int id, int parentId) {
        this.schemaNode = schemaNode;
        this.id = id;
        this.parentId = parentId;
    }

    public <T extends ISchemaNode> T getSchema() {
        //noinspection unchecked
        return (T)schemaNode;
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
}
