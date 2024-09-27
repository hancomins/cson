package com.hancomins.cson.internal;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IteratorTest (성공)")
public class IteratorTest {
    @Test
    public void csonArrayIteratorTest() {
        ArrayList<Object> list = new ArrayList<>();
        // make random value in list
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 1000; i++) {
            // type 0 : int, 1 : float, 2 : string, 3 : boolean, 4: CSONObject, 5: CSONArray
            int type = random.nextInt(7);
            switch (type) {
                case 0:
                    list.add(random.nextInt());
                    break;
                case 1:
                    list.add(random.nextFloat());
                    break;
                case 2:
                    list.add(random.nextInt() + "");
                    break;
                case 3:
                    list.add(random.nextBoolean());
                    break;
                case 4:
                    list.add(new CSONObject());
                    break;
                case 5:
                    list.add(new CSONArray());
                    break;
                case 6:
                    list.add(null);
                    break;
                default:
                    break;
            }
        }

        CSONArray csonArray = new CSONArray(list);
        CSONObject csonObject = new CSONObject();
        int no = 0;
        for(Object obj : csonArray) {
            csonObject.put(++no + "", obj);
        }

        Iterator<Object> iteratorArray = csonArray.iterator();
        while(iteratorArray.hasNext()) {
            Object obj = iteratorArray.next();
            System.out.println(obj);
            assertTrue(csonObject.containsValue(obj));

        }

        Iterator<Object> iteratorMap = csonObject.iterator();
        while(iteratorMap.hasNext()) {
            Object obj = iteratorMap.next();
            assertTrue(csonArray.contains(obj));
            iteratorMap.remove();
        }


        assertEquals(0, csonObject.size());

        byte[] bytes = new byte[random.nextInt(1000) + 100];
        random.nextBytes(bytes);
        csonArray.put(bytes);
        csonObject.put("bytes", bytes);

        String stringBytes = csonObject.getString("bytes");
        assertFalse(csonObject.containsValue(stringBytes));
        assertFalse(csonArray.contains(stringBytes));


        assertTrue(csonObject.containsValueNoStrict(stringBytes));
        assertTrue(csonArray.containsNoStrict(stringBytes));





    }

}
