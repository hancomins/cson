package com.hancomins.cson.serializer.mapper;

import java.util.List;

interface ISchemaArrayValue extends ISchemaValue {

     SchemaType getEndpointValueType();

     Class<?> getEndpointValueTypeClass();


     List<GenericItem> getCollectionItems();

     default GenericItem getCollectionItem() {
         if(getCollectionItems().isEmpty()) {
             return null;
         }
            return getCollectionItems().get(0);
     }

    default GenericItem getCollectionItem(int index) {
        List<GenericItem> collectionItems = getCollectionItems();
        if(index < 0 || index >= collectionItems.size()) {
            return null;
        }
        return collectionItems.get(index);
    }







     static boolean equalsCollectionTypes(List<GenericItem> a, List<GenericItem> b) {
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
