package com.hancomins.cson.serializer;

import com.hancomins.cson.CSONArray;
import com.hancomins.cson.CSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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


    @Test
    public void testGetList() {
        CSONObject csonObject = new CSONObject();
        CSONArray csonArray = new CSONArray();
        for(int i = 0; i < 10; i++) {
            csonArray.put((i % 2 == 0 ? "+" : "-") + i + "");
        }
        csonObject.put("list", csonArray);
        List<Float> list=  csonObject.getList("list", Float.class);
        for(int i = 0; i < 10; i++) {
            assertEquals( (i % 2 == 0 ? "" : "-") + i + ".0", list.get(i).toString());
        }
    }

    @Test
    public void testGetBooleanList() {
        CSONObject csonObject = new CSONObject();
        CSONArray csonArray = new CSONArray();
        for(int i = 0; i < 10; i++) {
            csonArray.put((i % 2 == 0));
        }
        csonObject.put("list", csonArray);
        List<Boolean> list=  csonObject.getList("list", Boolean.class);
        for(int i = 0; i < 10; i++) {
            assertEquals( (i % 2 == 0), list.get(i).booleanValue());
        }
    }

    @Test
    public void testGetObjectOfCSONClass() {
        CSONObject csonObject = new CSONObject();
        CSONArray csonArray = new CSONArray();
        for(int i = 0; i < 10; i++) {
            CSONClass csonClass = new CSONClass();
            csonClass.name = "name" + i;
            csonClass.value = "value" + i;
            csonClass.data = i + "";
            csonArray.put(csonClass);
        }
        csonObject.put("list", csonArray);
        System.out.println(csonObject.toString());
        List<CSONClass> list=  csonObject.getList("list", CSONClass.class);
        for(int i = 0; i < 10; i++) {
            assertEquals( "name" + i, list.get(i).name);
            assertEquals( "value" + i, list.get(i).value);
            assertEquals( i + "", list.get(i).data);
        }


        List<String> stringList=  csonObject.getList("list", String.class);
        for(int i = 0; i < 10; i++) {
            System.out.println(stringList.get(i));
        }

    }

}
