package com.hancomins.cson;

import com.hancomins.cson.options.ParsingOptions;
import com.hancomins.cson.util.NullValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("ValueBufferTest (성공)")
public class ValueBufferTest {

    @Test
    @DisplayName("Null 값 파싱 테스트")
    public void nullTest() {
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);
        state.reset();
        state.append("null");
        Object object = state.parseValue();
        assertEquals(NullValue.Instance, object);
    }


    @Test
    @DisplayName("내려쓰기 테스트")
    public void testBreakLine() {
        String value = "value5!\\\n\\tbreak line";
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);
        state.reset();
        state.append(value);
        assertEquals("value5!\n\tbreak line", state.toString());

    }


    @Test
    @DisplayName("Infinity 값 파싱 테스트")
    public void infinityParseTest() {

        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);
        state.reset();
        state.append("Infinity");
        Object value = state.parseValue();
        assertEquals(Double.POSITIVE_INFINITY, value);
        state.reset();
        state.append("-infinity");
        value = state.parseValue();
        assertEquals(Double.NEGATIVE_INFINITY, value);

        state.reset();
        state.append("+Infinity");
        value = state.parseValue();
        assertEquals(Double.POSITIVE_INFINITY, value);
        state.reset();
        state.append("+infinity");
        value = state.parseValue();
        assertEquals(Double.POSITIVE_INFINITY, value);

        state.reset();
        state.append("-infinity");
        value = state.parseValue();
        assertEquals(Double.NEGATIVE_INFINITY, value);

        json5Option = ParsingOptions.json5().setAllowInfinity(false);
        state = new ValueBuffer(json5Option);
        state.reset();
        state.append("+Infinity");
        value = state.parseValue();
        assertEquals("+Infinity", value);

        json5Option = ParsingOptions.json5().setAllowInfinity(false).setIgnoreNonNumeric(false);
        state = new ValueBuffer(json5Option);
        state.reset();
        state.append("+Infinity");
        assertEquals("+Infinity", state.parseValue());


    }


    @Test
    @DisplayName("NaN 값 파싱 테스트")
    public void nanParseTest() {

        Object value;
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);
        state.reset();
        state.append("NaN");
        value = state.parseValue();
        assertEquals(Double.NaN, value);

        state.reset();
        state.append("nan");
        value = state.parseValue();
        assertEquals(Double.NaN, value);


    }

    @Test
    @DisplayName( "숫자 파싱 테스트")
    public void numberParseTest() {
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);


        state.reset();
        state.append("-128");
        assertEquals(-128, state.parseNumber());

        state.reset();
        state.append("123");
        assertEquals(123, state.parseNumber());
        state.reset();
        state.append("-123");
        assertEquals(-123,state.parseNumber().intValue());
        state.reset();
        state.append("123.456");
        assertEquals(  Double.valueOf(123.456), Double.valueOf( state.parseNumber().doubleValue() )  );
        state.reset();
        state.append("-123.456");
        assertEquals( Double.valueOf(-123.456), Double.valueOf( state.parseNumber().doubleValue()));
        state.reset();
        state.append("+0123.456");
        assertEquals( Double.valueOf(123.456), Double.valueOf( state.parseNumber().doubleValue()));

        state.reset();
        state.append("+123.456");
        assertEquals( Double.valueOf(123.456), Double.valueOf( state.parseNumber().doubleValue()));
        state.reset();
        state.append("+123.45.6");
        Object value = state.parseValue();
        assertEquals("+123.45.6", value);

        state.reset();
        state.append(".456");
        Number number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0.456), Double.valueOf(number.doubleValue()));

        state.reset();
        state.append("-.456");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(-0.456), Double.valueOf( number.doubleValue()));

        state.reset();
        state.append("+.456");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0.456), Double.valueOf(number.doubleValue()));

        state.reset();
        state.append("0.0");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0.0), Double.valueOf( number.doubleValue()));

        state.reset();
        state.append("-0.0");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0.0), Double.valueOf(number.doubleValue()));

        state.reset();
        state.append("+0.0");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0.0), Double.valueOf( number.doubleValue()));

        state.reset();
        state.append("+0.0001");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0.0001), Double.valueOf( number.doubleValue()));

        state.reset();
        state.append("-00.0001");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(-0.0001), Double.valueOf( number.doubleValue()));


        state.reset();
        state.append("0");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0), Double.valueOf( number.doubleValue()));

        state.reset();
        state.append("00000");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(0), Double.valueOf( number.doubleValue()));


        state.reset();
        state.append("000008");
        number = state.parseNumber();
        assertNotNull(number);
        assertEquals(Double.valueOf(8), Double.valueOf( number.doubleValue()));

    }

    @Test
    public void escapeChar() {
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);

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
    @DisplayName("지수 파싱 테스트")
    public void exponentExpressionTest() {
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);

        state.reset();
        state.append("123e1");
        Number numberValue = (Number) state.parseValue();
        assertEquals(1230, numberValue.intValue());
        state.reset();
        state.append("123e+1");
        numberValue = (Number) state.parseValue();
        assertEquals(1230, numberValue.intValue());
        state.reset();
        state.append("123e-1");
        numberValue = state.parseNumber();
        assertEquals(12.3, numberValue.doubleValue(), 0.0001);
        state.reset();
        state.append("123e-1.1");
        Object value = state.parseValue();
        assertEquals("123e-1.1", value);
        state.reset();
        state.append("123e-1.1.1");
        value = state.parseValue();
        assertEquals("123e-1.1.1", value);

        state.append("123e--123132");
        value = state.parseValue();
        assertEquals("123e--123132", value);

        state.append("123eE123132");
        value = state.parseValue();
        assertEquals("123eE123132", value);
    }



    @Test
    @DisplayName("16진수 파싱 테스트")
    public void hexValueTest() {
        ParsingOptions<?> json5Option = ParsingOptions.json5();
        ValueBuffer state = new ValueBuffer(json5Option);

        state.reset();
        state.append("+0x123");
        Number number = state.parseNumber();
        assertEquals(291, number.intValue());


        state.reset();
        state.append("-0x123");
        number = state.parseNumber();
        assertEquals(-291, number.intValue());

        state.reset();
        state.append("0x123");
        number = state.parseNumber();
        assertEquals(291, number.intValue());

        state.reset();
        state.append("0X123fF");
        number = state.parseNumber();
        assertEquals(74751, number.intValue());
        state.reset();
        state.append("0X123.1");
        Object value = state.parseValue();
        assertEquals("0X123.1", value.toString());
        state.reset();
        state.append("0x123.1.1");
        value = state.parseValue();
        assertEquals("0x123.1.1", value.toString());
    }


}