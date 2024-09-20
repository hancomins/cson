package com.hancomins.cson;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentParsingTest {

    @Test
    @DisplayName("키에 대한 코멘트 파싱 테스트 (성공)")
    public void parseSimpleKeyComment() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : value }";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));
        System.out.println(csonObject.toString());
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트 (성공)")
    public void parseSimpleValueComment() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  value /*   코멘트 값 값 값 */ // zzzz\n }";
        CSONObject csonObject = new CSONObject(json);
        assertEquals("ke/y", csonObject.keySet().iterator().next());
        assertEquals("comment\nthis comment", csonObject.getCommentForKey("ke/y"));
        assertEquals("23", csonObject.getCommentAfterKey("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", csonObject.getCommentForValue("ke/y"));
        assertEquals("코멘트 값 값 값", csonObject.getCommentAfterValue("ke/y"));




    }
}
