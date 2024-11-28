package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 컬렉션 노드.
 * 항상 엔드포인트.
 */
public class _CollectionNode extends _AbsNode {

    private final List<_ArraySchemePointer> arraySchemaPointers = new ArrayList<>();

    // 필드로 정의된 컬렉션. 또는 root.
    private boolean isFieldCollection = false;
    void setFieldCollection() {
        isFieldCollection = true;
    }


    boolean isFieldCollection() {
        return isFieldCollection;
    }


    void addArraySchemaPointer(_ArraySchemePointer pointer) {
        arraySchemaPointers.add(pointer);
    }

    List<_ArraySchemePointer> getArraySchemaPointers() {
        return arraySchemaPointers;
    }

    @Override
    public _NodeType getType() {
        return _NodeType.COLLECTION_OBJECT;
    }

    @Override
    void merge(_AbsNode absNode) {
        if(!absNode.isArrayType()) {
            // todo 예외 메시지 개선
            throw new CSONException("컬렉션 노드는 컬렉션 노드만 병합할 수 있습니다. " + absNode.getType());
        }
        _CollectionNode collectionNode = (_CollectionNode) absNode;
        for(_ArraySchemePointer pointer : collectionNode.arraySchemaPointers) {
            if(arraySchemaPointers.contains(pointer)) {
                continue;
            }
            arraySchemaPointers.add(pointer);
        }
    }
}
