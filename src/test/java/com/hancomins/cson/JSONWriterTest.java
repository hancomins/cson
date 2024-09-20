package com.hancomins.cson;

import com.hancomins.cson.options.StringFormatOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterTest {

    @Test
    public void testWriteSimple() {

        CSONObject csonObject = new CSONObject();
        CSONArray csonArray = new CSONArray();
        csonArray.add("a");
        csonArray.add("b");
        csonArray.add("c");
        csonArray.add(new CSONObject().put("ok", "123").put("array", new CSONArray().put("a").put("11")));
        csonObject.put("array", csonArray);
        //csonObject.setCommentAfterValue("array", "comment after array");

        csonObject.put("emptyArray", new CSONArray());
        csonObject.put("emptyObject", new CSONObject());
        JSONWriter jsonWriter = new JSONWriter((JSONOptions) StringFormatOption.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(csonObject, jsonWriter);

        System.out.println(jsonWriter.toString());

        CSONObject csonObject2 = new CSONObject(jsonWriter.toString(), StringFormatOption.json5().setPretty(true).setUnprettyArray(false));
        System.out.println(csonObject2.toString());

        assertEquals(csonObject, csonObject2);
        assertEquals(csonObject.toString(StringFormatOption.json5().setPretty(true).setUnprettyArray(false)), csonObject2.toString());
    }

    @Test
    public void testWriteCommentInCSONArray() {


        CSONArray csonArray = new CSONArray();
        csonArray.put(0);
        csonArray.put(new CSONObject());
        csonArray.put(1);
        csonArray.put(new CSONArray());
        csonArray.setCommentForValue(0, "comment before a value at index 0");
        csonArray.setCommentAfterValue(0, "comment after a value at index 0");
        csonArray.setCommentForValue(1, "comment before a value at index 1");
        csonArray.setCommentAfterValue(1, "comment after a value at index 1");
        csonArray.setCommentForValue(3, "comment before a value at index 3");
        csonArray.setCommentAfterValue(3, "comment after a value at index 3");

        JSONWriter jsonWriter = new JSONWriter((JSONOptions) StringFormatOption.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(csonArray, jsonWriter);
        System.out.println(jsonWriter.toString());

    }

    @Test
    public void testWriteComment() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("a", "b");
        csonObject.setCommentForKey("a", "comment for a");
        csonObject.setCommentAfterKey("a", "comment after a");
        csonObject.setCommentForValue("a", "comment before a");
        csonObject.setCommentAfterValue("a", "comment after a");

        JSONWriter jsonWriter = new JSONWriter((JSONOptions) StringFormatOption.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(csonObject, jsonWriter);
        System.out.println(jsonWriter.toString());



        csonObject = new CSONObject();
        csonObject.put("a", new CSONObject());
        csonObject.setCommentForKey("a", "comment for a key");
        csonObject.setCommentAfterKey("a", "comment after a key");
        csonObject.setCommentForValue("a", "comment before a value");
        csonObject.setCommentAfterValue("a", "comment after a value");

        jsonWriter = new JSONWriter((JSONOptions) StringFormatOption.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(csonObject, jsonWriter);
        System.out.println(jsonWriter.toString());



    }


}