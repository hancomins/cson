package com.hancomins.cson.serializer.mapper;

public abstract class _AbsNode {

    private _NodeType type;
    private boolean endPoint;

    protected _NodeType getType() {
        return type;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected <T extends _AbsNode> T setType(_NodeType type) {
        this.type = type;
        //noinspection unchecked
        return (T) this;
    }




    boolean isArrayType() {
        return getType() == _NodeType.COLLECTION_OBJECT || getType() == _NodeType.COLLECTION_VALUE;
    }

    abstract void merge(_AbsNode absNode);

    void setEndPoint() {
        this.endPoint = true;
        this.type = _NodeType.VALUE;
    }

    boolean isEndPoint() {
        return endPoint;
    }

}
