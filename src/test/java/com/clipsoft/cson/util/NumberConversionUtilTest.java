package com.clipsoft.cson.util;

import junit.framework.TestCase;
import org.junit.Test;

public class NumberConversionUtilTest extends TestCase {

    @Test
    public  void testNumberConvert() {
        Number value = NumberConversionUtil.stringToNumber(new char[]{'A','.','1'}, 1,2, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
         System.out.println(value);
         assertEquals(0.1, value.doubleValue(), 0.0000000);

        value = NumberConversionUtil.stringToNumber(new char[]{'A','.'}, 1,1, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(0.0, value.doubleValue(), 0.0000000);

        value = NumberConversionUtil.stringToNumber(new char[]{'A','+'}, 1,1, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(0.0, value.doubleValue(), 0.0000000);

        value = NumberConversionUtil.stringToNumber(new char[]{'A','+', '1', '2', '3', '.', '1', '4'}, 1,6, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(123.1, value.doubleValue(), 0.0000000);


        value = NumberConversionUtil.stringToNumber(new char[]{'A','-'}, 1,1, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(0.0, value.doubleValue(), 0.0000000);

         value = NumberConversionUtil.stringToNumber(new char[]{'A','-','.','1','2'}, 1,4, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);

        System.out.println(value);
        assertEquals(-0.12, value.doubleValue(), 0.0000000);

        value = NumberConversionUtil.stringToNumber(new char[]{'A','-','.'}, 1,2, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(-0.0, value.doubleValue(), 0.0000000);

        value = NumberConversionUtil.stringToNumber(new char[]{'A','-','1','.','1','2'}, 1,3, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(-1.0, value.doubleValue(), 0.0000000);

        value = NumberConversionUtil.stringToNumber(new char[]{'A','-','1','.','1','2'}, 1,4, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(-1.1, value.doubleValue(), 0.0000000);


        value = NumberConversionUtil.stringToNumber(new char[]{'A','0','x','f','f','f'}, 1,4, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value);
        assertEquals(255, value.intValue());


        value = NumberConversionUtil.stringToNumber(new char[]{'A','1','.','4','e','+','5'}, 1,6, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value.intValue());
        assertEquals(140000, value.intValue());

        value = NumberConversionUtil.stringToNumber(new char[]{'A','+','.','4','e','+','5'}, 1,6, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value.intValue());
        assertEquals(40000, value.intValue());

        value = NumberConversionUtil.stringToNumber(new char[]{'A','-','.','4','e','+','5'}, 1,6, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
        System.out.println(value.intValue());
        assertEquals(-40000, value.intValue());



    }

}