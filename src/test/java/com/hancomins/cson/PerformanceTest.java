package com.hancomins.cson;


import com.hancomins.cson.options.JsonParsingOptions;
import org.json.JSONObject;

public class PerformanceTest {

    public static void main(String[] args) {

        /*

        String json5 = "{\n" +
                "  // comments\n" +
                "  unquoted: 'and you can quote me on that',\n" +
                "  single/Quotes: \"I can use 'double quotes' here\",\n" +
                "  lineBreaks: \"Look, Mom! \\\n" +
                "No \\\\n's!\",\n" +
                "  hexadecimal: 0xdecaf,\n" +
                "  leadingDecimalPoint: .8675309, andTrailing: 8675309.,\n" +
                "  positiveSign: +1,\n" +
                "  if: 'in objects', andIn: ['arrays',],\n" +
                "  \"backwardsCompatible\": \"with JSON\",\n" +
                "}";

        CSONObject json5Object = new CSONObject(json5);
        System.out.println(json5Object.toString());
*/
        //if(1 < 2) return;


        String json = "{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\n\"333333L\":3333e+33,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-1212312321783912389123871289371231231231238,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\",\"array\":[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\",[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\"],{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"},\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"],\"array2\":[[1,2],[3,4],[],{}],\"array3\":[\"\",[3,4],[],{}],\"array4\":[{},{},[],{\"inArray\":[]}],\"key111\":{\"1\":{}},\"key112\":[{}]}";


        for(int count = 0; count < 10; ++count) {
            long start = System.currentTimeMillis();
            //JSONObject jsonObject = new JSONObject(json);
            CSONObject csonObject = new CSONObject(json);
            for (int i = 0; i < 100000; i++) {

                csonObject.toString();

                //jsonObject.toString();


            }
            long end = System.currentTimeMillis();
            System.out.println("Time: " + (end - start) + "ms");
        }





    }
}
