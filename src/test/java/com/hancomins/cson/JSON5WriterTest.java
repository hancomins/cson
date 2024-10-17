package com.hancomins.cson;

import com.hancomins.cson.options.JSON5WriterOption;
import org.junit.jupiter.api.Test;

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
        csonObject.put("array", new CSONArray().put("value1").put("value2").put("value3"));
        csonObject.put("object", new CSONObject().put("key1", "value1").put("key2", "value2").put("key3", "value3"));

        //System.out.println(csonObject.toString(JSON5WriterOption.json()));
        //System.out.println(csonObject.toString(JSON5WriterOption.prettyJson()));
        System.out.println(csonObject.toString(JSON5WriterOption.json5()));
        System.out.println(csonObject.toString(JSON5WriterOption.prettyJson5()));
    }
}
