package com.hancomins.cson.serializer.mapper;


public class SchemaArrayNode extends SchemaObjectNode {

    @Override
    public NodeType getNodeType() {
        return NodeType.ARRAY;
    }
}
