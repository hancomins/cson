package com.hancomins.cson.serializer.mapper;


public class SchemaArrayNode extends SchemaObjectNode {

    @Override
    public _SchemaType getNodeType() {
        return _SchemaType.ARRAY;
    }
}
