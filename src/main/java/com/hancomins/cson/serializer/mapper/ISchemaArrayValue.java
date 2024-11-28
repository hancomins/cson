package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

interface ISchemaArrayValue extends ISchemaValue {

     SchemaType getEndpointValueType();



     List<CollectionItem> getCollectionItems();



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


    static List<CollectionItem> getGenericType(Type type, String path) {
        if(type == null) {
            throw new CSONObjectException("Unknown collection or RAW type. Collections must use <generic> types. (path: " + path + ")");
        }
        Type genericFieldType = type;
        ArrayList<CollectionItem> result = new ArrayList<>();
        if (genericFieldType instanceof ParameterizedType) {
            CollectionItem collectionBundle = new CollectionItem((ParameterizedType) genericFieldType);
            result.add(collectionBundle);
            return getGenericType(result,(ParameterizedType)genericFieldType, path);
        } else  {
            throw new CSONObjectException("Collections must use <generic> types. (path: " + path + ")");
        }
    }

    static List<CollectionItem>  getGenericType(List<CollectionItem> collectionBundles, ParameterizedType parameterizedType, String path) {
        Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
        if(fieldArgTypes.length == 0) {
            throw new CSONObjectException("Collections must use <generic> types. (path: " + path + ")");
        }
        if(fieldArgTypes[0] instanceof Class<?>) {
            return collectionBundles;
        }
        else if (fieldArgTypes[0] instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) fieldArgTypes[0];
            Type rawType = parameterizedType.getRawType();
            if(!(rawType instanceof Class<?>) || !Collection.class.isAssignableFrom((Class<?>)rawType)) {
                assert rawType instanceof Class<?>;
                if(Map.class.isAssignableFrom((Class<?>) rawType)) {
                    throw new CSONObjectException("java.util.Map type cannot be directly used as an element of Collection. Please create a class that wraps your Map and use it as an element of the Collection. (path: " + path + ")");
                }
                ISchemaValue.assertValueType((Class<?>)rawType, path);
                CollectionItem collectionItem = collectionBundles.get(collectionBundles.size() - 1);
                collectionItem.setValueClass((Class<?>)rawType);
                return collectionBundles;
            }
            CollectionItem collectionBundle = new CollectionItem(parameterizedType);
            collectionBundles.add(collectionBundle);
            return getGenericType(collectionBundles,parameterizedType, path);
        }
        else if(fieldArgTypes[0] instanceof TypeVariable) {
            CollectionItem collectionItem = collectionBundles.get(collectionBundles.size() - 1);
            collectionItem.setGenericTypeName(((TypeVariable) fieldArgTypes[0]).getName());

            collectionItem.setValueClass(Object.class);
            return collectionBundles;
        }
        else {
            throw new CSONObjectException("Invalid collection or RAW type. Collections must use <generic> types. (path: " + path + ")");
        }
    }






}
