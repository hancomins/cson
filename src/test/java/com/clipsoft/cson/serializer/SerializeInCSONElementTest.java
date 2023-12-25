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

        CSONObject csonObject2 = CSONObject.fromObject(csonClass);
        System.out.println(csonObject2.toString());

        System.out.println(csonObject.toString());

        assertEquals( "name", csonObject.getCSONObject("csonClass").getString("name"));
        assertEquals( "value", csonObject.getCSONObject("csonClass").getString("value"));
        assertEquals( millis + "", csonObject.getCSONObject("csonClass").getCSONObject("bundle").getString("value"));

        CSONClass newObject = csonObject.getObject("csonClass", CSONClass.class);

        assertEquals( "name", newObject.name);
        assertEquals( "value", newObject.value);
        assertEquals( millis + "", newObject.data);


        CSONArray csonArray = new CSONArray();
        csonArray.add(csonClass);

        System.out.println(csonArray.toString());

        assertEquals( "name", csonArray.getCSONObject(0).getString("name"));
        assertEquals( "value", csonArray.getCSONObject(0).getString("value"));
        assertEquals( millis + "", csonArray.getCSONObject(0).getCSONObject("bundle").getString("value"));


        ArrayList<CSONClass> newArrayList = new ArrayList<>();
        newArrayList.add(csonClass);
        newArrayList.add(csonClass);

        csonObject.put("csonClassList", newArrayList);

        System.out.println(csonObject.toString());

        assertEquals( "name", csonObject.getCSONArray("csonClassList").getCSONObject(0).getString("name"));
        assertEquals( "value", csonObject.getCSONArray("csonClassList").getCSONObject(0).getString("value"));
        assertEquals( millis + "", csonObject.getCSONArray("csonClassList").getCSONObject(0).getCSONObject("bundle").getString("value"));




    }
}
