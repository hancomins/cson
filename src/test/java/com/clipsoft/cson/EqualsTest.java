package com.clipsoft.cson;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EqualsTest {

    @Test
    public void test() {

        String v1 = "{\"host\":null,\"port\":0}";
        String v2 = "{\"host\":\"localhost\",\"port\":8080}";
        CSONObject v1CSON = new CSONObject(v1);
        CSONObject v2CSON = new CSONObject(v2);
        assertNotEquals(v1CSON, v2CSON);

    }

    @Test
    public void numberEquals() {
        CSONElements.equalsNumber(111.0, 111);
        CSONElements.equalsNumber(111.0000, 111f);
        CSONElements.equalsNumber(111.0000, 111d);
        CSONElements.equalsNumber(111.0000, BigInteger.valueOf(111));
        CSONElements.equalsNumber(111.0000, BigDecimal.valueOf(111));
        CSONElements.equalsNumber(111.001, BigDecimal.valueOf(111.001));

    }
}
