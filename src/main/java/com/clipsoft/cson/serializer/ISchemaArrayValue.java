package com.clipsoft.cson.serializer;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

interface ISchemaArrayValue extends ISchemaValue {

     Types getEndpointValueType();

     TypeElement getObjectValueTypeElement();

     List<CollectionItems> getCollectionItems();



     default boolean isGenericTypeValue() {
         int size = getCollectionItems().size();
         CollectionItems collectionItems = getCollectionItems().get(size - 1);
         return collectionItems.isGeneric();
     }




     static boolean equalsCollectionTypes(List<CollectionItems> a, List<CollectionItems> b) {
            if(a.size() != b.size()) {
                return false;
            }
            for(int i = 0; i < a.size(); i++) {
                Constructor<?> aConstructor = a.get(i).collectionConstructor;
                Constructor<?> bConstructor = b.get(i).collectionConstructor;

                if(!aConstructor.equals(bConstructor)) {
                    return false;
                }
            }
            return true;
     }


    static List<CollectionItems> getGenericType(Type type, String path) {
        if(type == null) {
            throw new CSONObjectException("Unknown collection or RAW type. Collections must use <generic> types. (path: " + path + ")");
        }
        Type genericFieldType = type;
        ArrayList<CollectionItems> result = new ArrayList<>();
        if (genericFieldType instanceof ParameterizedType) {
            CollectionItems collectionBundle = new CollectionItems((ParameterizedType) genericFieldType);
            result.add(collectionBundle);
            return getGenericType(result,(ParameterizedType)genericFieldType, path);
        } else  {
            throw new CSONObjectException("Collections must use <generic> types. (path: " + path + ")");
        }
    }

    static List<CollectionItems>  getGenericType(List<CollectionItems> collectionBundles, ParameterizedType parameterizedType, String path) {
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
                CollectionItems collectionItems = collectionBundles.get(collectionBundles.size() - 1);
                collectionItems.setValueClass((Class<?>)rawType);
                return collectionBundles;
            }
            CollectionItems collectionBundle = new CollectionItems(parameterizedType);
            collectionBundles.add(collectionBundle);
            return getGenericType(collectionBundles,parameterizedType, path);
        }
        else if(fieldArgTypes[0] instanceof TypeVariable) {
            CollectionItems collectionItems = collectionBundles.get(collectionBundles.size() - 1);
            collectionItems.setGenericTypeName(((TypeVariable) fieldArgTypes[0]).getName());

            collectionItems.setValueClass(Object.class);
            return collectionBundles;
        }
        else {
            throw new CSONObjectException("Invalid collection or RAW type. Collections must use <generic> types. (path: " + path + ")");
        }
    }






}
