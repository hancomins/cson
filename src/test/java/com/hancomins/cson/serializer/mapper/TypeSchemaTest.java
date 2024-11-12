package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.serializer.CSONValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeSchemaTest {

    public static class TestA {
        public int a;
        @CSONValue("a")
        public String b;
    }

    @Test
    void test() {
        TypeSchema schema = TypeSchema.create(TestA.class);
        SchemaObjectNode rootNode =  schema.getSchemaObjectNode();
        assertNotNull(rootNode);

    }


}