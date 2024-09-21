package com.hancomins.cson;


public class PerformanceTest {

    public static void main(String[] args) {


        String json = "{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":3333e+33,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-1212312321783912389123871289371231231231238,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\",\"array\":[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\",[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\"],{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"},\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"],\"array2\":[[1,2],[3,4],[],{}],\"array3\":[\"\",[3,4],[],{}],\"array4\":[{},{},[],{\"inArray\":[]}],\"key111\":{\"1\":{}},\"key112\":[{}]}";


        for(int count = 0; count < 10; ++count) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                CSONObject csonObject = new CSONObject(json, JSONParsingOptions.json5());
                //JSONObject jsonObject = new JSONObject(json);


            }
            long end = System.currentTimeMillis();
            System.out.println("Time: " + (end - start) + "ms");
        }





    }
}
