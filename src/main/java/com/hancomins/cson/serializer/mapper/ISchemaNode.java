package com.hancomins.cson.serializer.mapper;

interface ISchemaNode {


    NodeType getNodeType();

    ISchemaNode copyNode();

}
