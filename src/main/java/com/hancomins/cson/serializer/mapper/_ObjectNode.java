package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.CSONException;
import com.hancomins.cson.ErrorMessage;
import com.hancomins.cson.util.ArrayMap;

import java.util.*;

public class _ObjectNode extends _AbsNode{

    private Map<String, _AbsNode> children;

    private _ObjectNode parent;
    private ArrayMap<_SchemaPointer> classSchemaPointerMap = new ArrayMap<>();
    private List<_SchemaPointer> fieldSchemedPointerList = new ArrayList<>();
    private String comment;
    private String afterComment;
    private String name;

    /**
     * Map 또는 CSONObject 타입의 필드면 true
     * 어떤 값이든 넣을 수 있다.
     */
    private boolean isWildItem = false;

    private int maxSchemaId = 0;





    public _ObjectNode() {
    }

    _ObjectNode setComment(String comment, String afterComment) {
        this.comment = comment;
        this.afterComment = afterComment;
        return this;
    }


    _ObjectNode getObjectNode(String key) {
        if(this.children == null) {
            return null;
        }
        _AbsNode absNode = children.get(key);
        if(!(absNode instanceof _ObjectNode)) {
            return null;
        }
        return (_ObjectNode) absNode;
    }





    void selectCollectionItem() {
        if(fieldSchemedPointerList != null) {
            fieldSchemedPointerList.clear();
        }
        if(classSchemaPointerMap == null){
            return;
        }
        for(_SchemaPointer pointer : classSchemaPointerMap.values()) {
            pointer.setCollectionItem(true);
        }
    }

    _SchemaPointer getNodeSchemaPointer(int id) {
        if(classSchemaPointerMap == null) {
            return null;
        }
        return classSchemaPointerMap.get(id);
    }


    void putNodeSchema(ISchemaNode iSchemaNode, int id, int parentId) {
        _SchemaPointer pointer= null;
        if(iSchemaNode instanceof ClassSchema) {
            pointer = new _SchemaPointer((ClassSchema)iSchemaNode, id, parentId);
        } else if(iSchemaNode instanceof SchemaValueAbs) {
            pointer = new _SchemaPointer((SchemaValueAbs) iSchemaNode, id, parentId);
        }

        if(pointer != null && pointer.getId() != _SchemaPointer.NO_ID) {
            if(classSchemaPointerMap == null) {
                classSchemaPointerMap = new ArrayMap<>();
            }
            classSchemaPointerMap.put(pointer.getId(), pointer);
        }
        /*if(pointer.getParentId() != _SchemaPointer.NO_ID) {
            if(fieldSchemedPointerList == null) {
                fieldSchemedPointerList = new ArrayList<>();
            }
            if(iSchemaNode instanceof ClassSchema) {
                System.out.println("ClassSchema");
            }
            fieldSchemedPointerList.add(pointer);
        }*/
    }

    void putFieldSchema(ISchemaNode schema, int parentId) {
        if(schema instanceof SchemaValueAbs) {
            SchemaValueAbs schemaValueAbs = (SchemaValueAbs) schema;
            List<SchemaValueAbs> allSchemaValueAbsList = schemaValueAbs.getAllSchemaValueList();
            int allSchemaValueAbsListSize = allSchemaValueAbsList.size();
            if(allSchemaValueAbsListSize > 1) {
                for(int i = 1; i < allSchemaValueAbsListSize; ++i) {
                    putFieldSchema(allSchemaValueAbsList.get(i), parentId);
                }
            }
        }

        _SchemaPointer pointer = new _SchemaPointer(schema, _SchemaPointer.NO_ID, parentId);
        if(fieldSchemedPointerList == null) {
            fieldSchemedPointerList = new ArrayList<>();
        } else if(fieldSchemedPointerList.contains(pointer)) {
                return;
        }
        fieldSchemedPointerList.add(pointer);
    }





    void putNode(String key, _AbsNode node) {
        if(children == null) {
            children = new HashMap<>();
        }
        children.put(key, node);
    }

