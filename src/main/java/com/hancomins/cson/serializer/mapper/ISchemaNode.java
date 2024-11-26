package com.hancomins.cson.serializer.mapper;

import java.util.concurrent.atomic.AtomicInteger;

interface ISchemaNode {

    AtomicInteger LAST_ID = new AtomicInteger(1);

    int getId();
    int getParentId();
    void setParentId(int parentId);
    _SchemaType getNodeType();

    ISchemaNode copyNode();

    void setValue(Object parent, Object value);
}
