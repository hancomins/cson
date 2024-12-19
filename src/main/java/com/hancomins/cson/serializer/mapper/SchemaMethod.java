package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.util.DataConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


class SchemaMethod extends SchemaValueAbs implements ObtainTypeValueInvokerGetter {



    private ObtainTypeValueInvoker obtainTypeValueInvoker;

    private static Class<?> getValueType(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);

        Class<?>[] types =  method.getParameterTypes();
        if(csonValueSetter != null && csonValueGetter != null) {
            throw new CSONMapperException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must be annotated with @CSONValueGetter or @CSONValueSetter, not both");
        }

        if(csonValueSetter != null) {
            if(types.length != 1) {
                throw new CSONMapperException("Setter method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must have only one parameter");
            }
            return types[0];
        }
        else if(csonValueGetter != null) {
            Class<?> returnType = method.getReturnType();
            if(returnType == void.class || returnType == Void.class) {
                throw new CSONMapperException("Getter method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must have return type");
            }
            if(types.length != 0) {
                throw new CSONMapperException("Getter method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must have no parameter");
            }

            return returnType;
        }
        else {
            throw new CSONMapperException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must be annotated with @CSONValueGetter or @CSONValueSetter");
        }
    }


    private static String getPath(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);
        if(csonValueSetter != null) {
            String path = csonValueSetter.value().trim();
            if(path.isEmpty()) {
                path = csonValueSetter.key().trim();
            }
            if(path.isEmpty()) {
                path = setterNameFilter(method.getName());
            }
            return path.trim();
        }
        else if(csonValueGetter != null) {
            String path = csonValueGetter.value().trim();
            if(path.isEmpty()) {
                path = csonValueGetter.key().trim();
            }
            if(path.isEmpty()) {
                path = getterNameFilter(method.getName()).trim();
            }
            return path.trim();
        }
        else {
            throw new CSONMapperException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @CSONValueGetter or @CSONValueSetter");
        }
    }

    private static String setterNameFilter(String methodName) {
        if(methodName.length() > 3 && (methodName.startsWith("set") || methodName.startsWith("Set") || methodName.startsWith("SET") ||
                methodName.startsWith("put") || methodName.startsWith("Put") || methodName.startsWith("PUT") ||
                methodName.startsWith("add") || methodName.startsWith("Add") || methodName.startsWith("ADD"))) {
            String name = methodName.substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else {
            return methodName;
        }
    }

    private static java.lang.reflect.Type getGenericType(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);
        if(csonValueSetter != null) {
            return method.getParameters()[0].getParameterizedType();
        }
        else if(csonValueGetter != null) {
            return method.getGenericReturnType();
        }
        else {
            throw new CSONMapperException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must be annotated with @CSONValueGetter or @CSONValueSetter");
        }

    }

    static String getterNameFilter(String methodName) {
        if(methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("Get") || methodName.startsWith("GET"))) {
            String name =  methodName.substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else if(methodName.length() > 3 && (methodName.startsWith("is") || methodName.startsWith("Is") || methodName.startsWith("IS"))) {
            String name = methodName.substring(2);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else {
            return methodName;
        }
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        if(obtainTypeValueInvoker == null) {
            System.out.println(methodGetter.getName());
            System.out.println(methodSetter.getName());
        }

        if(obtainTypeValueInvoker != null) {
            return obtainTypeValueInvoker;
        }

        List<SchemaMethod> schemaMethods = getAllSchemaValueList();
        for(SchemaMethod schemaMethod : schemaMethods) {
            if(schemaMethod.obtainTypeValueInvoker != null) {
                return schemaMethod.obtainTypeValueInvoker;
            }
        }
        return null;

    }

    @Override
    public String targetPath() {
        if(methodSetter == null && methodGetter != null) {
            return methodGetter.getDeclaringClass().getName() + ".(Undeclared Setter)()";
        } else if(methodSetter == null && methodGetter == null) {
            return parentsTypeSchema.getType().getName() + ".(Undeclared Setter)()";
        }
       return methodSetter.getDeclaringClass().getName() + "." + methodSetter.getName() + "()";
    }

    @Override
    public boolean isIgnoreError() {
        return obtainTypeValueInvoker != null && obtainTypeValueInvoker.isIgnoreError();
    }

    static enum MethodType {
        Getter,
        Setter,
        Both

    }

    private final String methodPath;



    private static MethodType getMethodType(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);

        if(csonValueSetter != null) {
            return MethodType.Setter;
        }
        else if(csonValueGetter != null) {
            return MethodType.Getter;
        }
        else {
            throw new CSONMapperException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @CSONValueGetter or @CSONValueSetter");
        }
    }




    private MethodType methodType = null;
    private Method methodSetter;
    private Method methodGetter;

    private String comment = null;
    private String afterComment = null;

    private final boolean ignoreError;
    private final boolean isStatic;


    SchemaMethod(ClassSchema parentsTypeSchema, Method method) {
        super(parentsTypeSchema,getPath(method), getValueType(method), getGenericType(method));
        this.isStatic = java.lang.reflect.Modifier.isStatic(method.getModifiers());


        method.setAccessible(true);
        MethodType methodType = getMethodType(method);

        boolean isGetter = methodType == MethodType.Getter;
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
            ignoreError = method.getAnnotation(CSONValueGetter.class).ignoreError();
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
            ignoreError = method.getAnnotation(CSONValueSetter.class).ignoreError();
            obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(method.getName());
        }
        this.methodPath = methodPath;

        if(this.getSchemaType() != SchemaType.GenericType) {
            ISchemaValue.assertValueType(getValueTypeClass(), method.getDeclaringClass().getName() + "." + method.getName());
        }
        if(methodType == MethodType.Getter) {
            setGetter(method);
        }
        else if(methodType == MethodType.Setter) {
            setSetter(method);
        }
    }

    Method getMethod() {
        return methodGetter != null ? methodGetter : methodSetter;
    }


    private void setGetter(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        String comment = csonValueGetter.comment();
        String afterComment = csonValueGetter.commentAfterKey();
        this.comment = comment.isEmpty() ? null : comment;
        this.afterComment = afterComment.isEmpty() ? null : afterComment;
        methodGetter = method;
        if(methodType == MethodType.Setter) {
            this.methodType = MethodType.Both;
        } else {
            this.methodType = MethodType.Getter;
        }
    }

    private void setSetter(Method method) {
        methodSetter = method;
        if(methodType == MethodType.Getter) {
            this.methodType = MethodType.Both;
        } else {
            this.methodType = MethodType.Setter;
        }
        if(obtainTypeValueInvoker == null) {
            obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(method.getName());
        }

    }




    @Override
    boolean appendDuplicatedSchemaValue(SchemaValueAbs node) {
        if(this.methodType != MethodType.Both &&  node instanceof SchemaMethod &&
                this.parentsTypeSchema == node.parentsTypeSchema &&
                this.valueTypeClass == node.valueTypeClass) {

            if(node instanceof SetterGetterSchemaUseMap &&
                    this instanceof SetterGetterSchemaUseMap &&
                    !((SetterGetterSchemaUseMap) node).getElementType().equals(((SetterGetterSchemaUseMap) this).getElementType())
            ) {
                return super.appendDuplicatedSchemaValue(node);
            } else if(node instanceof SchemaSetterGetterUseCollection &&
                    this instanceof SchemaSetterGetterUseCollection &&
                    !node.equalsValueType(this))
             {
                return super.appendDuplicatedSchemaValue(node);
            }

            SchemaMethod schemaMethod = (SchemaMethod) node;
            if(schemaMethod.methodType == MethodType.Getter && this.methodType == MethodType.Setter) {
                setGetter(schemaMethod.methodGetter);
                return true;
            } else if(schemaMethod.methodType == MethodType.Setter && this.methodType == MethodType.Getter) {
                setSetter(schemaMethod.methodSetter);
                return true;
            }
        }

        return super.appendDuplicatedSchemaValue(node);
    }



    public MethodType getMethodType() {
        return methodType;
    }

    @Override
    public ISchemaNode copyNode() {
        return new SchemaMethod(parentsTypeSchema, methodSetter);
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getAfterComment() {
        return afterComment;
    }

    @Override
    public boolean isAbstractType() {
        return types() == SchemaType.AbstractObject;
    }

    @Override
    Object onGetValue(Object parentValue) {
        if(methodGetter == null) return null;
        Object parent = null;
        if(!isStatic) parent = parentValue;
        try {
            Object value = methodGetter.invoke(parent);
            if(isEnum && value != null) {
                return value.toString();
            }
            return value;

        } catch (Exception e) {
            if(ignoreError) {
                return null;
            }
            throw new CSONMapperException("Failed to invoke method " + this.methodPath, e);
        }
    }


    @Override
    void onSetValue(Object parentValue, Object value) {
        if(methodSetter == null) return;
        Object parent = null;
        if(!isStatic) parent = parentValue;
        try {
            if(isEnum) {
                try {
                    //noinspection unchecked
                    value = Enum.valueOf((Class<Enum>) valueTypeClass, value.toString());
                } catch (Exception e) {
                    value = null;
                }
            }
            if(value != null && !valueTypeClass.isAssignableFrom(value.getClass()) ) {
                value = DataConverter.convertValue(valueTypeClass, value);
                methodSetter.invoke(parent, value);
            } else {
                methodSetter.invoke(parent, value);
            }
        } catch (Throwable e) {
            if(ignoreError) {
                return;
            }
            if(e instanceof InvocationTargetException) {
                //noinspection AssignmentToCatchBlockParameter
                e = e.getCause();
            }

            throw new CSONMapperException("Failed to invoke method " + this.methodPath, e);
        }
    }





    static boolean isSchemaMethodGetter(ISchemaNode schemaValue) {
        return schemaValue.getClass() == SchemaMethod.class && (((SchemaMethod)schemaValue).getMethodType() == MethodType.Getter  || ((SchemaMethod)schemaValue).getMethodType() == MethodType.Both);
    }



}
