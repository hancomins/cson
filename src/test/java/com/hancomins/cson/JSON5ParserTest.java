package com.hancomins.cson;

import junit.framework.TestCase;
import org.junit.Test;

public class JSON5ParserTest extends TestCase {

    @Test
    public void testUnquoted() {
        String json = "{\n" +
                "  unquoted: 'and you can quote me on that',\n" +
                "}";

        CSONObject csonObject = new CSONObject(json, StringFormatOption.json());


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

}