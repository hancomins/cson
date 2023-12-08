package com.snoworca.cson.serializer;


import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

class TypeElement {

    private final Class<?> type;
    private final Constructor<?> constructor;


    private SchemaObjectNode schema;

    private final String comment;
    private final String commentAfter;


    protected SchemaObjectNode getSchema() {
        if(schema == null) {
            schema = NodePath.makeSchema(this,null);
        }
        return schema;
    }

    protected static TypeElement create(Class<?> type) {
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
        String commentBefore = cson.comment();
        String commentAfter = cson.commentAfter();
        this.comment = commentBefore.isEmpty() ? null : commentBefore;
        this.commentAfter = commentAfter.isEmpty() ? null : commentAfter;

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





}
