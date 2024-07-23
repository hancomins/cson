package com.hancomins.cson;

import org.junit.Test;

public class JSONWriterTest {

    public static void main(String[] args) {
        CSONObject csonObject = new CSONObject();
        CSONArray csonArray = new CSONArray();
        csonArray.add("a");
        csonArray.add("b");
        csonArray.add("c");
        csonObject.put("array", csonArray);

        JSONWriter jsonWriter = new JSONWriter((JSONOptions) StringFormatOption.json5());
        JSONWriter.writeJSONElement(csonObject, jsonWriter);
    }

    @Test
    public void testWrite() {



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

    }


}