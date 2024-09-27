package com.hancomins.cson.internal.issue;


import com.hancomins.cson.internal.CSONObject;
import com.hancomins.cson.options.ParsingOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * json5 에서 '' 으로 감싸져있는 빈 문자열을 읽으면 숫자 0 으로 인식
 * https://github.com/clipsoft-rnd/cson/issues/2
 */
public class Issues2 {

    @Test
    public void test() {
        CSONObject cson = new CSONObject("{a: '', b: ['']}", ParsingOptions.json5());
        System.out.println(cson);
        assertEquals("", cson.get("a").toString());
        assertEquals("", cson.getCSONArray("b").getString(0));

    }

}
