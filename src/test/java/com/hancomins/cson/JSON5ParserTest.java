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
    public void testLineBreak() {

        String json = "{\n" +
                //"  // comments\n" +
                "  lineBreaks: \"Look, Mom!\\\nNo \\\\nnewlines!\",\n" +
                "}";



        CSONObject csonObject = new CSONObject(json, StringFormatOption.json());
        assertEquals("Look, Mom!\nNo \\nnewlines!", csonObject.optString("lineBreaks"));

    }

    @Test
    public void testSimpleJson5Parse() {

         String json = "{\n" +
         //"  // comments\n" +
         "  unquoted: 'and you can quote me on that',\n" +
         "  singleQuotes: 'I can use \"double quotes\" here',\n" +
         "  lineBreaks: \"Look, Mom!\\\nNo \\\\nnewlines!\",\n" +
         "  hexadecimal: 0xdecaf,\n" +
         "  leadingDecimalPoint: .8675309, andTrailing: 8675309.,\n" +
         "  positiveSign: +1,\n" +
         "  trailingComma: 'in objects', andIn: ['arrays',],\n" +
         "  \"backwardsCompatible\": \"with JSON\",\n" +
         "}";
         CSONObject csonObject = new CSONObject(json, StringFormatOption.json());
         assertEquals("and you can quote me on that", csonObject.optString("unquoted"));
         assertEquals("I can use \"double quotes\" here", csonObject.optString("singleQuotes"));
         assertEquals("Look, Mom!\nNo \\nnewlines!", csonObject.optString("lineBreaks"));
         assertEquals(0xdecaf, csonObject.optInt("hexadecimal"));
         assertEquals(0.8675309, csonObject.optDouble("leadingDecimalPoint"));
         assertEquals(8675309.0, csonObject.optDouble("andTrailing"));
         assertEquals(1, csonObject.optInt("positiveSign"));
         assertEquals("in objects", csonObject.optString("trailingComma"));
         assertEquals("with JSON", csonObject.optString("backwardsCompatible"));
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
    public void testNullValue() {
        String complexJson5 = "{\n" +
                "  nullValue: \n\n null\n\n,\n" +
                " okValue: \"ok\",\n" +
                "}";
        CSONObject csonObject = new CSONObject(complexJson5, StringFormatOption.json());
        assertNull(csonObject.opt("nullValue"));
        assertEquals("ok", csonObject.optString("okValue"));
    }

    @Test
    public void testComment() {
        String complexJson5 = "{\n" +
                "  // This is a comment\n" +
                "}";
        CSONObject csonObject = new CSONObject(complexJson5, StringFormatOption.json());

    }


        @Test
    public void testComplexJson5Parsing() {
        String complexJson5 = "{\n" +
                "  unquotedKey: 'unquoted string value',\n" +
                "  'singleQuotes': \"can use double quotes inside\",\n" +
                "  nestedObject: {\n" +
                "    array: [1, 2, 3\n, { nestedKey: 'nestedValue' }, ['nested', 'array']],\n" +
                "    boolean: true,\n" +
                "  },\n" +
                "  nullValue: null,\n" +
                "  // This is a comment\n" +
                "  trailingComma: \n'this is fine',\n" +
                "  trailing1Comma: 'this is fine',\n" +
                "}";

        CSONObject csonObject = new CSONObject(complexJson5, StringFormatOption.json());

        // Assert basic values
        assertEquals("unquoted string value", csonObject.optString("unquotedKey"));
        assertEquals("can use double quotes inside", csonObject.optString("singleQuotes"));
        assertNull(csonObject.opt("nullValue"));
        assertTrue(csonObject.optCSONObject("nestedObject").optBoolean("boolean"));

        // Assert nested object and array
        CSONObject nestedObject = csonObject.optCSONObject("nestedObject");
        assertNotNull(nestedObject);
        assertEquals(3, nestedObject.optCSONArray("array").getInt(2));

        // Assert nested array within an array
        CSONArray nestedArray = nestedObject.optCSONArray("array").optCSONArray(4);
        assertEquals("nested", nestedArray.optString(0));

        // Assert nested object within an array
        CSONObject nestedObjectInArray = nestedObject.optCSONArray("array").optCSONObject(3);
        assertEquals("nestedValue", nestedObjectInArray.optString("nestedKey"));

        System.out.println(csonObject);
    }

    @Test
    public void testPerformance() {
        String speedTest = "{\"name\":\"John Doe\",\"age\":30,\"isEmployed\":true,\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"12345\"},\"phoneNumbers\":[{\"type\":\"home\",\"number\":\"555-555-5555\"},{\"type\":\"work\",\"number\":\"555-555-5556\"}],\"email\":\"johndoe@example.com\",\"website\":\"http://www.johndoe.com\",\"children\":[{\"name\":\"Jane Doe\",\"age\":10,\"school\":{\"name\":\"Elementary School\",\"address\":{\"street\":\"456 School St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"12345\"}}},{\"name\":\"Jim Doe\",\"age\":8,\"school\":{\"name\":\"Elementary School\",\"address\":{\"street\":\"456 School St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"12345\"}}}],\"hobbies\":[\"reading\",\"hiking\",\"coding\"],\"education\":{\"highSchool\":{\"name\":\"Anytown High School\",\"yearGraduated\":2005},\"university\":{\"name\":\"State University\",\"yearGraduated\":2009,\"degree\":\"Bachelor of Science\",\"major\":\"Computer Science\"}},\"workExperience\":[{\"company\":\"Tech Corp\",\"position\":\"Software Engineer\",\"startDate\":\"2010-01-01\",\"endDate\":\"2015-01-01\",\"responsibilities\":[\"Developed web applications\",\"Led a team of 5 developers\",\"Implemented new features\"]},{\"company\":\"Web Solutions\",\"position\":\"Senior Developer\",\"startDate\":\"2015-02-01\",\"endDate\":\"2020-01-01\",\"responsibilities\":[\"Architected software solutions\",\"Mentored junior developers\",\"Managed project timelines\"]}],\"skills\":[{\"name\":\"Java\",\"level\":\"expert\"},{\"name\":\"JavaScript\",\"level\":\"advanced\"},{\"name\":\"Python\",\"level\":\"intermediate\"}],\"certifications\":[{\"name\":\"Certified Java Developer\",\"issuedBy\":\"Oracle\",\"date\":\"2012-06-01\"},{\"name\":\"Certified Scrum Master\",\"issuedBy\":\"Scrum Alliance\",\"date\":\"2014-09-01\"}],\"languages\":[{\"name\":\"English\",\"proficiency\":\"native\"},{\"name\":\"Spanish\",\"proficiency\":\"conversational\"}],\"projects\":[{\"name\":\"Project Alpha\",\"description\":\"A web application for managing tasks\",\"technologies\":[\"Java\",\"Spring Boot\",\"React\"],\"role\":\"Lead Developer\",\"startDate\":\"2018-01-01\",\"endDate\":\"2019-01-01\"},{\"name\":\"Project Beta\",\"description\":\"A mobile app for tracking fitness\",\"technologies\":[\"Kotlin\",\"Android\",\"Firebase\"],\"role\":\"Developer\",\"startDate\":\"2019-02-01\",\"endDate\":\"2020-01-01\"}]}";



                /*"{\n" +
                "  unquoted: and you can quote me on that," +
                " unquoted_integer: 123" +
                "}";*/

        StringFormatOption<?> jsonOption = StringFormatOption.json();

        long start = 0;
        for(int c = 0; c < 100; ++c) {


            start = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                JSONObject jsonObject = new JSONObject(speedTest);
                //jsonObject.getString("unquoted");
                //jsonObject.getFloat("unquoted_integer");
            }
            System.out.println("org.json: " + (System.currentTimeMillis() - start));


            start = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                CSONObject csonObject1 = new CSONObject(speedTest, jsonOption);
                //csonObject1.getString("unquoted");
                //csonObject1.getFloat("unquoted_integer");
            }
            System.out.println("CSON: " + (System.currentTimeMillis() - start));

        }
    }

}