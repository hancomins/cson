package com.hancomins.cson;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathItemTest {

    @Test
    public void mixedMultiPathParseTest() {
        List<PathItem> pathItemBasic = PathItem.parseMultiPath2("path1[10].path2[1][2]path3.path4");
        assertEquals("path1", pathItemBasic.get(0).getName());
        assertTrue(pathItemBasic.get(0).isArrayValue());
        assertTrue(pathItemBasic.get(0).isObject());
        assertFalse(pathItemBasic.get(0).isInArray());

        assertEquals(10, pathItemBasic.get(1).getIndex());
        assertEquals("path2", pathItemBasic.get(1).getName());
        assertTrue(pathItemBasic.get(1).isArrayValue());
        assertTrue(pathItemBasic.get(1).isObject());
        assertTrue(pathItemBasic.get(1).isInArray());

        PathItem pathItem3 = pathItemBasic.get(3);
        assertEquals(2, pathItem3.getIndex());
        assertTrue(pathItem3.isInArray());
        assertFalse(pathItem3.isArrayValue());
        assertTrue(pathItem3.isObject());

        PathItem pathItem4 = pathItemBasic.get(4);
        assertEquals(-1, pathItem4.getIndex());
        assertFalse(pathItem4.isInArray());
        assertFalse(pathItem4.isArrayValue());










    }

    @Test
    public void multiArrayPathParseTest() {
        List<PathItem> pathItemBasic = PathItem.parseMultiPath2("key1.key3.key2.key4");
        assertEquals("key1", pathItemBasic.get(0).getName());
        assertEquals("key3", pathItemBasic.get(1).getName());
        assertEquals("key2", pathItemBasic.get(2).getName());
        assertEquals("key4", pathItemBasic.get(3).getName());

        List<PathItem> pathItems = PathItem.parseMultiPath2("key1[0][1][2][3]");
        assertEquals("key1", pathItems.get(0).getName());
        assertEquals(0, pathItems.get(1).getIndex());
        assertEquals(1, pathItems.get(2).getIndex());
        assertEquals(2, pathItems.get(3).getIndex());
        assertEquals(3, pathItems.get(4).getIndex());

        List<PathItem>  pathItemsB = PathItem.parseMultiPath2("key1[0].key2[1].key3[2].pathKey[3]");
        assertEquals("key1", pathItemsB.get(0).getName());
        assertTrue(pathItemsB.get(0).isArrayValue());
        assertFalse(pathItemsB.get(0).isInArray());
        assertEquals(0, pathItemsB.get(1).getIndex());
        assertEquals("key2", pathItemsB.get(1).getName());
        assertTrue(pathItemsB.get(1).isArrayValue());
        assertTrue(pathItemsB.get(1).isInArray());
        assertEquals("key3", pathItemsB.get(2).getName());
        assertTrue(pathItemsB.get(2).isArrayValue());
        assertTrue(pathItemsB.get(2).isInArray());
        assertEquals("pathKey", pathItemsB.get(3).getName());
        assertTrue(pathItemsB.get(3).isArrayValue());
        assertTrue(pathItemsB.get(3).isInArray());
        assertEquals(3, pathItemsB.get(4).getIndex());
        assertEquals("", pathItemsB.get(4).getName());
        assertTrue(pathItemsB.get(4).isInArray());
        assertFalse(pathItemsB.get(4).isArrayValue());
        assertTrue(pathItemsB.get(4).isEndPoint());





        pathItemsB = PathItem.parseMultiPath2("key1[0]key2[1]key3[2]pathKey[3]");
        assertEquals("key1", pathItemsB.get(0).getName());
        assertTrue(pathItemsB.get(0).isArrayValue());
        assertFalse(pathItemsB.get(0).isInArray());
        assertEquals(-1, pathItemsB.get(0).getIndex());

        assertEquals("key2", pathItemsB.get(1).getName());
        assertTrue(pathItemsB.get(1).isArrayValue());
        assertTrue(pathItemsB.get(1).isInArray());
        assertEquals(0, pathItemsB.get(1).getIndex());


        assertEquals("key3", pathItemsB.get(2).getName());
        assertTrue(pathItemsB.get(2).isArrayValue());
        assertTrue(pathItemsB.get(2).isInArray());
        assertEquals(1, pathItemsB.get(2).getIndex());


        assertEquals("pathKey", pathItemsB.get(3).getName());
        assertTrue(pathItemsB.get(3).isArrayValue());
        assertTrue(pathItemsB.get(3).isInArray());
        assertEquals(2, pathItemsB.get(3).getIndex());

        assertEquals(3, pathItemsB.get(4).getIndex());
        assertEquals("", pathItemsB.get(4).getName());
        assertTrue(pathItemsB.get(4).isInArray());
        assertFalse(pathItemsB.get(4).isArrayValue());
        assertTrue(pathItemsB.get(4).isEndPoint());

        List<PathItem> pathItemsC = PathItem.parseMultiPath2("[100][0][1][2][3]");

        assertEquals(100, pathItemsC.get(0).getIndex());
        assertTrue(pathItemsC.get(0).isInArray());
        assertTrue(pathItemsC.get(0).isArrayValue());

        assertEquals(0, pathItemsC.get(1).getIndex());
        assertTrue(pathItemsC.get(1).isInArray());
        assertTrue(pathItemsC.get(1).isArrayValue());

        assertEquals(1, pathItemsC.get(2).getIndex());
        assertTrue(pathItemsC.get(2).isInArray());
        assertTrue(pathItemsC.get(2).isArrayValue());

        assertEquals(2, pathItemsC.get(3).getIndex());
        assertTrue(pathItemsC.get(3).isInArray());
        assertTrue(pathItemsC.get(3).isArrayValue());

        assertEquals(3, pathItemsC.get(4).getIndex());
        assertTrue(pathItemsC.get(4).isInArray());
        assertTrue(pathItemsC.get(4).isEndPoint());
        assertFalse(pathItemsC.get(4).isArrayValue());



    }


}