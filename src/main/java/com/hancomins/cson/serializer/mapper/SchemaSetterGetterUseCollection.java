package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONArray;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

class SchemaSetterGetterUseCollection extends SchemaMethod implements ISchemaArrayValue {

    private final List<GenericItem> collectionBundles;
    protected final SchemaType schemaValueType;
    private final ObtainTypeValueInvoker obtainTypeValueInvoker;
    private final Class<?> endpointClass;

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
        this.collectionBundles = isGetter ? GenericItem.analyzeMethodReturn(method) : GenericItem.analyzeParameter(method, 0);
        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(method.getName());
        endpointClass = collectionBundles.get(collectionBundles.size() - 1).getValueType();
        schemaValueType = SchemaType.of(endpointClass);
        if(schemaValueType == SchemaType.Object || schemaValueType == SchemaType.AbstractObject) {
            ClassSchema result = ClassSchemaMap.getInstance().getClassSchema(endpointClass);
            setObjectTypeSchema(result);
        }
    }



    @Override
    public List<GenericItem> getCollectionItems() {
        return collectionBundles;
    }

    @Override
    public boolean isAbstractType() {
        return schemaValueType == SchemaType.AbstractObject;
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }


    @Override
    public SchemaType getEndpointValueType() {
        return schemaValueType;
    }

    @Override
    public Class<?> getEndpointValueTypeClass() {
        return endpointClass;
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
        if(this.schemaValueType != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }



}
