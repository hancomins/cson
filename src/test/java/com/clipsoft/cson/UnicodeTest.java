package com.clipsoft.cson;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class UnicodeTest {

    // 리소스의 config-store-test.json5 파일을 string 으로 읽어오는 메서드.
    public String readConfigStoreTest() {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("config-store-test.json5")) {
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            return new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Test
    public void testBrokenString() {
        CSONObject csonObject = new CSONObject("{a:'uceab'}", StringFormatOption.json5());
        CSONArray csonArray = new CSONArray("[\"uceab\"]", StringFormatOption.json5());

        assertEquals("uceab", csonObject.get("a"));
        assertEquals("uceab", csonArray.get(0));

    }

    @Test
    public void testHex() {
        CSONObject csonObject = new CSONObject("{a:'0xceab'}", StringFormatOption.json5());
        CSONArray csonArray = new CSONArray("[0xceab]", StringFormatOption.json5());

        assertEquals('캫', csonObject.get("a"));
        assertEquals(52907, csonArray.getInt(0));

    }

    @Test
    public void testUnicode() {
        CSONObject csonObject = new CSONObject("{a:'\\uD83D\\uDE0A', b: '\\uceab'}", StringFormatOption.json5());
        CSONArray csonArray = new CSONArray("['\\uD83D\\uDE0A']", StringFormatOption.json5());

        assertEquals("😊", csonObject.get("a"));
        assertEquals("😊", csonArray.get(0));

        System.out.println(csonObject.toString());

        csonObject = new CSONObject(csonObject.toString(), StringFormatOption.json5());
        csonArray = new CSONArray(csonArray.toString(), StringFormatOption.json5());

        assertEquals("😊", csonObject.get("a"));
        assertEquals("😊", csonArray.get(0));

        csonObject = new CSONObject(csonObject.toString(StringFormatOption.jsonPure()));
        csonArray = new CSONArray(csonArray.toString(StringFormatOption.jsonPure()));

        assertEquals("😊", csonObject.get("a"));
        assertEquals("😊", csonArray.get(0));

        assertEquals("캫", csonObject.get("b"));

        String pure = "{\"a\":\"하\\uD83D\\uDE0A하\", \"b\": \"\\uceab\"}";
        System.out.println(pure);
        csonObject = new CSONObject(pure);


        assertEquals("하😊하", csonObject.get("a"));
        assertEquals("캫", csonObject.get("b"));



    }
}
