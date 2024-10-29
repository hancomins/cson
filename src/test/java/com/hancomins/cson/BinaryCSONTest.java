package com.hancomins.cson;


import com.hancomins.cson.options.JSON5WriterOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.PrimitiveIterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryCSONTest {

    @Test
    public void testNumberTypes() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("int", 1);
        csonObject.put("float", 1.1f);
        csonObject.put("double", 2.2);
        csonObject.put("long", Long.MAX_VALUE);
        csonObject.put("short", Short.MAX_VALUE);
        csonObject.put("byte", (byte)128);
        csonObject.put("char", 'c');
        byte[] csonBytes = csonObject.toBytes();
        CSONObject readObject = new CSONObject(csonBytes);
        System.out.println(csonObject);
        assertEquals(csonObject, readObject);
        assertEquals(csonObject.getInt("int"), readObject.getInt("int"));
        //assertInstanceOf(Integer.class, readObject.get("int"));
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



    @Test
    public void withComment() {
        CSONObject csonObject = new CSONObject();
        csonObject.setHeaderComment("header comment");
        csonObject.setFooterComment("footer comment");

        csonObject.put("key", "value");

        csonObject.setCommentForKey("key", " comment\n for key");
        csonObject.setCommentAfterKey("key", " comment after key ");
        csonObject.setCommentForValue("key", " comment for value ");
        csonObject.setCommentAfterValue("key", " comment after value ");


        csonObject.put("emptyObject", new CSONObject());
        csonObject.setCommentForKey("emptyObject", " for emptyObject");
        csonObject.setCommentAfterKey("emptyObject", " after emptyObject ");
        csonObject.setCommentForValue("emptyObject", " comment for emptyObject value ");
        csonObject.setCommentAfterValue("emptyObject", " comment after emptyObject value ");

        csonObject.put("emptyArray", new CSONArray());
        csonObject.setCommentForKey("emptyArray", " for emptyArray");
        csonObject.setCommentAfterKey("emptyArray", " after emptyArray ");
        csonObject.setCommentForValue("emptyArray", " comment for emptyArray value ");
        csonObject.setCommentAfterValue("emptyArray", " comment after emptyArray value ");


        CSONArray valueArray = new CSONArray().put("value1").put("value2").put("value3");
        csonObject.put("array", valueArray);
        csonObject.setCommentForKey("array", " for array");
        csonObject.setCommentAfterKey("array", " after array ");
        csonObject.setCommentForValue("array", " comment for array value ");
        csonObject.setCommentAfterValue("array", " comment after array value ");
        valueArray.setCommentForValue(0, " comment for array value 0 ");
        valueArray.setCommentForValue(1, " comment for array value 1 ");
        valueArray.setCommentForValue(2, " comment for array value 2 ");
        valueArray.setCommentAfterValue(0, " comment after array value 0 ");
        valueArray.setCommentAfterValue(1, " comment after array value 1 ");
        valueArray.setCommentAfterValue(2, " comment after array value 2 ");




        CSONObject valueObject = new CSONObject().put("key1", "value1").put("key2", "value2").put("key3", "value3");
        csonObject.put("object", valueObject);
        csonObject.setCommentForKey("object", " for object");
        csonObject.setCommentAfterKey("object", " after object ");
        csonObject.setCommentForValue("object", " comment for object value ");
        csonObject.setCommentAfterValue("object", " comment after object value ");
        valueObject.setCommentForKey("key1", " for key1");
        valueObject.setCommentAfterKey("key1", " after key1 ");
        valueObject.setCommentForValue("key1", " comment for key1 value ");
        valueObject.setCommentAfterValue("key1", " comment after key1 value ");
        valueObject.setCommentForKey("key2", " for key2");
        valueObject.setCommentAfterKey("key2", " after key2 ");
        valueObject.setCommentForValue("key2", " comment for key2 value ");
        valueObject.setCommentAfterValue("key2", " comment after key2 value ");
        valueObject.setCommentForKey("key3", " for key3");
        valueObject.setCommentAfterKey("key3", " after key3 ");
        valueObject.setCommentForValue("key3", " comment for key3 value ");
        valueObject.setCommentAfterValue("key3", " comment after key3 value ");


        //System.out.println(csonObject.toString(JSON5WriterOption.json()));
        //System.out.println(csonObject.toString(JSON5WriterOption.prettyJson()));
        System.out.println(csonObject.toString(JSON5WriterOption.json5()));


        CSONObject parseredCSONObject = new CSONObject(csonObject.toBytes());
        assertEquals("value", parseredCSONObject.get("key"));
        assertEquals(" comment\n for key", parseredCSONObject.getCommentForKey("key"));
        assertEquals(" comment after key ", parseredCSONObject.getCommentAfterKey("key"));
        assertEquals(" comment for value ", parseredCSONObject.getCommentForValue("key"));
        assertEquals(" comment after value ", parseredCSONObject.getCommentAfterValue("key"));

        // Verify comments for emptyObject
        assertNotNull(parseredCSONObject.get("emptyObject"));
        assertEquals(" for emptyObject", parseredCSONObject.getCommentForKey("emptyObject"));
        assertEquals(" after emptyObject ", parseredCSONObject.getCommentAfterKey("emptyObject"));
        assertEquals(" comment for emptyObject value ", parseredCSONObject.getCommentForValue("emptyObject"));
        assertEquals(" comment after emptyObject value ", parseredCSONObject.getCommentAfterValue("emptyObject"));

        // Verify comments for emptyArray
        assertNotNull(parseredCSONObject.get("emptyArray"));
        assertEquals(" for emptyArray", parseredCSONObject.getCommentForKey("emptyArray"));
        assertEquals(" after emptyArray ", parseredCSONObject.getCommentAfterKey("emptyArray"));
        assertEquals(" comment for emptyArray value ", parseredCSONObject.getCommentForValue("emptyArray"));
        assertEquals(" comment after emptyArray value ", parseredCSONObject.getCommentAfterValue("emptyArray"));

        // Verify comments for array values
        CSONArray parsedArray = parseredCSONObject.getCSONArray("array");
        assertEquals(" comment for array value 0 ", parsedArray.getCommentForValue(0));
        assertEquals(" comment after array value 0 ", parsedArray.getCommentAfterValue(0));
        assertEquals(" comment for array value 1 ", parsedArray.getCommentForValue(1));
        assertEquals(" comment after array value 1 ", parsedArray.getCommentAfterValue(1));
        assertEquals(" comment for array value 2 ", parsedArray.getCommentForValue(2));
        assertEquals(" comment after array value 2 ", parsedArray.getCommentAfterValue(2));

        // Verify comments for object values
        CSONObject parsedObject = parseredCSONObject.getCSONObject("object");
        assertEquals(" for key1", parsedObject.getCommentForKey("key1"));
        assertEquals(" after key1 ", parsedObject.getCommentAfterKey("key1"));
        assertEquals(" comment for key1 value ", parsedObject.getCommentForValue("key1"));
        assertEquals(" comment after key1 value ", parsedObject.getCommentAfterValue("key1"));
        assertEquals(" for key2", parsedObject.getCommentForKey("key2"));
        assertEquals(" after key2 ", parsedObject.getCommentAfterKey("key2"));
        assertEquals(" comment for key2 value ", parsedObject.getCommentForValue("key2"));
        assertEquals(" comment after key2 value ", parsedObject.getCommentAfterValue("key2"));
        assertEquals(" for key3", parsedObject.getCommentForKey("key3"));
        assertEquals(" after key3 ", parsedObject.getCommentAfterKey("key3"));
        assertEquals(" comment for key3 value ", parsedObject.getCommentForValue("key3"));
        assertEquals(" comment after key3 value ", parsedObject.getCommentAfterValue("key3"));


        System.out.println(csonObject.toString(JSON5WriterOption.prettyJson5()));
        parseredCSONObject = new CSONObject(csonObject.toBytes());
        assertEquals("value", parseredCSONObject.get("key"));
        assertEquals(" comment\n for key", parseredCSONObject.getCommentForKey("key"));
        assertEquals(" comment after key ", parseredCSONObject.getCommentAfterKey("key"));
        assertEquals(" comment for value ", parseredCSONObject.getCommentForValue("key"));
        assertEquals(" comment after value ", parseredCSONObject.getCommentAfterValue("key"));

        // Verify comments for emptyObject
        assertNotNull(parseredCSONObject.get("emptyObject"));
        assertEquals(" for emptyObject", parseredCSONObject.getCommentForKey("emptyObject"));
        assertEquals(" after emptyObject ", parseredCSONObject.getCommentAfterKey("emptyObject"));
        assertEquals(" comment for emptyObject value ", parseredCSONObject.getCommentForValue("emptyObject"));
        assertEquals(" comment after emptyObject value ", parseredCSONObject.getCommentAfterValue("emptyObject"));

        // Verify comments for emptyArray
        assertNotNull(parseredCSONObject.get("emptyArray"));
        assertEquals(" for emptyArray", parseredCSONObject.getCommentForKey("emptyArray"));
        assertEquals(" after emptyArray ", parseredCSONObject.getCommentAfterKey("emptyArray"));
        assertEquals(" comment for emptyArray value ", parseredCSONObject.getCommentForValue("emptyArray"));
        assertEquals(" comment after emptyArray value ", parseredCSONObject.getCommentAfterValue("emptyArray"));

        // Verify comments for array values
        parsedArray = parseredCSONObject.getCSONArray("array");
        assertEquals(" comment for array value 0 ", parsedArray.getCommentForValue(0));
        assertEquals(" comment after array value 0 ", parsedArray.getCommentAfterValue(0));
        assertEquals(" comment for array value 1 ", parsedArray.getCommentForValue(1));
        assertEquals(" comment after array value 1 ", parsedArray.getCommentAfterValue(1));
        assertEquals(" comment for array value 2 ", parsedArray.getCommentForValue(2));
        assertEquals(" comment after array value 2 ", parsedArray.getCommentAfterValue(2));

        // Verify comments for object values
        parsedObject = parseredCSONObject.getCSONObject("object");
        assertEquals(" for key1", parsedObject.getCommentForKey("key1"));
        assertEquals(" after key1 ", parsedObject.getCommentAfterKey("key1"));
        assertEquals(" comment for key1 value ", parsedObject.getCommentForValue("key1"));
        assertEquals(" comment after key1 value ", parsedObject.getCommentAfterValue("key1"));
        assertEquals(" for key2", parsedObject.getCommentForKey("key2"));
        assertEquals(" after key2 ", parsedObject.getCommentAfterKey("key2"));
        assertEquals(" comment for key2 value ", parsedObject.getCommentForValue("key2"));
        assertEquals(" comment after key2 value ", parsedObject.getCommentAfterValue("key2"));
        assertEquals(" for key3", parsedObject.getCommentForKey("key3"));
        assertEquals(" after key3 ", parsedObject.getCommentAfterKey("key3"));
        assertEquals(" comment for key3 value ", parsedObject.getCommentForValue("key3"));
        assertEquals(" comment after key3 value ", parsedObject.getCommentAfterValue("key3"));


        assertEquals("header comment", parseredCSONObject.getHeaderComment());
        assertEquals("footer comment", parseredCSONObject.getFooterComment());



    }

    @Test
    public void sizeCompare() {
        if(1 < 2) return;
        InputStream inputStream = PerformanceTest.class.getClassLoader().getResourceAsStream("large-file.json");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json = byteArrayOutputStream.toString();
        CSONObject csonObject = new CSONObject(json);
        byte[] csonBytes = csonObject.toBytes();
        System.out.println("json: " + json.getBytes().length);
        System.out.println("cson: " + csonBytes.length);
        System.out.println( 100 - ((float)csonBytes.length / json.getBytes().length) * 100 + "%");

        long start = System.currentTimeMillis();
        for(int i = 0 ; i < 10; i++) {
            CSONObject csonObject1 =  new CSONObject(json);
            csonObject1.toString();
        }
        System.out.println("json: " + (System.currentTimeMillis() - start) + "ms");


        start = System.currentTimeMillis();
        for(int i = 0 ; i < 10; i++) {
            CSONObject csonObject1 =  new CSONObject(csonBytes);
            csonObject1.toBytes();
        }
        System.out.println("cson: " + (System.currentTimeMillis() - start) + "ms");




    }


}
