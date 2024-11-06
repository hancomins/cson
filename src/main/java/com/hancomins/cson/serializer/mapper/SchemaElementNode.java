package com.hancomins.cson.serializer.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SchemaElementNode implements ISchemaNode {
    private ISchemaNode parent;
    private List<SchemaValueAbs> parentSchemaFieldList = new ArrayList<>();

    /**
     * 브런치 노드. 참조된 필드가 없는 노드.
     */
    protected boolean isBranchNode = true;




    protected boolean isBranchNode() {
        return isBranchNode;
    }

    @SuppressWarnings("unchecked")
    protected <T extends SchemaElementNode> T setBranchNode(boolean branchNode) {
        isBranchNode = branchNode;
        this.onBranchNode(branchNode);
        return (T) this;
    }


    SchemaElementNode() {}

    ISchemaNode getParent() {
        return parent;
    }


    protected abstract void onBranchNode(boolean branchNode);


    public SchemaElementNode setParent(ISchemaNode parent) {
        this.parent = parent;
        return this;
    }

    protected List<SchemaValueAbs> getParentSchemaFieldList() {
        return parentSchemaFieldList;
    }

    protected void setParentSchemaFieldList(List<SchemaValueAbs> parentSchemaFieldList) {
        this.parentSchemaFieldList = parentSchemaFieldList;
    }

    public SchemaElementNode addParentFieldRack(SchemaValueAbs parentFieldRack) {
        if(this.parentSchemaFieldList.contains(parentFieldRack)) {
            return this;
        }
        this.parentSchemaFieldList.add(parentFieldRack);
        return this;
    }

    public SchemaElementNode addParentFieldRackAll(Collection<SchemaValueAbs> parentFieldRackCollection) {
        for(SchemaValueAbs parentFieldRack : parentFieldRackCollection) {
            addParentFieldRack(parentFieldRack);
        }
        return this;
    }

    public abstract void merge(SchemaElementNode schemaElementNode);


}
