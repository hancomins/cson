package com.hancomins.cson.serializer;

interface ISchemaNode {


    NodeType getNodeType();

    ISchemaNode copyNode();

}
