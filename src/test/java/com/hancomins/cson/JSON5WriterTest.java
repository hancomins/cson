package com.hancomins.cson;

import com.hancomins.cson.options.JSON5WriterOption;
import com.hancomins.cson.serializer.CSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSON5WriterTest {

    @Test
    public void simpleWrite() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("key", "value");
        csonObject.put("emptyObject", new CSONObject());
        csonObject.put("emptyArray", new CSONArray());
        csonObject.put("array", new CSONArray().put("value1").put("value2").put("value3"));
        csonObject.put("object", new CSONObject().put("key1", "value1").put("key2", "value2").put("key3", "value3"));

        System.out.println(csonObject.toString(JSON5WriterOption.json()));
        System.out.println(csonObject.toString(JSON5WriterOption.prettyJson()));
        System.out.println(csonObject.toString(JSON5WriterOption.prettyJson().setUnprettyArray(true)));
        System.out.println(csonObject.toString(JSON5WriterOption.json5()));
    }

    @Test
    public void withComment() {
        CSONObject csonObject = new CSONObject();
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


        CSONObject parseredCSONObject = new CSONObject(csonObject.toString(JSON5WriterOption.json5()));
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
        parseredCSONObject = new CSONObject(csonObject.toString(JSON5WriterOption.prettyJson5()));
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

    }
}
