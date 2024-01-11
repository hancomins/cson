package com.clipsoft.cson.serializer;


import com.clipsoft.cson.CSONArray;
import com.clipsoft.cson.CSONObject;
import com.clipsoft.cson.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final ConcurrentHashMap<String, ObtainTypeValueInvoker> fieldValueObtaiorMap = new ConcurrentHashMap<>();

    private SchemaObjectNode schema;

    private final String comment;
    private final String commentAfter;
    private final Set<String> genericTypeNames = new HashSet<>();


    protected SchemaObjectNode getSchema() {
        if(schema == null) {
            schema = NodePath.makeSchema(this,null);
        }
        return schema;
    }

    private static Class<?> findNoAnonymousClass(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            return superClass;
        }
        Class<?>[] interfaces = type.getInterfaces();
        if(interfaces != null && interfaces.length > 0) {
            Class<?> foundCsonInterface = null;
            for(Class<?> interfaceClass : interfaces) {
                if(interfaceClass.getAnnotation(CSON.class) != null) {
                    if(foundCsonInterface != null) {
                        String allInterfaceNames = Arrays.stream(interfaces).map(Class::getName).reduce((a, b) -> a + ", " + b).orElse("");
                        throw new CSONSerializerException("Anonymous class " + type.getName() + "(implements  " + allInterfaceNames + "), implements multiple @CSON interfaces.  Only one @CSON interface is allowed.");
                    }
                    foundCsonInterface = interfaceClass;
                }
            }
            if(foundCsonInterface != null) {
                return foundCsonInterface;
            }
        }
        return type;
    }



    protected synchronized static TypeElement create(Class<?> type) {
        type = findNoAnonymousClass(type);

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



    protected boolean containsGenericType(String name) {
        return genericTypeNames.contains(name);
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

        searchTypeParameters();
        searchMethodOfAnnotatedWithObtainTypeValue();

    }

    private void searchTypeParameters() {
        TypeVariable<?>[] typeVariables = this.type.getTypeParameters();
        for(TypeVariable<?> typeVariable : typeVariables) {
            genericTypeNames.add(typeVariable.getName());
        }
    }


    private void searchMethodOfAnnotatedWithObtainTypeValue() {
        List<ObtainTypeValueInvoker> obtainTypeValueInvokers = ObtainTypeValueInvoker.searchObtainTypeValueInvoker(this);
        for(ObtainTypeValueInvoker obtainTypeValueInvoker : obtainTypeValueInvokers) {
            // 뒤에 있는 것일수록 부모 클래스의 것이므로 덮어쓰지 않는다.
            fieldValueObtaiorMap.putIfAbsent(obtainTypeValueInvoker.getFieldName(), obtainTypeValueInvoker);
        }

    }

    @SuppressWarnings("unchecked")


    Class<?> getType() {
        return type;
    }


    String getComment() {
        return comment;
    }

    String getCommentAfter() {
        return commentAfter;
    }


    /**
     * 인터페이스를 포함한 부모 클래스 모두를 탐색하여 @CSON 어노테이션이 있는지 확인한다.
     * @param type 탐색할 클래스
     * @return
     */
    private static boolean isCSONAnnotated(Class<?> type) {
        AtomicBoolean result = new AtomicBoolean(false);
        ReflectionUtils.searchSuperClassAndInterfaces(type, (superClass) -> {
            CSON a = superClass.getAnnotation(CSON.class);
            if(a != null) {
                result.set(true);
                return false;
            }
            return true;
        });
        return false;

    }


    Set<String> getGenericTypeNames() {
        return genericTypeNames;
    }

    private static boolean isCSONAnnotatedOfInterface(Class<?> type) {
        Class<?>[] interfaces = type.getInterfaces();
        if(interfaces == null) {
            return false;
        }

        for(Class<?> interfaceClass : interfaces) {
            CSON a = interfaceClass.getAnnotation(CSON.class);
            if(a != null) {
                return true;
            }
            if(isCSONAnnotated(interfaceClass)) {
                return true;
            }

        }
        return false;
    }


    private static void checkCSONAnnotation(Class<?> type) {
         Annotation a = type.getAnnotation(CSON.class);
         if(a == null) {
             if(isCSONAnnotated(type) || isCSONAnnotatedOfInterface(type)) {
                 return;
             }
             if(type.isAnonymousClass()) {
                throw new CSONSerializerException("Anonymous class " + type.getName() + " is not annotated with @CSON");
             }
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

    ObtainTypeValueInvoker findObtainTypeValueInvoker(String fieldName) {
        return fieldValueObtaiorMap.get(fieldName);
    }



    static class ObtainTypeValueInvoker {


        static List<ObtainTypeValueInvoker> searchObtainTypeValueInvoker(TypeElement typeElement) {
            List<ObtainTypeValueInvoker> result = new ArrayList<>();

                // 이미 등록된 메서드를 덮어쓰지 않도록 하기 위해 HashSet을 사용한다.
            HashSet<String> methodNames = new HashSet<>();
            Set<String> genericTypeNames = typeElement.getGenericTypeNames();
            if(genericTypeNames == null) {
                genericTypeNames = Collections.emptySet();
            }

            Class<?> currentType = typeElement.getType();
            searchObtainTypeValueInvokerInType(genericTypeNames, currentType, result,methodNames);
            Class<?>[] interfaces = currentType.getInterfaces();
            if(interfaces != null) {
                for(Class<?> interfaceClass : interfaces) {
                    searchObtainTypeValueInvokerInType(genericTypeNames, interfaceClass, result,methodNames);
                }
            }


            return result;

        }

        static void searchObtainTypeValueInvokerInType(Set<String> genericTypeNames, Class<?> currentType, List<ObtainTypeValueInvoker> result, HashSet<String> obtainMethodNames) {
            while(currentType != Object.class && currentType != null) {
                Method[] methods = currentType.getDeclaredMethods();
                for(Method method : methods) {
                    // 이미 등록된 메서드를 덮어쓰지 않도록 하기 위해 HashSet을 사용한다.
                    String methodName = makeMethodName(method);
                    if(obtainMethodNames.contains(methodName)) {
                       continue;
                    }
                    ObtainTypeValue obtainTypeValue = method.getAnnotation(ObtainTypeValue.class);
                    List<Method> allMethods = ReflectionUtils.getAllInheritedMethods(currentType);
                    List<Field> allFields =  ReflectionUtils.getAllInheritedFields(currentType);



                    if(obtainTypeValue != null) {
                        String[] fieldNames = obtainTypeValue.fieldNames();
                        String[] setterMethodNames = obtainTypeValue.setterMethodNames();
                        if(fieldNames == null || fieldNames.length == 0 && (setterMethodNames == null || setterMethodNames.length == 0)) {
                            fieldNames = new String[]{SchemaMethod.getterNameFilter(method.getName())};
                        }
                        if(setterMethodNames == null || setterMethodNames.length == 0) {
                            setterMethodNames = new String[0];
                        }
                        for(String fieldName : fieldNames) {
                            fieldName = fieldName.trim();

                            String finalFieldName = fieldName;
                            if(allFields.stream().filter(field -> field.getAnnotation(CSONValue.class) != null).noneMatch(field -> field.getName().equals(finalFieldName))) {
                                throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ". Field '" + fieldName + "' not found in " + currentType.getName());
                            }

                            Class<?> returnType = verifyMethod(genericTypeNames, currentType, method);
                            obtainMethodNames.add(methodName);
                            result.add(new ObtainTypeValueInvoker( method, fieldName,
                                    returnType,
                                    method.getParameterTypes(),
                                    obtainTypeValue.ignoreError(), false));
                        }


                        for(String setterMethodName : setterMethodNames) {
                            setterMethodName = setterMethodName.trim();
                            String finalMethodName = setterMethodName;
                            if(allMethods.stream().filter(methodObj -> methodObj.getAnnotation(CSONValueSetter.class) != null).noneMatch(methodObj -> methodObj.getName().equals(finalMethodName))) {
                                throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ". Method '" + methodName + "' not found in " + currentType.getName());
                            }

                            Class<?> returnType = verifyMethod(genericTypeNames, currentType, method);
                            obtainMethodNames.add(methodName);
                            result.add(new ObtainTypeValueInvoker(method, setterMethodName,
                                    returnType,
                                    method.getParameterTypes(),
                                    obtainTypeValue.ignoreError(), false));
                        }

                    }
                }
                currentType= currentType.getSuperclass();
            }
        }

        private static Class<?> verifyMethod(Set<String> genericTypeNames, Class<?> currentType, Method method) {
            Class<?> returnType = method.getReturnType();
            Type genericReturnType = method.getGenericReturnType();
            boolean genericType = false;
            if(genericReturnType instanceof TypeVariable && genericTypeNames.contains(((TypeVariable<?>) genericReturnType).getName())) {
                genericType = true;
            }
            else if(!Types.isSingleType(Types.of(returnType)) && (returnType == void.class || returnType == Void.class || returnType == null || (!genericType && returnType.getAnnotation(CSON.class) == null))) {
                throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ".  Return type must be a class annotated with @CSON");
            }
            int parameterCount = method.getParameterCount();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if(parameterCount > 0) {
                Class<?> parameterType = parameterTypes[0];
                if(!parameterType.isAssignableFrom(CSONObject.class)) {
                    throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ". Parameter type only can be CSONObject");
                }
            }
            if(parameterCount > 1) {
                Class<?> parameterType = parameterTypes[1];
                if(!parameterType.isAssignableFrom(CSONObject.class)) {
                    throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ". Parameter type only can be CSONObject");
                }
            }
            if(parameterCount > 2) {
                throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ".  Parameter count must be zero or one of CSONElement or CSONObject or CSONArray");
            }
            return returnType;
        }

        private static String makeMethodName(Method method) {
            StringBuilder methodNameBuilder = new StringBuilder();
            methodNameBuilder.append(method.getName()).append("(");
            if(method.getParameterCount() == 0) {
                return methodNameBuilder.append(")").toString();
            }
            for(Class<?> parameterType : method.getParameterTypes()) {
                methodNameBuilder.append(parameterType.getName()).append(",");
            }
            methodNameBuilder.deleteCharAt(methodNameBuilder.length() - 1).append(")");
            return methodNameBuilder.toString();
        }


        private ObtainTypeValueInvoker(Method method,String fieldName,  Class<?> returnType, Class<?>[] parameters, boolean ignoreError, boolean isField) {
            this.method = method;
            this.returnType = returnType;
            this.parameters = parameters;
            this.ignoreError = ignoreError;
            this.fieldName = fieldName;
            this.isField = isField;
        }

        private Method method;
        private Class<?> returnType;
        private Class<?>[] parameters;
        boolean ignoreError = false;
        private String fieldName;
        private boolean isField = false;

        String getFieldName() {
            return fieldName;
        }




        Object obtain(Object parents,CSONObject item, CSONObject all) {
            try {
                if(parameters == null || parameters.length == 0) {
                    return method.invoke(parents);
                } else if(parameters.length == 1) {
                    return method.invoke(parents, item);
                } else if(parameters.length == 2) {
                    return method.invoke(parents, item,all);
                } else {
                    throw new CSONSerializerException("Invalid @ObtainTypeValue method " + method.getName() + " of " + method.getDeclaringClass().getName() + ".  Parameter count must be zero or one of CSONElement or CSONObject or CSONArray");
                }
            } catch (Exception e) {
                if(ignoreError) {
                    return null;
                }
                throw new CSONSerializerException("Failed to invoke @ObtainTypeValue method " + method.getName() + " of " + method.getDeclaringClass().getName(), e);
            }
        }

    }




}
