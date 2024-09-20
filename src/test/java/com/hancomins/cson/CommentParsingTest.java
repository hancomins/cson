package com.hancomins.cson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommentParsingTest {

    @Test
    @DisplayName("quoted 키에 대한 코멘트 파싱 테스트 (성공)")
    public void parseSimpleUnquotedKeyComment() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : value }";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));
        System.out.println(csonObject.toString());
    }

    @Test
    @DisplayName("quoted 키에 대한 코멘트 파싱 테스트 (성공)")
    public void parseSimpleQuotedKeyComment() {
        String json = "{/*comment*/ //this comment\n 'ke/y'/*23*/ : value }";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));
        System.out.println(csonObject.toString());
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.1 (성공)")
    public void parseSimpleValueComment1() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  value /*   코멘트 값 값 값 */ // zzzz\n}";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));

        assertEquals("value", csonObject.get("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", csonObject.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonObject.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.2 (성공)")
    public void parseSimpleValueComment2() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  \"value\" /*   코멘트 값 값 값 */ // zzzz\n}";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));
        assertEquals("value", csonObject.get("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", csonObject.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonObject.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.3 (성공)")
    public void parseSimpleValueComment3() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  {/*이건 파싱하면 안됨*/} /*   코멘트 값 값 값 */ // zzzz\n}";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));

        assertEquals("{}", csonObject.getCSONObject("ke/y").toString(JSONOptions.json()));
        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", csonObject.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonObject.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.4 (성공)")
    public void parseSimpleValueComment4() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  [/*이건 파싱하면 안됨*/] /*   코멘트 값 값 값 */ // zzzz\n}";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));
        assertEquals("[]", csonObject.getCSONArray("ke/y").toString(JSONOptions.json()));
        System.out.println(csonObject.getCSONArray("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", csonObject.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonObject.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("Number 값에대한 코멘트 파싱 테스트.4 (성공)")
    public void parseNumberValueComment4() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  123123 /*   코멘트 값 값 값 */ // zzzz\n,}";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));
        assertEquals(123123, csonObject.getInt("ke/y"));
        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", csonObject.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonObject.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("CSONArray 내의 코멘트 파싱 테스트.1")
    public void parseCommentInArray1() {
        String json = "[/*comment*/ //this comment\n //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  123123 /*   코멘트 값 값 값 */ // zzzz\n,]";
        CSONArray csonArray = new CSONArray(json);
        assertEquals("comment\nthis comment\n코멘트1 \n 코멘트2\n\n코멘트3 ", csonArray.getCommentForValue(0));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonArray.getCommentAfterValue(0));
        assertEquals(123123, csonArray.getInt(0));

    }

    @Test
    @DisplayName("CSONArray 내의 코멘트 파싱 테스트.2")
    public void parseCommentInArray2() {
        String json = "[/*comment*/ //this comment\n //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  [] /*   코멘트 값 값 값 */ // zzzz\n,]";
        CSONArray csonArray = new CSONArray(json);
        assertEquals("comment\nthis comment\n코멘트1 \n 코멘트2\n\n코멘트3 ", csonArray.getCommentForValue(0));
        assertEquals("   코멘트 값 값 값 \n zzzz", csonArray.getCommentAfterValue(0));
        assertTrue(csonArray.get(0) instanceof CSONArray);

    }
}
