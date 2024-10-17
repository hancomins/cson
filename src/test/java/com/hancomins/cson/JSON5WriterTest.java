package com.hancomins.cson;

import com.hancomins.cson.options.JsonWritingOptions;
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

        System.out.println(csonObject.toString(JsonWritingOptions.json()));
        System.out.println(csonObject.toString(JsonWritingOptions.prettyJson()));
        System.out.println(csonObject.toString(JsonWritingOptions.prettyJson().setUnprettyArray(true)));
        System.out.println(csonObject.toString(JsonWritingOptions.json5()));
    }
}
