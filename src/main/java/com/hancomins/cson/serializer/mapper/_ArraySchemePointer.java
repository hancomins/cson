package com.hancomins.cson.serializer.mapper;

import java.util.List;

public class _ArraySchemePointer extends _SchemaPointer {

    //private final List<Integer> indexList;

    _ArraySchemePointer(ISchemaArrayValue schemaValueAbs, int id, int parentId) {
        super(schemaValueAbs, id, parentId);
        //this.indexList = Collections.unmodifiableList(indexList);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


    /*public List<Integer> getIndexList() {
        return indexList;
    }*/




}
