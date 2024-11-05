package com.hancomins.cson.serializer;




public class SchemaArrayNode extends SchemaObjectNode {

    @Override
    public NodeType getNodeType() {
        return NodeType.ARRAY;
    }
}
