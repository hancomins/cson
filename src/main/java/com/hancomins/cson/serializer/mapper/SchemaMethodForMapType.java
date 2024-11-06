package com.hancomins.cson.serializer.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

class SchemaMethodForMapType extends SchemaMethod implements ISchemaMapValue {


    @SuppressWarnings("DuplicatedCode")
    static boolean isMapTypeParameterOrReturns(Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);

        if(getter != null && Map.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        Class<?>[] types = method.getParameterTypes();
        if(setter != null && types.length == 1 && Map.class.isAssignableFrom(types[0])) {
            return true;
        }
        return false;
    }

    private final Constructor<?> constructorMap;
    private final Class<?> elementClass;
    private final boolean isGenericTypeValue;
    private final boolean isAbstractValue;

    private final String methodPath;

    SchemaMethodForMapType(TypeSchema parentsTypeSchema, Method method) {
        super(parentsTypeSchema, method);

        boolean isGetter = getMethodType() == MethodType.Getter;
        Type genericType = isGetter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
        }
        this.methodPath = methodPath;


        Map.Entry<Class<?>, Type> entry = ISchemaMapValue.readKeyValueGenericType(genericType, methodPath);
        Class<?> keyClass = entry.getKey();
        Type valueType = entry.getValue();
        boolean isGenericValue = false;
        if(valueType instanceof Class<?>) {
            this.elementClass = (Class<?>)valueType;
        } else if(valueType instanceof TypeVariable) {
            this.elementClass = Object.class;
            isGenericValue = true;

        } else {
            this.elementClass = null;
        }
        isGenericTypeValue = isGenericValue;

        if(elementClass != null && !isGenericValue) {
            ISchemaValue.assertValueType(elementClass, methodPath);
        }
        ISchemaMapValue.assertCollectionOrMapValue(elementClass,methodPath);



        if(!String.class.isAssignableFrom(keyClass)) {
            if(isGetter) {
                throw new CSONSerializerException("The key of Map, which is the return value of the method, is not of String type. Please use String key. (path: " + methodPath + ")");
            } else {
                throw new CSONSerializerException("The key of Map, which is the parameter of the method, is not of String type. Please use String key. (path: " + methodPath + ")");
            }
        }
        constructorMap = ISchemaMapValue.constructorOfMap(getValueTypeClass());
        isAbstractValue = elementClass != null && elementClass.isInterface() || java.lang.reflect.Modifier.isAbstract(elementClass.getModifiers());

    }





    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaMapValue)) {
            return false;
        }
        ISchemaMapValue mapValue = (ISchemaMapValue)schemaValueAbs;
        if(elementClass != null && !elementClass.equals( mapValue.getElementType())) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }

    @Override
    public Class<?> getElementType() {
        return elementClass;
    }

    @Override
    public Object newInstance() {
        try {
            return constructorMap.newInstance();
        } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new CSONSerializerException("Map type " + getValueTypeClass().getName() + " has no default constructor. (path: " + methodPath + ")", e);
        }
    }

    @Override
    public boolean isGenericValue() {
        return isGenericTypeValue;
    }

    @Override
    public boolean isAbstractType() {
        return isAbstractValue;
    }

}
