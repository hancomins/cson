package com.clipsoft.cson.serializer;

import com.clipsoft.cson.CSONArray;
import com.clipsoft.cson.CSONObject;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SerializeInCSONElementTest {
    @CSON
    public static class CSONClass {
        @CSONValue
        String name;
        @CSONValue
        String value;

        @CSONValue("bundle.value")
        String data;

    }

    @Test
    public void testSerializeInCSONElement() throws Exception {
        CSONClass csonClass = new CSONClass();
        csonClass.name = "name";
        csonClass.value = "value";
        long millis = System.currentTimeMillis();
        csonClass.data = millis + "";
        CSONObject csonObject = new CSONObject();
        csonObject.put("csonClass", csonClass);

        System.out.println(csonObject.toString());

        assertEquals( "name", csonObject.getObject("csonClass").getString("name"));
        assertEquals( "value", csonObject.getObject("csonClass").getString("value"));
        assertEquals( millis + "", csonObject.getObject("csonClass").getObject("bundle").getString("value"));

        CSONClass newObject = csonObject.getObject("csonClass", CSONClass.class);
        assertEquals( "name", newObject.name);
        assertEquals( "value", newObject.value);
        assertEquals( millis + "", newObject.data);


        CSONArray csonArray = new CSONArray();
        csonArray.add(csonClass);

        System.out.println(csonArray.toString());

        assertEquals( "name", csonArray.getObject(0).getString("name"));
        assertEquals( "value", csonArray.getObject(0).getString("value"));
        assertEquals( millis + "", csonArray.getObject(0).getObject("bundle").getString("value"));


        ArrayList<CSONClass> newArrayList = new ArrayList<>();
        newArrayList.add(csonClass);

        csonObject.put("csonClassList", newArrayList);

        System.out.println(csonObject.toString());

        assertEquals( "name", csonObject.getArray("csonClassList").getObject(0).getString("name"));
        assertEquals( "value", csonObject.getArray("csonClassList").getObject(0).getString("value"));
        assertEquals( millis + "", csonObject.getArray("csonClassList").getObject(0).getObject("bundle").getString("value"));




    }
}
