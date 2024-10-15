package com.hancomins.cson;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;

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
        //innerObject.put("key4", new CSONObject().put("key", "value").put("key2", 123L));
        csonObject.put("key2", 123L);


        CSONObject originCsonObject = csonObject.clone();
        System.out.println("       cson: " + csonObject);
        System.out.println("origin cson: " + originCsonObject);
        byte[] csonBytes = csonObject.toBytes();
        System.out.println("csonBytes: " + csonBytes.length);
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(readObject);
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

    @Test
    public void testObjectInArray() {
        CSONObject csonObject = new CSONObject();
        CSONArray csonArray = new CSONArray();
        CSONArray innerArray = new CSONArray();
        innerArray.add("value");
        csonArray.add(innerArray);
        innerArray.add(123L);
        innerArray.add("sfdsf");
        innerArray.add(new CSONObject());
        csonObject.put("array", csonArray);
        CSONObject originCson = csonObject.clone();


        byte[] csonBytes = csonObject.toBytes();
        CSONObject readArray = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(originCson, readArray);
        assertEquals(csonObject, readArray);

    }


    private CSONObject makeCSOObject() {
        Random random = new Random(System.currentTimeMillis());
        byte[] randomBuffer = new byte[random.nextInt(64) +2];
        random.nextBytes(randomBuffer);
        CSONObject csonObject = new CSONObject();
        csonObject.put("1", 1);
        csonObject.put("1.1", 1.1f);
        csonObject.put("2.2", 2.2);
        csonObject.put("333333L", 333333L);
        csonObject.put("boolean", true);
        csonObject.put("char", 'c');
        csonObject.put("short", (short)32000);
        csonObject.put("byte", (byte)128);
        csonObject.put("null", null);
        csonObject.put("string", "stri \" \n\rng");
        csonObject.put("this", csonObject);
        csonObject.put("byte[]", randomBuffer);
        CSONArray csonArray = new CSONArray();
        csonArray.add(1);
        csonArray.add(1.1f);
        csonArray.add((double)2.2);
        csonArray.put(333333L);
        csonArray.put(true);
        csonArray.put('c');
        csonArray.add((short)32000);
        csonArray.add((byte)128);
        csonArray.add(null);
        csonArray.add("stri \" \n\rng");
        csonArray.add(csonArray);
        csonArray.add(csonObject.clone());
        csonArray.add(randomBuffer);
        csonObject.put("array", csonArray);
        csonObject.put("array2", new CSONArray().put(new CSONArray().put(1).put(2)).put(new CSONArray().put(3).put(4)).put(new CSONArray()).put(new CSONObject()));
        csonObject.put("array3", new CSONArray().put("").put(new CSONArray().put(3).put(4)).put(new CSONArray()).put(new CSONObject()));
        csonObject.put("array4", new CSONArray().put(new CSONObject()).put(new CSONObject()).put(new CSONArray()).put(new CSONObject().put("inArray",new CSONArray())));
        csonObject.put("key111", new CSONObject().put("1", new CSONObject()));
        csonObject.put("key112", new CSONArray().put(new CSONObject()));



        return csonObject;
    }


    @Test
    public void cloneAndEqualsTest() throws  Exception {
        CSONObject csonObject = makeCSOObject();
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);
    }



}
