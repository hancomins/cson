package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONObject;
import com.hancomins.cson.serializer.CSONValue;
import com.hancomins.cson.serializer.CSONValueSetter;
import com.hancomins.cson.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

public class ObtainTypeValueInvoker {

    static List<ObtainTypeValueInvoker> searchObtainTypeValueInvoker(ClassSchema typeSchema) {
        List<ObtainTypeValueInvoker> result = new ArrayList<>();

        // 이미 등록된 메서드를 덮어쓰지 않도록 하기 위해 HashSet을 사용한다.
        HashSet<String> methodNames = new HashSet<>();
        Set<String> genericTypeNames = typeSchema.getGenericTypeNames();
        if(genericTypeNames == null) {
            genericTypeNames = Collections.emptySet();
        }

        Class<?> currentType = typeSchema.getType();
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
                            throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ".field with annotated @CSONValue '" + fieldName + "' not found in " + currentType.getName());
                        }

                        Class<?> returnType = verifyMethod(genericTypeNames, currentType, method);
                        obtainMethodNames.add(methodName);
                        result.add(new ObtainTypeValueInvoker( method, fieldName,
                                returnType,
                                method.getParameterTypes(),
                                obtainTypeValue.ignoreError(),obtainTypeValue.deserializeAfter(), false));
                    }




                    for(String setterMethodName : setterMethodNames) {
                        setterMethodName = setterMethodName.trim();
                        String finalMethodName = setterMethodName;
                        if(allMethods.stream().filter(methodObj -> methodObj.getAnnotation(
                                CSONValueSetter.class) != null).noneMatch(methodObj -> methodObj.getName().equals(finalMethodName))) {
                            throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ". Method '" + methodName + "' not found in " + currentType.getName());
                        }

                        Class<?> returnType = verifyMethod(genericTypeNames, currentType, method);
                        obtainMethodNames.add(methodName);
                        result.add(new ObtainTypeValueInvoker(method, setterMethodName,
                                returnType,
                                method.getParameterTypes(),
                                obtainTypeValue.ignoreError(),obtainTypeValue.deserializeAfter(), false));
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
        /*else if(!Types.isSingleType(Types.of(returnType)) && (returnType == void.class || returnType == Void.class || returnType == null || (!genericType && returnType.getAnnotation(CSON.class) == null))) {
            throw new CSONSerializerException("Invalid @ObtainTypeValue method of " + currentType.getName() + "." + method.getName() + ".  Return type must be a class annotated with @CSON");
        }*/
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


    private ObtainTypeValueInvoker(Method method,String fieldName,  Class<?> returnType, Class<?>[] parameters, boolean ignoreError,boolean deserializeAfter, boolean isField) {
        this.method = method;
        this.returnType = returnType;
        this.parameters = parameters;
        this.ignoreError = ignoreError;
        this.fieldName = fieldName;
        this.isField = isField;
        this.deserializeAfter = deserializeAfter;
    }

    private Method method;
    private Class<?> returnType;
    private Class<?>[] parameters;
    private boolean ignoreError = false;
    private String fieldName;
    private boolean deserializeAfter = true;
    private boolean isField = false;

    String getFieldName() {
        return fieldName;
    }

    boolean isDeserializeAfter() {
        return deserializeAfter;
    }


    boolean isIgnoreError() {
        return ignoreError;
    }




    Object obtain(Object parents, CSONObject item, CSONObject all) {
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
