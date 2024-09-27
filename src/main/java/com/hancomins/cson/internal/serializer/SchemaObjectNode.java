package com.hancomins.cson.internal.serializer;




import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class SchemaObjectNode extends SchemaElementNode {

    private final Map<Object, ISchemaNode> map = new LinkedHashMap<>();

    private SchemaFieldNormal fieldRack;

    private String comment;
    private String afterComment;

    SchemaObjectNode() {}

    @Override
    protected void onBranchNode(boolean branchNode) {

    }


    SchemaFieldNormal getFieldRack() {
        return fieldRack;
    }

    SchemaObjectNode setFieldRack(SchemaFieldNormal fieldRack) {
        this.fieldRack = fieldRack;
        return this;
    }




    ISchemaNode get(Object key) {
        return map.get(key);
    }


    void put(Object key, ISchemaNode value) {
        if(value instanceof SchemaElementNode) {
            ((SchemaElementNode) value).setParent(this);
        }
        map.put(key, value);
    }


    Map<Object, ISchemaNode> getMap() {
        return map;
    }

    SchemaArrayNode getArrayNode(Object key) {
        return (SchemaArrayNode) map.get(key);
    }

    SchemaObjectNode getObjectNode(Object key) {
        return (SchemaObjectNode) map.get(key);
    }

    @Override
    public SchemaObjectNode copyNode() {
        SchemaObjectNode objectNode = new SchemaObjectNode();
        for(Map.Entry<Object, ISchemaNode> entry : map.entrySet()) {
            ISchemaNode node = entry.getValue().copyNode();
            if(node instanceof SchemaElementNode) {
                ((SchemaElementNode) node).setParentSchemaFieldList(getParentSchemaFieldList());
            }
            objectNode.put(entry.getKey(), node);
        }
        return objectNode;
    }


    void setComment(String comment) {
        this.comment = comment;
    }

    void setAfterComment(String afterComment) {
        this.afterComment = afterComment;
    }

    String getAfterComment() {
        return afterComment;
    }

    String getComment() {
        return comment;
    }

    Set<Object> keySet() {
        return map.keySet();
    }


    @Override
    public void merge(SchemaElementNode schemaElementNode) {
        if(schemaElementNode instanceof SchemaObjectNode) {
            SchemaObjectNode objectNode = (SchemaObjectNode) schemaElementNode;
            Set<Map.Entry<Object, ISchemaNode>> entrySet = objectNode.map.entrySet();
            for(Map.Entry<Object, ISchemaNode> entry : entrySet) {
                Object key = entry.getKey();
                ISchemaNode node = entry.getValue();
                ISchemaNode thisNode = map.get(key);
                if(thisNode instanceof  SchemaObjectNode && node instanceof SchemaObjectNode) {
                    ((SchemaObjectNode) thisNode).merge((SchemaObjectNode) node);
                } else if(thisNode instanceof SchemaValueAbs && node instanceof SchemaValueAbs) {
                    if(!((SchemaValueAbs)thisNode).appendDuplicatedSchemaValue((SchemaValueAbs) node)) {
                        map.put(key, node);
                    }
                }
                else {
                    map.put(key, node);
                }
            }
            mergeComment(objectNode);
        }
        addParentFieldRackAll(schemaElementNode.getParentSchemaFieldList());
        setBranchNode(schemaElementNode.isBranchNode() || this.isBranchNode());

    }

    private void mergeComment(SchemaObjectNode schemaObjectNode) {
        if(schemaObjectNode.comment != null && this.comment == null) {
            this.comment = schemaObjectNode.comment;
        } else  if(schemaObjectNode.comment != null) {
            this.comment += "\n" + schemaObjectNode.comment;
        }
        if(schemaObjectNode.afterComment != null && this.afterComment == null) {
            this.afterComment = schemaObjectNode.afterComment;
        } else if(schemaObjectNode.afterComment != null) {
            this.afterComment += "\n" + schemaObjectNode.afterComment;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        Set<Map.Entry<Object, ISchemaNode>> entrySet = map.entrySet();
        for(Map.Entry<Object, ISchemaNode> entry : entrySet) {
            int branchMode = entry.getValue() instanceof SchemaElementNode ? ((SchemaElementNode) entry.getValue()).isBranchNode() ? 1 : 0 : -1;
            stringBuilder.append(entry.getKey()).append(branchMode > 0 ? "(b)" : "").append(":").append(entry.getValue().toString()).append(",");
        }
        if(stringBuilder.length() > 1) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}
