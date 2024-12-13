package com.hancomins.cson.serializer.mapper;

import java.util.concurrent.atomic.AtomicInteger;

interface ISchemaNode {


    SchemaType getSchemaType();

    ISchemaNode copyNode();

    void setValue(Object parent, Object value);
}
