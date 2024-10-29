package com.clipsoft.cson;

import org.junit.Test;

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
}
