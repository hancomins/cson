package com.hancomins.cson;

import org.junit.Test;

import static org.junit.Assert.*;
public class ValueParseStateTest  {

    @Test
    public void nullTest() {
        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);
        state.reset();
        state.append("null    ");
        assertTrue(state.isNull());
        assertEquals(null, state.getNumber());
    }


    @Test
    public void testBreakLine() {
        String value = "value5!\\\n\\tbreak line";
        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);
        state.reset();
        state.append(value);
        assertEquals("value5!\n\tbreak line", state.toString());

    }


    @Test
    public void infinityParseTest() {

        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);
        state.reset();
        state.append("Infinity");
        assertEquals(Double.POSITIVE_INFINITY, state.getNumber());
        state.reset();
        state.append("-infinity");
        assertEquals(Double.NEGATIVE_INFINITY, state.getNumber());

        state.reset();
        state.append("+Infinity");
        assertEquals(Double.POSITIVE_INFINITY, state.getNumber());
        state.reset();
        state.append("+infinity");
        assertEquals(Double.POSITIVE_INFINITY, state.getNumber());

        state.reset();
        state.append("-infinity");
        assertEquals(Double.NEGATIVE_INFINITY, state.getNumber());


    }


    @Test
    public void nanParseTest() {

        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);
        state.reset();
        state.append("NaN");
        assertEquals(Double.NaN, state.getNumber());

        state.reset();
        state.append("nan");
        assertTrue(state.isNumber());
        assertEquals(Double.NaN, state.getNumber());

        state.reset();
        state.append("-NaN");
        assertTrue(state.isNumber());
        assertEquals("-NaN", state.toString());
        assertEquals(Double.NaN, state.getNumber());

        state.reset();
        state.append("+NaN");
        assertTrue(state.isNumber());
        assertEquals("+NaN", state.toString());
        assertEquals(Double.NaN, state.getNumber());
    }

    @Test
    public void numberParseTest() {
        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);


        state.reset();
        state.append("-128      ");
        assertEquals(-128, state.getNumber());

        state.reset();
        state.append("123");
        assertEquals(123, state.getNumber());
        state.reset();
        state.append("-123");
        assertEquals(-123,state.getNumber().intValue());
        state.reset();
        state.append("123.456");
        assertEquals(  Double.valueOf(123.456), Double.valueOf( state.getNumber().doubleValue() )  );
        state.reset();
        state.append("-123.456");
        assertEquals( Double.valueOf(-123.456), Double.valueOf( state.getNumber().doubleValue()));
        state.reset();
        state.append("+123.456");
        assertEquals( Double.valueOf(123.456), Double.valueOf( state.getNumber().doubleValue()));
        state.reset();
        state.append("+123.45.6");
        assertFalse(state.isNumber());
        assertEquals("+123.45.6", state.toString());

        state.reset();
        state.append(".456");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(0.456), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("-.456");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(-0.456), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("+.456");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(0.456), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("0.0");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(0.0), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("-0.0");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(0.0), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("+0.0");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(0.0), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("+0.0001");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(0.0001), Double.valueOf( state.getNumber().doubleValue()));

        state.reset();
        state.append("-0.0001");
        assertTrue(state.isNumber());
        assertEquals(Double.valueOf(-0.0001), Double.valueOf( state.getNumber().doubleValue()));

    }

    @Test
    public void escapeChar() {
        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);

        state.reset();
        state.setAllowControlChar(false);
        try {
            state.append("sdafadsfadsf\tsdafdasfadsf");
            assert false;
        } catch (Exception e) {
            assertTrue(e instanceof CSONException);
        }
        state.setAllowControlChar(true);





        state.reset();
        state.append("\\\\\t");
        assertEquals("\\\t", state.toString());


        state.reset();
        state.append("O\\n\\nK\\u{1F600}\\u24F8\u24F8\uD83D\uDE00\\\"");

        System.out.println(state.toString());
        assertEquals("O\n\nK\uD83D\uDE00\u24F8\u24F8\uD83D\uDE00\"", state.toString());



    }


    @Test
    public void exponentExpressionTest() {
        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);

        state.reset();
        state.append("123e1");
        assertTrue(state.isNumber())    ;
        assertEquals(1230, state.getNumber().intValue());
        state.reset();
        state.append("123e+1");
        assertEquals(1230, state.getNumber().intValue());
        state.reset();
        state.append("123e-1");
        assertEquals(12.3, state.getNumber().doubleValue(), 0.0001);
        state.reset();
        state.append("123e-1.1");
        assertFalse(state.isNumber());
        assertEquals("123e-1.1", state.toString());
        state.reset();
        state.append("123e-1.1.1");
        assertFalse(state.isNumber());
        assertEquals("123e-1.1.1", state.toString());


    }

    @Test
    public void hexValueTest() {
        StringFormatOption<?> json5Option = StringFormatOption.json5();
        ValueParseState state = new ValueParseState(json5Option);

        state.reset();
        state.append("+0x123");
        assertTrue(state.isNumber());
        assertEquals(291, state.getNumber().intValue());


        state.reset();
        state.append("-0x123");
        assertTrue(state.isNumber());
        assertEquals(-291, state.getNumber().intValue());

        state.reset();
        state.append("0x123");
        assertTrue(state.isNumber());
        assertEquals(291, state.getNumber().intValue());

        state.reset();
        state.append("0X123fF");
        assertTrue(state.isNumber());
        assertEquals(74751, state.getNumber().intValue());
        state.reset();
        state.append("0X123.1");
        assertFalse(state.isNumber());
        assertEquals("0X123.1", state.toString());
        state.reset();
        state.append("0x123.1.1");
        assertFalse(state.isNumber());
        assertEquals("0x123.1.1", state.toString());
    }


}