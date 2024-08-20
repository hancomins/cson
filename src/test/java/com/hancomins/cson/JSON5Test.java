package com.hancomins.cson;

import org.json.JSONArray;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSON5Test {


    @Test
    public void testA() {
        CSONArray csonArray = new CSONArray("[\"value5!\n\tbreak line\"]", JSONOptions.json5().setAllowControlChar(true));
        assertEquals("value5!\n\tbreak line",csonArray.getString(0));

    }


    @Test
    public void test() {
        CSONObject csonObject = new CSONObject("{key: \"value\", key 2: \"value2\", key3: 'value3'," +
                " key4: value4 ," +
                " 'key5': \"value5!\\tbreak line\", object: {key: value,}, 'byte[]': [+1,+2,+3,+4,5,6,7,8,9,10,Infinity,NaN,],  }", JSONOptions.json5());

        assertEquals("value",csonObject.get("key"));
        assertEquals("value2",csonObject.get("key 2"));
        assertEquals("value3",csonObject.get("key3"));
        assertEquals("value4",csonObject.get("key4"));
        assertEquals("value5!\tbreak line",csonObject.get("key5"));
        assertEquals(12,csonObject.getCSONArray("byte[]").size());
        CSONArray array =  csonObject.getCSONArray("byte[]");
        assertTrue(Double.isInfinite(array.getDouble(10)));
        assertTrue(Double.isNaN(array.getDouble(11)));



    }

    @Test
    public void testTopComment() {
        CSONObject csonObject = new CSONObject("// 루트코멘트 \n { \n" +
                "// 코멘트입니다. \n " +
                " key: \"value\" }" , JSONOptions.json5());

        assertEquals("루트코멘트",csonObject.getHeadComment());

        csonObject = new CSONObject("// 루트코멘트 \n// 코멘트입니다222. \n" +
                "  /* 코\n멘\n트\r\n는\n\t주\n석\n\n\n\n*/" +
                "{ \n" +
                "// 코멘트입니다. \n" +
                " key: \"value\" }\n\n\n\n\n\r\n\t\t\t\t\t ", JSONOptions.json5()   );

        assertEquals("루트코멘트\n코멘트입니다222.\n코\n" +
                "멘\n" +
                "트\r\n" +
                "는\n" +
                "\t주\n" +
                "석",csonObject.getHeadComment());

        assertEquals("value",csonObject.get("key"));
    }

    @Test
    public void testKeyCommentSimple() {
        //CSONObject csonObject = new CSONObject("{key:'value', /* 여기에도 코멘트가 존재 가능*/ } /** 여기도 존재가능 **/"  );

        CSONObject  csonObject = new CSONObject("{key:'value',} ", JSONOptions.json5());
        assertEquals("value", csonObject.get("key"));

        csonObject = new CSONObject("{key:'value', // 코멘트X \n }// 코멘트", JSONOptions.json5());
        assertEquals("value", csonObject.get("key"));
        assertEquals("코멘트", csonObject.getTailComment());

        csonObject = new CSONObject("{key:'value' } // 코멘트 "  , JSONOptions.json5());
        assertEquals("코멘트", csonObject.getTailComment());
        csonObject = new CSONObject("{key:'value',} // 코멘트 \n // 코멘트2" , JSONOptions.json5());
        assertEquals("코멘트\n코멘트2", csonObject.getTailComment());
    }

    @Test
    public void testArrayComment() {
        JSONArray jsonArray = new JSONArray("[1,,2,3,4,5,6,7,8,9,10,Infinity,NaN,]"  );
        Object obj =  jsonArray.get(1);

        CSONArray csonArray = null;
        csonArray = new CSONArray("[//index1\n1,2,3,4,5,6,7,8,9,10,Infinity,NaN,] // 코멘트 \n // 코멘트2" , JSONOptions.json5());
        assertEquals("index1",csonArray.getCommentObject(0).getBeforeComment());


        csonArray = new CSONArray("/*테*///스\n" +
                "/*트*/[//index1\n" +
                "1\n" +
                "//index1After\n" +
                ",,/* 이 곳에 주석 가능 */,\"3 \"/*index 3*/,4,5,6,7,8,9,10,11," +
                "/*오브젝트 시작*/{/*알수없는 영역*/}/*오브젝트끝*/,13,//14\n" +
" {/*123*/123:456//456\n,},/*15배열로그*/[,,]/*15after*/,[],[],-Infinity,NaN," +
                ",[{},{}],[,/*index22*/],[,/*index23*/]/*index23after*/,24,[,]//index25after\n," +
                "{1:2,//코멘트\n}//코멘트\n,] // 코멘트 \n // 코멘트2",  JSONOptions.json5());
        System.out.println(csonArray);
        assertEquals("index1",csonArray.getCommentObject(0).getBeforeComment());
        assertEquals("테\n스\n트",csonArray.getHeadComment());
        assertEquals("index1After",csonArray.getCommentObject(0).getAfterComment());
        assertEquals(1,csonArray.getInteger(0));
        assertEquals(null,csonArray.get(1));
        assertEquals(null,csonArray.get(2));
        assertEquals("이 곳에 주석 가능",csonArray.getCommentObject(2).getBeforeComment());
        assertEquals("3 ",csonArray.get(3));
        assertEquals(3,csonArray.getInteger(3));
        assertEquals("index 3",csonArray.getCommentObject(3).getAfterComment());

        assertEquals("오브젝트 시작", csonArray.getCSONObject(12).getHeadComment());
        assertEquals("알수없는 영역", csonArray.getCSONObject(12).getCommentAfterElement().getBeforeComment());
        assertEquals("오브젝트끝", csonArray.getCSONObject(12).getCommentAfterElement().getAfterComment());

        assertEquals("오브젝트끝",csonArray.getCommentObject(12).getAfterComment());

        assertEquals(Double.NEGATIVE_INFINITY,csonArray.get(18));
        assertEquals(Double.NaN,csonArray.get(19));


        CSONArray idx15Array = csonArray.getCSONArray(15);
        assertEquals("15배열로그",csonArray.getCommentObject(15).getBeforeComment());
        assertEquals("15배열로그",idx15Array.getHeadComment());
        assertEquals("15after",csonArray.getCommentObject(15).getAfterComment());
        assertEquals(null, idx15Array.get(0));
        assertEquals(null, idx15Array.get(1));


        assertTrue(Double.isInfinite(csonArray.getDouble(18)));
        assertTrue(Double.isNaN(csonArray.getDouble(19)));





    }


    @Test
    public void toStringTest1() {
        CSONObject csonObject = new CSONObject("{array:[/*코멘트*/],  key:{/*코멘트*/}/*코멘트*/, /*코멘트*/} ", JSONOptions.json5());
        System.out.println(csonObject.toString());

        assertEquals("코멘트",csonObject.getCSONArray("array").getCommentAfterElement().getBeforeComment());
        assertEquals("코멘트",csonObject.getCommentAfterElement().getBeforeComment());

    }

    @Test
    public void toStringTest() {
        String jsonString =
                "//인덱스\n//야호!!여기가 처음이다." +
                        "\n{ //키1_앞\n" +
                            "키1/*키1_뒤*/: " +
                    "/*값1앞*/값1/*값1뒤*/," +
                    "키2:[/*값2[0]*/1/*2[0]값*/,/*값2[1]*/2/*2[1]값*/,/*값2[2]*/3/*2[2]값*/,/*값2[3]*/4,/*값2[4]*/{}/*[4]값2*/," +
                        "{/*키2.1*/키2.1/*2.1키*/:/*값2.1*/값2.1/*2.1값*/,키2.2 /*\\\n\t\t\t2.2키\n\t\t\ttestㅇㄴㅁㄹㅇㄴㅁㄹㄴㅇㄻㅇㄴㄹ\n\t\t\tsdafasdfadsfdasㅇㄴㅁㄹㄴㅇㄻㄴㅇㄹ\n\\*/:/*값2.2*/{키2.2.1/*2.2.1키*/:값2.2.1/*2.2.1값*/}/*2.2값*/" +
                                        ",/*키2.3*/키2.3/*2.3키*/:/*값2.3*/{/*키2.3.1*/키2.3.1/*2.3.1키*/:값2.3.1/*2.3.1값*/}/*2.3값*/}/*2[5]값*/,[],/*배열 안에 배열*/[]/*배열 안에 배열 \n끝*/,// 배열 마지막 \n]/*값2*/," +
                "}//테일. 어쩌고저쩌고";
        // array  파싱하는 과정에서 빈 공간은 건너뛰는 것 같음...


        CSONObject csonObject = new CSONObject(jsonString, JSONOptions.json5());

       System.out.println(csonObject.toString());

    }

    @Test
    public void testMultilineComment() {
        String json5Str = "{ \n" +
                "/* 코멘트입니다. */\n //222 \n " +
                " key: /* 값 코멘트 */ \"value\"//값 코멘트 뒤\n,key2: \"val/* ok */ue2\",/*array코멘트*/array:[1,2,3,4,Infinity]//array코멘트 값 뒤\n,/*코멘트array2*/array2/*코멘트array2*/:/*코멘트array2b*/[1,2,3,4]/*코멘트array2a*/,/* 오브젝트 */ object " +
                "// 오브젝트 코멘트 \n: /* 오브젝트 값 이전 코멘트 */ { p : 'ok' \n, // 이곳은? \n } // 오브젝트 코멘트 엔드 \n  , // key3comment \n 'key3'" +
                " /*이상한 코멘트*/: // 값 앞 코멘트 \n 'value3' // 값 뒤 코멘트 \n /*123 */,\"LFARRAY\":[\"sdfasdf \\\n123\"]  ,  \n /*123*/ } /* 꼬리 다음 코멘트 */";
        CSONObject.setDefaultStringFormatOption(JSONOptions.json5());
        CSONObject csonObject = new CSONObject(json5Str);
        assertEquals("코멘트입니다.\n222",csonObject.getCommentOfKey("key"));
        assertEquals("array코멘트", csonObject.getCommentOfKey("array"));
        assertEquals("array코멘트 값 뒤",csonObject.getCommentObjectOfValue("array").getAfterComment());

        System.out.println(csonObject.toString());
        // csonObject.getCommentOfKey("key");


    }


    @Test
    public void testKeyComment() throws IOException {


        String json5Str = "{ \n" +
                "/* 코멘트입니다. */\n //222 \n " +
                " key: /* 값 코멘트 */ \"value\"//값 코멘트 뒤\n,key2: \"val/* ok */ue2\",/*array코멘트*/array:[1,2,3,4,Infinity],/*코멘트array2*/array2/*코멘트array2*/:/*코멘트array2b*/[1,2,3,4]/*코멘트array2a*/,/* 오브젝트 */ object " +
                "// 오브젝트 코멘트 \n: /* 오브젝트 값 이전 코멘트 */ { p : 'ok' \n, // 이곳은? \n } // 오브젝트 코멘트 엔드 \n  , // key3comment \n 'key3'" +
                " /*이상한 코멘트*/: // 값 앞 코멘트 \n 'value3' // 값 뒤 코멘트 \n /*123 */,\"LFARRAY\":[\"sdfasdf \\\n123\"]  ,  \n /*123*/ } /* 꼬리 다음 코멘트 */";

        CSONObject.setDefaultStringFormatOption(JSONOptions.json5());
        //System.out.println(json5Str);
        CSONObject origin = new CSONObject(json5Str , JSONOptions.json5().setKeyQuote(""));
        CSONObject  csonObject = new CSONObject(json5Str , JSONOptions.json5());
        assertEquals("코멘트입니다.\n222",csonObject.getCommentObjectOfKey("key").getBeforeComment());
        System.out.println(csonObject);

        csonObject = new CSONObject(csonObject.toString() , JSONOptions.json5());
        System.out.println(csonObject.toString(JSONOptions.json5()));

        assertEquals("코멘트입니다.\n222",csonObject.getCommentObjectOfKey("key").getBeforeComment());
        assertEquals("값 코멘트",csonObject.getCommentOfValue("key"));
        assertEquals("값 코멘트 뒤",csonObject.getCommentAfterValue("key"));



        assertEquals("array코멘트",csonObject.getCommentObjectOfKey("array").getBeforeComment());
        assertEquals(null,csonObject.getCommentOfKey("key2"));
        assertEquals(null,csonObject.getCommentOfValue("key2"));
        assertEquals("val/* ok */ue2",csonObject.getString("key2"));
        assertEquals("key3comment",csonObject.getCommentOfKey("key3"));
        assertEquals("이상한 코멘트",csonObject.getCommentObjectOfKey("key3").getAfterComment());
        assertEquals("이상한 코멘트",csonObject.getCommentObjectOfKey("key3").getAfterComment());
        assertEquals("값 앞 코멘트",csonObject.getCommentOfValue("key3"));
        assertEquals("값 뒤 코멘트\n  123",csonObject.getCommentObjectOfValue("key3").getAfterComment());
        assertEquals("123\n꼬리 다음 코멘트",csonObject.getTailComment());

        CommentObject keyCommentObject = csonObject.getCommentObjectOfKey("object");
        assertEquals("오브젝트", keyCommentObject.getBeforeComment());
        CSONObject subObject =  csonObject.getCSONObject("object");
        assertEquals("ok",subObject.get("p"));

        CommentObject valueCommentObject = csonObject.getCommentObjectOfValue("object");
        assertEquals(csonObject.getCommentObjectOfValue("object").getBeforeComment(),subObject.getHeadComment());
        //assertEquals("이곳은?",subObject.getCommentAfterElement().getBeforeComment());
        assertEquals("오브젝트 코멘트 엔드",subObject.getCommentAfterElement().getAfterComment());

        assertEquals("오브젝트 코멘트", keyCommentObject.getAfterComment());


        assertEquals(origin.toString(JSONOptions.json5()), new CSONObject(csonObject.toString(JSONOptions.json5()), JSONOptions.json5()).toString(JSONOptions.json5()));




        String obj = origin.toString(JSONOptions.json());
        System.out.println(obj);
        long start = System.currentTimeMillis();
        /*start = System.currentTimeMillis();
        CSONObject csonObjects = null;// = new CSONObject(obj);
        for(int i = 0; i < 1000000; ++i) {
            NoSynchronizedStringReader stringReader = new NoSynchronizedStringReader(obj);
            csonObjects = (CSONObject) PureJSONParser.parsePureJSON(stringReader);
            stringReader.close();
            //CSONObject csonObjects = new CSONObject(obj);
            //csonObjects.toString();
            String aaa = "";
            aaa.trim();
        }
        System.out.println("cson : " + csonObjects.toString());

        System.out.println("cson : " + (System.currentTimeMillis() - start));
*/





    }


    @Test
    public void commentTest() {
        CSONObject csonObject = new CSONObject(JSONOptions.json5());
        csonObject.put("key","value");
        csonObject.setCommentForKey("key","키 앞 코멘트1");
        csonObject.setCommentAfterKey("key","키 뒤 코멘트1");

        csonObject.setCommentForValue("key","값 앞 코멘트1");
        csonObject.setCommentAfterValue("key","값 뒤 코멘트1");

        CSONArray csonArray = new CSONArray(JSONOptions.json5());
        csonObject.put("arrayKey",csonArray);
        CSONObject objinObj = new CSONObject(JSONOptions.json5());
        objinObj.put("key","value");
        objinObj.setCommentForKey("key","키 앞 코멘트");
        objinObj.setCommentAfterKey("key","키 뒤 코멘트");
        objinObj.setCommentForValue("key","값 앞 코멘트");
        objinObj.setCommentAfterValue("key","값 뒤 코멘트");
        assertEquals(objinObj.toString(), new CSONObject(objinObj.toString(), JSONOptions.json5()).toString());

        csonArray.setCommentForValue(0,"배열값 앞 코멘트");
        csonArray.setCommentAfterValue(0,"배열값 뒤 코멘트");
        csonArray.setCommentForValue(2,"배열값 앞 코멘트2");
        csonArray.setCommentAfterValue(2,"배열값 뒤 코멘트2");

        csonArray.setCommentForValue(3,"배열값 앞 코멘트3");
        csonArray.setCommentAfterValue(3,"배열값 뒤 코멘트3");

        csonArray.put(objinObj);
        csonArray.put(objinObj);
        csonArray.put(123);
        csonArray.put(new CSONArray().put(1).put(2).put(3).put(4).put(5).put(6).put(7).put(8).put(9).put(10).put(Double.POSITIVE_INFINITY).put(Double.NaN).setTailComment("sdafasdfdasf"));
        objinObj.setTailComment("오브젝트 뒤 코멘트");
        objinObj.setHeadComment("오브젝트 앞 코멘트");


        csonArray.setHeadComment("배열 앞 코멘트");
        csonArray.setTailComment("배열 뒤 코멘트");

        assertEquals(csonArray.toString(), new CSONArray(csonArray.toString(), JSONOptions.json5()).toString());




        csonObject.setCommentForKey("arrayKey","키 앞 코멘트");
        csonObject.setCommentAfterKey("arrayKey","키 뒤 코멘트");
        csonObject.setCommentForValue("arrayKey","값 앞 코멘트");
        csonObject.setCommentAfterValue("arrayKey","값 뒤 코멘트");

        csonObject.setCommentForKey("arrayKey","키 앞 코멘트\nok");
        csonObject.setCommentAfterKey("arrayKey","키 뒤 코멘트");
        csonObject.setCommentForValue("arrayKey","값 앞 코멘트");
        csonObject.setCommentAfterValue("arrayKey","값 뒤 코멘트");
        System.out.println(csonObject.toString());

       CSONObject result = new CSONObject(csonObject.toString(), JSONOptions.json5());
        System.out.println(result.toString());

        assertEquals(csonObject.toString(), result.toString());

    }




}
