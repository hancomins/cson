package com.hancomins.cson;

import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Test;

public class JSON5ParserTest extends TestCase {

    @Test
    public void testUnquoted() {

        char q = '\t';
        System.out.println(Character.isWhitespace(q));

        String jsonKeyUnquoted = "{\n" +
                "  unquoted: 'and you can quote me on that\"',\n" +
                "}";



        CSONObject csonObject = new CSONObject(jsonKeyUnquoted, StringFormatOption.json());
        System.out.println(csonObject.toString());

        assertEquals(csonObject.get("unquoted"), "and you can quote me on that\"");


        String jsonValueUnquoted = "{\n" +
                "  unquoted: and you can quote\n me on that\",\n" +
                " unquoted_integer: 123.0\n" +
                "}";

        //JSONObject jsonObject = new JSONObject(jsonValueUnquoted);
        csonObject = new CSONObject(jsonValueUnquoted, StringFormatOption.json());
        System.out.println(csonObject.toString());
        assertEquals(csonObject.get("unquoted"), "and you can quote\n me on that\"");
        assertEquals(Double.valueOf(csonObject.getDouble("unquoted_integer")),  Double.valueOf( 123.0));


        String jsonValueSingleUnquoted = "{\n" +
                "  'singleQuoted': and you can quote\n me on that\",\n" +
                " 'singleQuoted_float': 123.0\n" +
                "}";

        //JSONObject jsonObject = new JSONObject(jsonValueUnquoted);
        csonObject = new CSONObject(jsonValueSingleUnquoted, StringFormatOption.json());
        System.out.println(csonObject.toString());
        assertEquals(csonObject.get("singleQuoted"), "and you can quote\n me on that\"");
        assertEquals(Double.valueOf(csonObject.getDouble("singleQuoted_float")),  Double.valueOf( 123.0));





        /**JSON5Parser parser = new JSON5Parser();
        String json = "{\n" +
                "  // comments\n" +
                "  unquoted: 'and you can quote me on that',\n" +
                "  singleQuotes: 'I can use \"double quotes\" here',\n" +
                "  lineBreaks: \"Look, Mom!\\\nNo \\\\nnewlines!\",\n" +
                "  hexadecimal: 0xdecaf,\n" +
                "  leadingDecimalPoint: .8675309, andTrailing: 8675309.,\n" +
                "  positiveSign: +1,\n" +
                "  trailingComma: 'in objects', andIn: ['arrays',],\n" +
                "  \"backwardsCompatible\": \"with JSON\",\n" +
                "}";
        CSONObject csonObject =
        assertEquals("and you can quote me on that", csonObject.optString("unquoted"));
        assertEquals("I can use \"double quotes\" here", csonObject.optString("singleQuotes"));
        assertEquals("Look, Mom!\nNo \\\nnewlines!", csonObject.optString("lineBreaks"));
        assertEquals(0xdecaf, csonObject.optInt("hexadecimal"));
        assertEquals(0.8675309, csonObject.optDouble("leadingDecimalPoint"));
        assertEquals(8675309.0, csonObject.optDouble("andTrailing"));
        assertEquals(1, csonObject.optInt("positiveSign"));
        assertEquals("in objects", csonObject.optString("trailingComma"));
        assertEquals("with JSON", csonObject.optString("backwardsCompatible"));**/
    }

    @Test
    public void testConsecutiveCommas() {

            String json = "{\n" +
                    "  \"consecutiveCommas\": \"are just fine\",,,\n" +
                    " nullValue :  ,\n" +
                    " arrays: [1,2,,3,],\n" +

                    "}";


            CSONObject csonObject = new CSONObject(json, StringFormatOption.json().setAllowConsecutiveCommas(true));
            System.out.println(csonObject);
            assertEquals("are just fine", csonObject.optString("consecutiveCommas"));
            assertNull(csonObject.get("nullValue"));
            assertEquals(4, csonObject.optCSONArray("arrays").size());
            assertEquals(1, csonObject.optCSONArray("arrays").getInt(0));
            assertEquals(2, csonObject.optCSONArray("arrays").getInt(1));
            assertNull(csonObject.optCSONArray("arrays").get(2));
            assertEquals(3, csonObject.optCSONArray("arrays").getInt(3));

            try {
                csonObject = new CSONObject(json, StringFormatOption.json().setAllowConsecutiveCommas(false));
                assertEquals("are just fine", csonObject.optString("consecutiveCommas"));
                fail();
            } catch (Exception e) {

            }
    }

    @Test
    public void testTrailingComma() {

            String json = "{\n" +
                    "  \"trailingComma\": \"in objects\",\n" +
                    "  \"andIn\": [\"arrays\",],\n" +
                    "}";

            CSONObject csonObject = new CSONObject(json, StringFormatOption.json().setAllowTrailingComma(true));
            assertEquals("in objects", csonObject.optString("trailingComma"));
            assertEquals("arrays", csonObject.optCSONArray("andIn").optString(0));

            try {
                csonObject = new CSONObject(json, StringFormatOption.json().setAllowTrailingComma(false));
                assertEquals("in objects", csonObject.optString("trailingComma"));
                assertEquals("arrays", csonObject.optCSONArray("andIn").optString(0));
                fail();
            } catch (Exception e) {

            }

    }

    @Test
    public void testPerformance() {


        String speedTest = "{\n" +
                "  unquoted: and you can quote me on that," +
                " unquoted_integer: 123" +
                "}";

        StringFormatOption<?> jsonOption = StringFormatOption.json();

        long start = 0;
        for(int c = 0; c < 100; ++c) {


            start = System.currentTimeMillis();
            for (int i = 0; i < 10000000; i++) {
                JSONObject jsonObject = new JSONObject(speedTest);
                jsonObject.getString("unquoted");
                jsonObject.getFloat("unquoted_integer");
            }
            System.out.println("JSON: " + (System.currentTimeMillis() - start));


            start = System.currentTimeMillis();
            for (int i = 0; i < 10000000; i++) {
                CSONObject csonObject1 = new CSONObject(speedTest, jsonOption);
                csonObject1.getString("unquoted");
                csonObject1.getFloat("unquoted_integer");
            }
            System.out.println("CSON: " + (System.currentTimeMillis() - start));

        }
    }

}