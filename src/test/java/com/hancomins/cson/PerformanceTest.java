package com.hancomins.cson;



import com.hancomins.cson.options.ParsingOptions;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PerformanceTest {

    public static void main(String[] args) {



        String simpleJSON = "{key: [1234]} ";
        CSONObject csonObject1 = new CSONObject(simpleJSON);









        //String json = "{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\n\"333333L\":3333e+33,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-1212312321783912389123871289371231231231238,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\",\"array\":[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\",[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\"],{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"},\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"],\"array2\":[[1,2],[3,4],[],{}],\"array3\":[\"\",[3,4],[],{}],\"array4\":[{},{},[],{\"inArray\":[]}],\"key111\":{\"1\":{}},\"key112\":[{}]}";

        String json = "";
        //InputStream inputStream = PerformanceTest.class.getClassLoader().getResourceAsStream("large-file.json");
        InputStream inputStream = PerformanceTest.class.getClassLoader().getResourceAsStream("sample1.json");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            json = byteArrayOutputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }


        long startCreate = System.currentTimeMillis();

        for (int i = 0; i < 11189; i++) {


           //JSONObject jsonObject = new JSONObject();
            //CSONObject csonObject = new CSONObject();

        }
        System.out.println("Time Create: " + (System.currentTimeMillis() - startCreate) + "ms");


        for(int count = 0; count < 100; ++count) {
            long start = System.currentTimeMillis();
            //

            for (int i = 0; i < 30000; i++) {

                //JSONArray jsonArray = new JSONArray(json);
                //CSONArray csonArray = new CSONArray(json);


                JSONObject jsonObject = new JSONObject(json);

                //CSONObject csonObject = new CSONObject(json, ParsingOptions.json5().setAllowControlCharacters(true));
                /*if(i == 0 && count == 0) {
                    System.out.println(csonObject.toString());
                }*/

                /*if(i == 0 && count == 0) {
                    byte[] bufferA = csonObject.toString().getBytes(StandardCharsets.UTF_8);
                    // bufferA 를 MegaByte 단위로 출력
                    System.out.println("bufferA.length: " + bufferA.length / 1024 / 1024 + "MB");
                }*/



            }
            long end = System.currentTimeMillis();
            System.out.println("Time: " + (end - start) + "ms");
        }





    }
}
