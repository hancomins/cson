package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.CSONArray;
import com.hancomins.cson.CSONObject;
import com.hancomins.cson.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class TypeSchema {


    protected static final TypeSchema CSON_OBJECT;

    static {
        try {
            CSON_OBJECT = new TypeSchema(CSONObject.class, CSONObject.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected static final TypeSchema CSON_ARRAY;

    static {
        try {
            CSON_ARRAY = new TypeSchema(CSONArray.class, CSONArray.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final boolean explicit;
    private final Class<?> type;
    private final Constructor<?> constructor;
    private final ConcurrentHashMap<String, ObtainTypeValueInvoker> fieldValueObtainMap = new ConcurrentHashMap<>();

    private SchemaObjectNode schema;

    private final String comment;
    private final String commentAfter;
    private final Set<String> genericTypeNames = new HashSet<>();


    protected SchemaObjectNode getSchemaObjectNode() {
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



    protected synchronized static TypeSchema create(Class<?> type) {
        type = findNoAnonymousClass(type);

        if(CSONObject.class.isAssignableFrom(type)) {
            return CSON_OBJECT;
        }
        if(CSONArray.class.isAssignableFrom(type)) {
            return CSON_ARRAY;
        }
        //checkCSONAnnotation(type);
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
        return new TypeSchema(type, constructor);
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

    private TypeSchema(Class<?> type, Constructor<?> constructor) {
        this.type = type;
        this.constructor = constructor;
        CSON cson = type.getAnnotation(CSON.class);

        if(cson != null) {
            explicit = cson.explicit();
            String commentBefore = cson.comment();
            String commentAfter = cson.commentAfter();

            this.comment = commentBefore.isEmpty() ? null : commentBefore;
            this.commentAfter = commentAfter.isEmpty() ? null : commentAfter;
        } else {
            explicit = false;
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
            fieldValueObtainMap.putIfAbsent(obtainTypeValueInvoker.getFieldName(), obtainTypeValueInvoker);
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
        return fieldValueObtainMap.get(fieldName);
    }


    boolean isExplicit() {
        return explicit;
    }



}
