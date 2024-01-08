package com.clipsoft.cson.serializer;


import com.clipsoft.cson.CSONArray;
import com.clipsoft.cson.CSONElement;
import com.clipsoft.cson.CSONObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

class TypeElement {


    protected static final TypeElement CSON_OBJECT;

    static {
        try {
            CSON_OBJECT = new TypeElement(CSONObject.class, CSONObject.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected static final TypeElement CSON_ARRAY;

    static {
        try {
            CSON_ARRAY = new TypeElement(CSONArray.class, CSONArray.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<?> type;
    private final Constructor<?> constructor;
    private final ConcurrentHashMap<String,  ObjectObtainorMethodRack> fieldValueObtaiorMap = new ConcurrentHashMap<>();

    private SchemaObjectNode schema;

    private final String comment;
    private final String commentAfter;


    protected SchemaObjectNode getSchema() {
        if(schema == null) {
            schema = NodePath.makeSchema(this,null);
        }
        return schema;
    }

    protected synchronized static TypeElement create(Class<?> type) {
        if(CSONObject.class.isAssignableFrom(type)) {
            return CSON_OBJECT;
        }
        if(CSONArray.class.isAssignableFrom(type)) {
            return CSON_ARRAY;
        }

        checkCSONAnnotation(type);
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
        return new TypeElement(type, constructor);
    }



    protected Object newInstance() {
        try {
            if(constructor == null) {
                checkConstructor(type);
                return null;
            }
            return constructor.newInstance();
        } catch (Exception e) {
            throw new CSONSerializerException("Failed to create instance of " + type.getName(), e);
        }
    }

    private TypeElement(Class<?> type, Constructor<?> constructor) {
        this.type = type;

        this.constructor = constructor;
        CSON cson = type.getAnnotation(CSON.class);
        if(cson != null) {
            String commentBefore = cson.comment();
            String commentAfter = cson.commentAfter();
            this.comment = commentBefore.isEmpty() ? null : commentBefore;
            this.commentAfter = commentAfter.isEmpty() ? null : commentAfter;
        } else {
            this.comment = null;
            this.commentAfter = null;
        }
        searchFieldValueObtainor();
    }

    @SuppressWarnings("unchecked")
    private void searchFieldValueObtainor() {
        Class<?> currentType = type;
        while(currentType != Object.class && currentType != null) {
            Method[] methods = currentType.getDeclaredMethods();
            for(Method method : methods) {
                ObjectObtainor objectObtainor = method.getAnnotation(ObjectObtainor.class);
                if(objectObtainor != null) {
                    String fieldName = objectObtainor.fieldName();
                    try {
                        type.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException e) {
                        throw new CSONSerializerException("Invalid @ObjectObtainor method of " + type.getName() + "." + method.getName() + ". Field " + fieldName + " not found in " + type.getName());
                    }
                    Class<?> returnType = method.getReturnType();
                    if(returnType == void.class || returnType == Void.class || returnType == null || returnType.getAnnotation(CSON.class) == null) {
                        throw new CSONSerializerException("Invalid @ObjectObtainor method of " + type.getName() + "." + method.getName() + ".  Return type must be a class annotated with @CSON.");
                    }
                    if(method.getParameterCount() == 1) {
                        Class<?> parameterType = method.getParameterTypes()[0];
                        if(!CSONElement.class.isAssignableFrom(parameterType) && !CSONObject.class.isAssignableFrom(parameterType) && !CSONArray.class.isAssignableFrom(parameterType)) {
                            throw new CSONSerializerException("Invalid @ObjectObtainor method of " + type.getName() + "." + method.getName() + ". Parameter type only can be CSONElement or CSONObject or CSONArray");
                        }
                    }
                    if(method.getParameterCount() > 1) {
                        throw new CSONSerializerException("Invalid @ObjectObtainor method of " + type.getName() + "." + method.getName() + ".  Parameter count must be zero or one of CSONElement or CSONObject or CSONArray");
                    }
                    fieldValueObtaiorMap.put(fieldName, new ObjectObtainorMethodRack(method, returnType, method.getParameterCount() == 0 ? null : (Class<? extends CSONElement>) method.getParameterTypes()[0]));

                }


            }
            currentType= currentType.getSuperclass();
        }
    }

    Class<?> getType() {
        return type;
    }


    String getComment() {
        return comment;
    }

    String getCommentAfter() {
        return commentAfter;
    }



    private static void checkCSONAnnotation(Class<?> type) {
         Annotation a = type.getAnnotation(CSON.class);
         if(a == null) {
             throw new CSONSerializerException("Type " + type.getName() + " is not annotated with @CSON");
         }
    }

    private static void checkConstructor(Class<?> type) {
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            if(constructor == null) {
                throw new CSONSerializerException("Type " + type.getName() + " has no default constructor");
            }
        } catch (NoSuchMethodException e) {
            throw new CSONSerializerException("Type " + type.getName() + " has invalid default constructor");
        }

    }

    ObjectObtainorMethodRack findObjectObrainorRack(String fieldName) {
        return fieldValueObtaiorMap.get(fieldName);
    }



    static class ObjectObtainorMethodRack {

        private ObjectObtainorMethodRack(Method method, Class<?> returnType, Class<? extends CSONElement> parameter) {
            this.method = method;
            this.returnType = returnType;
            this.parameter = parameter;
        }

        Method method;
        Class<?> returnType;
        Class<? extends CSONElement> parameter;


        public Object obtain(Object parents, CSONElement csonElement) {
            try {
                if(parameter == null) {
                    return method.invoke(parents);
                } else {
                    return method.invoke(parents, parameter.isAssignableFrom(csonElement.getClass()) ? csonElement : null);
                }
            } catch (IllegalAccessException  | InvocationTargetException e) {
                throw new CSONSerializerException("Failed to invoke @ObjectObtainor method " + method.getName() + " of " + method.getDeclaringClass().getName(), e);
            }
        }

    }




}
