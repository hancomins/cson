package com.hancomins.cson.serializer.mapper;

import java.util.List;

interface ISchemaArrayValue extends ISchemaValue {

     SchemaType getEndpointValueType();

     Class<?> getEndpointValueTypeClass();


     List<CollectionItem> getCollectionItems();

     default CollectionItem getCollectionItem() {
         if(getCollectionItems().isEmpty()) {
             return null;
         }
            return getCollectionItems().get(0);
     }

    default CollectionItem getCollectionItem(int index) {
        List<CollectionItem> collectionItems = getCollectionItems();
        if(index < 0 || index >= collectionItems.size()) {
            return null;
        }
        return collectionItems.get(index);
    }






     default boolean isGenericTypeValue() {
         int size = getCollectionItems().size();
         CollectionItem collectionItem = getCollectionItems().get(size - 1);
         return collectionItem.isGeneric();
     }




     static boolean equalsCollectionTypes(List<CollectionItem> a, List<CollectionItem> b) {
            if(a.size() != b.size()) {
                return false;
            }
            for(int i = 0; i < a.size(); i++) {
                if(!a.get(i).compatibleCollectionType(b.get(i))) {
                    return false;
                }

            }
            return true;
     }




}
