package com.hancomins.cson.serializer;

import com.hancomins.cson.CSONArray;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

class SchemaMethodForArrayType extends SchemaMethod implements ISchemaArrayValue {




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

    private final List<CollectionItems> collectionBundles;
    protected final Types endpointValueType;
    private final TypeSchema objectValueTypeSchema;


    SchemaMethodForArrayType(TypeSchema parentsTypeSchema, Method method) {
        super(parentsTypeSchema, method);


        boolean isGetter = getMethodType() == MethodType.Getter;
        Type genericFieldType = isGetter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
        }


        this.collectionBundles = ISchemaArrayValue.getGenericType(genericFieldType, methodPath);
        CollectionItems lastCollectionItems = this.collectionBundles.get(this.collectionBundles.size() - 1);
        Class<?> valueClass = lastCollectionItems.getValueClass();
        endpointValueType = lastCollectionItems.isGeneric() ? Types.GenericType : Types.of(valueClass);
        if (endpointValueType == Types.Object) {
            objectValueTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeSchema = null;
        }

    }



    @Override
    public Types getEndpointValueType() {
        return this.endpointValueType;
    }

    @Override
    public TypeSchema getObjectValueTypeElement() {
        return this.objectValueTypeSchema;
    }

    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }


    @Override
    public boolean isAbstractType() {
        return endpointValueType == Types.AbstractObject;
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
