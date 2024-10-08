package com.hancomins.cson;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryCSONTest {

    @Test
    public void testNumberTypes() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("int", 1);
        csonObject.put("float", 1.1f);
        csonObject.put("double", 2.2);
        csonObject.put("long", 333333L);
        csonObject.put("short", (short)32000);
        csonObject.put("byte", (byte)128);
        csonObject.put("char", 'c');
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);
        assertEquals(csonObject.get("int"), readObject.get("int"));
        assertInstanceOf(Integer.class, readObject.get("int"));
        assertEquals(csonObject.get("float"), readObject.get("float"));
        assertInstanceOf(Float.class, readObject.get("float"));
        assertEquals(csonObject.get("double"), readObject.get("double"));
        assertInstanceOf(Double.class, readObject.get("double"));
        assertEquals(csonObject.get("long"), readObject.get("long"));
        assertInstanceOf(Long.class, readObject.get("long"));
        assertEquals(csonObject.get("short"), readObject.get("short"));
        assertInstanceOf(Short.class, readObject.get("short"));
        assertEquals(csonObject.get("byte"), readObject.get("byte"));
        assertInstanceOf(Byte.class, readObject.get("byte"));
        assertEquals(csonObject.get("char"), readObject.get("char"));
        assertInstanceOf(Character.class, readObject.get("char"));
    }


    @Test
    public void testStringTypes() {
        CSONObject csonObject = new CSONObject();
        // 0bytes string
        csonObject.put("1", "");
        // 15bytes string
        csonObject.put("2", "short string");
        // 254bytes string
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 254; i++) {
            sb.append("a");
        }
        csonObject.put("3", sb.toString());
        sb.setLength(0);
        // 65533 bytes string
        for(int i = 0; i < 65533; i++) {
            sb.append("a");
        }
        csonObject.put("4", sb.toString());
        sb.setLength(0);
        // 65535 bytes string
        for(int i = 0; i < 65535; i++) {
            sb.append("a");
        }
        csonObject.put("5", sb.toString());
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        assertEquals(csonObject, readObject);
        assertEquals(csonObject.get("1"), readObject.get("1"));
        assertInstanceOf(String.class, readObject.get("1"));
        assertEquals(csonObject.get("2"), readObject.get("2"));
        assertInstanceOf(String.class, readObject.get("2"));
        assertEquals(csonObject.get("3"), readObject.get("3"));
        assertInstanceOf(String.class, readObject.get("3"));
        assertEquals(csonObject.get("4"), readObject.get("4"));
        assertInstanceOf(String.class, readObject.get("4"));
        assertEquals(csonObject.get("5"), readObject.get("5"));
        assertInstanceOf(String.class, readObject.get("5"));
    }

    @Test
    public void testByteArray() {
        CSONObject csonObject = new CSONObject();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 0bytes byte array
        csonObject.put("1", baos.toByteArray());
        // 254bytes byte array
        for(int i = 0; i < 254; i++) {
            baos.write(1);
        }
        csonObject.put("2", baos.toByteArray());
        baos.reset();
        // 65533 bytes byte array
        for(int i = 0; i < 65533; i++) {
            baos.write(1);
        }
        csonObject.put("3", baos.toByteArray());
        baos.reset();
        // 165535 bytes byte array
        for(int i = 0; i < 165535; i++) {
            baos.write(1);
        }
        csonObject.put("4", baos.toByteArray());
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        assertEquals(csonObject, readObject);
        assertArrayEquals((byte[])csonObject.get("1"), (byte[])readObject.get("1"));
        assertArrayEquals((byte[])csonObject.get("2"), (byte[])readObject.get("2"));
        assertArrayEquals((byte[])csonObject.get("3"), (byte[])readObject.get("3"));
        assertArrayEquals((byte[])csonObject.get("4"), (byte[])readObject.get("4"));
        System.out.println(readObject.getString("2"));


    }


    @Test
    @DisplayName("CSONOBject")
    public void testSimpleKeyValue() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("key", "value");
        csonObject.put("key2", 123L);
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);
    }

    @Test
    public void testObjectInEmptyObject() {
        CSONObject csonObject = new CSONObject();
        CSONObject innerObject = new CSONObject();
        csonObject.put("inner", innerObject);
        CSONObject originCsonObject = csonObject.clone();
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(originCsonObject, readObject);
        assertEquals(csonObject, readObject);
    }

    @Test
    public void testObjectInObject() {
        CSONObject csonObject = new CSONObject();
        CSONObject innerObject = new CSONObject();
        innerObject.put("key", "value");
        csonObject.put("inner", innerObject);
        innerObject.put("key2", 123L);
        innerObject.put("key3", new CSONObject());
        innerObject.put("key4", new CSONObject().put("key", "value").put("key2", 123L));
        csonObject.put("key2", 123L);

        CSONObject originCsonObject = csonObject.clone();
        System.out.println(csonObject);
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(originCsonObject, readObject);
        assertEquals(csonObject, readObject);
    }

    @Test
    public void testArrayInEmptyArray() {
        CSONArray csonArray = new CSONArray();
        CSONArray innerArray = new CSONArray();
        csonArray.add(innerArray);
        CSONArray originCsonArray = csonArray.clone();
        byte[] csonBytes = csonArray.toBytes();
        CSONArray readArray = new CSONArray(csonBytes);
        System.out.println(csonArray);
        assertEquals(originCsonArray, readArray);
        assertEquals(csonArray, readArray);


    }



}
