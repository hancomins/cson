package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class _ObjectNode {

    private Map<String, _ObjectNode> children;

    private _NodeType type = _NodeType.END_POINT;
    private _ObjectNode parent;
    private ArrayMap<_SchemaPointer> classSchemaPointerMap;
    private ArrayList<_SchemaPointer> fileSchemedPointerList;
    private String comment;
    private String afterComment;
    private String name;
    private boolean endPoint = false;



    public _ObjectNode() {
    }

    _ObjectNode setComment(String comment, String afterComment) {
        this.comment = comment;
        this.afterComment = afterComment;
        return this;
    }


    _ObjectNode setNodeType(_NodeType nodeType) {
        this.type = nodeType;
        return this;
    }

    void putClassSchema(ClassSchema classSchema, int id, int parentId) {
        _SchemaPointer pointer = new _SchemaPointer(classSchema, id, parentId);
        if(pointer.getId() != _SchemaPointer.NO_ID) {
            if(classSchemaPointerMap == null) {
                classSchemaPointerMap = new ArrayMap<>();
            }
            classSchemaPointerMap.put(classSchema.getId(), pointer);
        }
        if(pointer.getParentId() != _SchemaPointer.NO_ID) {
            if(fileSchemedPointerList == null) {
                fileSchemedPointerList = new ArrayList<>();
            }
            fileSchemedPointerList.add(pointer);
        }
    }

    void putFieldSchema(ISchemaNode schema, int parentId) {
        _SchemaPointer pointer = new _SchemaPointer(schema, _SchemaPointer.NO_ID, parentId);
        if(fileSchemedPointerList == null) {
            fileSchemedPointerList = new ArrayList<>();
        } else if(fileSchemedPointerList.contains(pointer)) {
                return;
        }
        fileSchemedPointerList.add(pointer);

    }





    void putNode(String key, _ObjectNode node) {
        if(children == null) {
            children = new HashMap<>();
        }
        children.put(key, node);
    }

    _ObjectNode getNode(String key) {
        if(this.children == null) {
            return null;
        }
        return children.get(key);
    }

    _ObjectNode getParent() {
        return parent;
    }

    void setParent(_ObjectNode parent) {
        this.parent = parent;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    private void mergeSchemas(_ObjectNode node) {
        if(node.classSchemaPointerMap != null) {
            if(classSchemaPointerMap == null) {
                classSchemaPointerMap = new ArrayMap<>();
            }
            classSchemaPointerMap.putAll(node.classSchemaPointerMap);
        }
        if(node.fileSchemedPointerList != null) {
            if(fileSchemedPointerList == null) {
                fileSchemedPointerList = new ArrayList<>();
            }
            for(_SchemaPointer pointer : node.fileSchemedPointerList) {
                if(fileSchemedPointerList.contains(pointer)) {
                    continue;
                }
                fileSchemedPointerList.add(pointer);
            }
        }

    }

    void merge(_ObjectNode node) {
        mergeSchemas(node);
        if(node.children == null) {
            return;
        }
        if(children == null) {
            children = new HashMap<>();
        }
        node.children.forEach((key, value) -> {
            if(children.containsKey(key)) {
                children.get(key).merge(value);
            } else {
                children.put(key, value);
            }
        });
    }

    void setEndPoint() {
        this.endPoint = true;
    }

    boolean isEndPoint() {
        return endPoint;
    }

    List<_SchemaPointer> getFileSchemedPointerList() {
        return fileSchemedPointerList;
    }



}