    _AbsNode getNode(String key) {
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

    void setWildItem(boolean isWildItem) {
        this.isWildItem = isWildItem;
    }

    boolean isWildItem() {
        return isWildItem;
    }

    List<_SchemaPointer> getNodeSchemaPointerList() {
        return classSchemaPointerMap == null ? null : (List<_SchemaPointer>) classSchemaPointerMap.values();
    }

    _SchemaPointer getFirstSchemaPointer() {
        if(classSchemaPointerMap == null || classSchemaPointerMap.isEmpty()) {
            return null;
        }
        return classSchemaPointerMap.get(0);
    }


    private void mergeSchemas(_ObjectNode node) {
        if(node.classSchemaPointerMap != null) {
            if(classSchemaPointerMap == null) {
                classSchemaPointerMap = new ArrayMap<>();
            }
            classSchemaPointerMap.putAll(node.classSchemaPointerMap);
        }
        if(node.fieldSchemedPointerList != null) {
            if(fieldSchemedPointerList == null) {
                fieldSchemedPointerList = new ArrayList<>();
            }
            for(_SchemaPointer pointer : node.fieldSchemedPointerList) {
                if(fieldSchemedPointerList.contains(pointer)) {
                    continue;
                }
                fieldSchemedPointerList.add(pointer);
            }
        }
    }

    /*private String makeThisPath() {
        if(parent == null) {
            return name == null ? "" : name;
        }
        String path = parent.makeThisPath();
        if(path.isEmpty()) {
            return name;
        }
        return name  + "." + name;
    }*/

    @Override
    void merge(_AbsNode absNode) {

        _NodeType inputNodeType = absNode.getType();
        _NodeType thisType = getType();


        if((inputNodeType != thisType) && (getType() == _NodeType.VALUE || inputNodeType == _NodeType.VALUE) ) {
            throw new CSONException(ErrorMessage.CONFLICT_KEY_VALUE_TYPE.formatMessage(name));
        }
        if(absNode.isArrayType()) {
            //todo: 에러 메시지
            throw  new CSONException("Array ");
        }

        _ObjectNode node = (_ObjectNode) absNode;

        setWildItem(node.isWildItem());


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





    List<_SchemaPointer> getFieldSchemedPointerList() {
        return fieldSchemedPointerList;
    }

    /*

    String toString(int indent) {
        StringBuilder indentString = new StringBuilder();
        for(int i = 0; i < indent; i++) {
            indentString.append("\t");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(indentString).append("{\n");
        if(comment != null) {
            sb.append(indentString).append("\tcomment:\"").append(comment).append("\"\n");
        }
        if(afterComment != null) {
            sb.append(indentString).append("\tafterComment:\"").append(afterComment).append("\"\n");
        }
        if(fieldSchemedPointerList != null && !fieldSchemedPointerList.isEmpty()) {
            sb.append(indentString).append("\tfileSchemedPointerList:[").append("\n");
            for(_SchemaPointer pointer : fieldSchemedPointerList) {
                sb.append(indentString).append("\t\t").append(pointer.toString()).append(",\n");
            }
            sb.append(indentString).append("\t],\n");
        }

        if(classSchemaPointerMap != null) {
            sb.append(indentString).append("\tclassSchemaPointerMap:[").append("\n");
            for(_SchemaPointer pointer : classSchemaPointerMap.values()) {
                sb.append(indentString).append("\t\t").append(pointer.toString()).append(",\n");
            }
            sb.append(indentString).append("\t],\n");
        }

        sb.append(indentString).append("\tchildren:{\n");
        if(children != null) {
            for (Map.Entry<String, _ObjectNode> entry : children.entrySet()) {
                sb.append(indentString).append("\t\t").append(entry.getKey()).append(":\n").append(entry.getValue().toString(indent + 2)).append(",\n");
            }
            sb.append(indentString).append("\t\t}");
        } else {
            sb.append(indentString).append("\t\t\t}\n");
        }
        sb.append(indentString).append("}\n");

        return sb.toString();
    }*/

    _ObjectNode setMaxSchemaId(int maxSchemaId) {
        this.maxSchemaId = maxSchemaId;
        return this;
    }

    int getMaxSchemaId() {
        return maxSchemaId;
    }

    /*@Override
    public String toString() {
        return toString(0);
    }*/

}
