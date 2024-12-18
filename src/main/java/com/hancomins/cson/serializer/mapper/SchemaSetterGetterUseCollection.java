package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONArray;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

class SchemaSetterGetterUseCollection extends SchemaMethod implements ISchemaArrayValue {


    private final List<CollectionItem> collectionBundles;
    protected final SchemaType valueType;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;

    @Override
    public SchemaType getSchemaType() {
        return SchemaType.Collection;
    }

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
            } else return Collection.class.isAssignableFrom(types[0]);
        }
        return false;
    }



    SchemaSetterGetterUseCollection(ClassSchema classSchema, Method method) {
        super(classSchema, method);
        boolean isGetter = getMethodType() == MethodType.Getter;
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
        }

        this.collectionBundles = isGetter ? CollectionItem.buildCollectionItemsByMethodReturn(method) : CollectionItem.buildCollectionItemsByParameter(method, 0);


        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(method.getName());
        SchemaFieldArray.SchemaArrayInitializeResult result = SchemaFieldArray.initialize(collectionBundles, classSchema, methodPath);
        valueType = result.getValueType();
        setObjectTypeSchema(result.getObjectTypeSchema());


    }



    @Override
    public List<CollectionItem> getCollectionItems() {
        return collectionBundles;
    }

    @Override
    public boolean isAbstractType() {
        return valueType == SchemaType.AbstractObject;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }


    @Override
    public SchemaType getEndpointValueType() {
        return valueType;
    }

    @Override
    public Class<?> getEndpointValueTypeClass() {
        return collectionBundles.get(collectionBundles.size() - 1).getValueClass();
    }

    @Override
    public ISchemaNode copyNode() {
        return new SchemaSetterGetterUseCollection(parentsTypeSchema, getMethod());
    }

    @Override
    public Object newInstance() {
        return collectionBundles.get(0).newInstance();
    }



    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaArrayValue)) {
            return false;
        }

        if(!ISchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((ISchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        if(this.valueType != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }



}
