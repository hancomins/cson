package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONArray;
import com.hancomins.cson.serializer.CSONValueGetter;
import com.hancomins.cson.serializer.CSONValueSetter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

class SetterGetterSchemaUseCollection extends SchemaMethod implements ISchemaArrayValue {




    @SuppressWarnings("DuplicatedCode")
    static boolean isCollectionTypeParameterOrReturns(Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);
        if(getter != null && CSONArray.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        else if(getter != null && Collection.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        Class<?>[] types = method.getParameterTypes();
        if(setter != null && types.length == 1) {
            if(CSONArray.class.isAssignableFrom(types[0])) {
                return false;
            } else if(Collection.class.isAssignableFrom(types[0])) {
                return true;
            }
        }
        return false;
    }

    private final List<CollectionItem> collectionBundles;
    protected final SchemaType endpointValueType;



    SetterGetterSchemaUseCollection(ClassSchema parentsTypeSchema, Method method) {
        super(parentsTypeSchema, method);
        boolean isGetter = getMethodType() == MethodType.Getter;
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
        }

        this.collectionBundles = isGetter ? CollectionItem.buildCollectionItemsByMethodReturn(method) : CollectionItem.buildCollectionItemsByParameter(method, 0);
        CollectionItem lastCollectionItem = this.collectionBundles.get(this.collectionBundles.size() - 1);
        Class<?> valueClass = lastCollectionItem.getValueClass();
        endpointValueType = lastCollectionItem.isGeneric() ? SchemaType.GenericType : SchemaType.of(valueClass);
        if (endpointValueType == SchemaType.Object) {
            setObjectTypeSchema(ClassSchemaMap.getInstance().getClassSchema(valueClass));
        } else {
            setObjectTypeSchema(null);
        }

    }



    @Override
    public SchemaType getEndpointValueType() {
        return this.endpointValueType;
    }

    @Override
    public Class<?> getEndpointValueTypeClass() {
        return collectionBundles.get(collectionBundles.size() - 1).getValueClass();
    }


    @Override
    public List<CollectionItem> getCollectionItems() {
        return collectionBundles;
    }


    @Override
    public boolean isAbstractType() {
        return endpointValueType == SchemaType.AbstractObject;
    }


    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaArrayValue)) {
            return false;
        }

        if(!ISchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((ISchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        if(this.endpointValueType != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }



}
