package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.container.ArrayDataContainer;
import com.hancomins.cson.container.ArrayDataContainerWrapper;
import com.hancomins.cson.container.DataIterator;
import com.hancomins.cson.container.FormatType;
import com.hancomins.cson.util.ArrayMap;
import com.hancomins.cson.util.DataConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionMappingContainer implements ArrayDataContainer {


    private CollectionMappingContainer parentContainer;
    private _CollectionNode rootNode;
    @SuppressWarnings("rawtypes")
    private List<ValueBundle> values = new ArrayList<>();
    int depth = 0;





    @SuppressWarnings("unchecked")
    CollectionMappingContainer(_CollectionNode rootNode, ArrayMap<Object> values) {
        this.rootNode = rootNode;
        List<_ArraySchemePointer> arraySchemePointerList =  rootNode.getArraySchemaPointers();

        for(_ArraySchemePointer arraySchemePointer : arraySchemePointerList) {
            ISchemaArrayValue iSchemaArrayValue = arraySchemePointer.getSchema();
            CollectionItem collectionItem = iSchemaArrayValue.getCollectionItem();
            if(collectionItem == null) {
                continue;
            }
            Collection<Object> collection = (Collection<Object>)collectionItem.newInstance();
            int parentID = arraySchemePointer.getParentId();
            iSchemaArrayValue.setValue(values.get(parentID), collection);
            this.values.add(new ValueBundle(collection,iSchemaArrayValue));
        }
    }


    CollectionMappingContainer(CollectionMappingContainer parentContainer, _CollectionNode rootNode,int depth) {
        this.values = new ArrayList<>();
        List<ValueBundle> parentValues = parentContainer.values;
        this.depth = depth;
        this.rootNode = rootNode;
        List<_ArraySchemePointer> arraySchemePointerList =  rootNode.getArraySchemaPointers();
        for(int i = 0,n = arraySchemePointerList.size(); i < n; i++) {
            ISchemaArrayValue iSchemaArrayValue = arraySchemePointerList.get(i).getSchema();
            CollectionItem collectionItem = iSchemaArrayValue.getCollectionItem(depth);
            ValueBundle parentValueBundle = parentValues.get(i);
            if(collectionItem == null || !parentValueBundle.isAvailable()) {
                this.values.add(ValueBundle.notAvailable());
                continue;
            }
            Collection<Object> collection = (Collection<Object>)collectionItem.newInstance();
            this.values.add(ValueBundle.create(collection,iSchemaArrayValue));
            parentValueBundle.collection.add(collection);
        }
    }


    @Override
    public void add(Object value) {
        for(ValueBundle collectionBundle : values) {
            if(value instanceof ArrayDataContainerWrapper) {
                ArrayDataContainerWrapper arrayDataContainerWrapper = (ArrayDataContainerWrapper)value;
                ArrayDataContainer collectionContainer = arrayDataContainerWrapper.getContainer();
                if(collectionContainer == null) {
                    CollectionMappingContainer innerCollection = new CollectionMappingContainer(this,rootNode,depth + 1);
                    arrayDataContainerWrapper.setContainer(innerCollection);
                }
            } else {
                Object convertedValue = DataConverter.convertValue(collectionBundle.schemaArrayValue.getEndpointValueTypeClass(), value);
                collectionBundle.collection.add(convertedValue);
            }
        }

    }

    @Override
    public Object get(int index) {
        return null;
    }

    @Override
    public void set(int index, Object value) {

    }

    @Override
    public void setComment(int index, String comment, CommentPosition position) {

    }

    @Override
    public String getComment(int index, CommentPosition position) {
        return "";
    }

    @Override
    public void remove(int index) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setSourceFormat(FormatType formatType) {

    }

    @Override
    public void setComment(CommentObject<?> commentObject) {

    }

    @Override
    public DataIterator<?> iterator() {
        return null;
    }

    @Override
    public CommentObject<Integer> getCommentObject(int index) {
        return null;
    }


    private static class ValueBundle {
        static ValueBundle notAvailable() {
            ValueBundle valueBundle = new ValueBundle();
            valueBundle.isAvailable = false;
            return valueBundle;
        }


        static ValueBundle create(Collection<Object> collection,ISchemaArrayValue schemaArrayValue) {
            return new ValueBundle(collection,schemaArrayValue);
        }


        ValueBundle(Collection<Object> collection,ISchemaArrayValue schemaArrayValue) {
            this.collection = collection;
            this.schemaArrayValue = schemaArrayValue;
        }

        private ValueBundle() {
        }

        private boolean isAvailable() {
            return isAvailable;
        }


        Collection<Object> collection;
        ISchemaArrayValue schemaArrayValue;
        int level = 0;
        boolean isAvailable = true;


    }
}
