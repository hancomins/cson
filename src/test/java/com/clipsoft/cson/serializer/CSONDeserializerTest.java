package com.clipsoft.cson.serializer;

import com.clipsoft.cson.JSONOptions;
import com.clipsoft.cson.CSONObject;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class CSONDeserializerTest {


    @CSON
    public static class TestListInListInListClass {
        @CSONValue
        public LinkedList<ArrayDeque<ArrayList<String>>> list = new LinkedList<>();
    }
    @Test
    public void listInListInListTest() {
        TestListInListInListClass testObj = new TestListInListInListClass();
        testObj.list.add(null);
        ArrayDeque<ArrayList<String>> list1 = new ArrayDeque<>();
        ArrayList<String> list2 = new ArrayList<>();
        list2.add(null);
        list2.add("test2");
        list2.add("test3");
        list1.add(list2);
        testObj.list.add(list1);

        ArrayDeque<ArrayList<String>> list1_1 = new ArrayDeque<>();
        ArrayList<String> list2_1 = new ArrayList<>();
        list2_1.add("test1");
        list2_1.add("test2");
        list2_1.add(null);
        list2_1.add("test3");
        list1_1.add(list2_1);
        testObj.list.add(list1_1);

        ArrayDeque<ArrayList<String>> list1_2 = new ArrayDeque<>();
        ArrayList<String> list2_2 = new ArrayList<>();
        list2_2.add("test1");
        list2_2.add("test2");
        list2_2.add("test3");
        list1_2.add(list2_2);
        testObj.list.add(list1_2);

        testObj.list.add(null);
        testObj.list.add(null);




        CSONObject cson = CSONSerializer.toCSONObject(testObj);
        TestListInListInListClass resultObj = CSONSerializer.fromCSONObject(cson, new TestListInListInListClass());
        assertEquals(testObj.list.size(), resultObj.list.size());

        for(int i = 0; i < testObj.list.size(); ++i) {
            ArrayDeque<ArrayList<String>> list1_3 = testObj.list.get(i);
            ArrayDeque<ArrayList<String>> list1_4 = resultObj.list.get(i);
            if(list1_3 == null) {
                assertNull(list1_4);
                continue;
            }

            assertEquals(list1_3.size(), list1_4.size());
            Iterator<ArrayList<String>> iterator1_3 = list1_3.iterator();
            Iterator<ArrayList<String>> iterator1_4 = list1_4.iterator();
            while(iterator1_3.hasNext()) {
                ArrayList<String> list2_3 = iterator1_3.next();
                ArrayList<String> list2_4 = iterator1_4.next();
                assertEquals(list2_3.size(), list2_4.size());
                for(int k = 0; k < list2_3.size(); ++k) {
                    assertEquals(list2_3.get(k), list2_4.get(k));
                }
            }
        }


    }

    @CSON
    public static class TestClass {
        @CSONValue
        public String name;
        @CSONValue
        public int age;
        @CSONValue
        public boolean isMale;
        @CSONValue
        public String nullValue;

        @CSONValue
        public ArrayList<String> childrenNames = new ArrayList<>();
        @CSONValue
        public ArrayList<TreeSet<Integer>> tourDates  = new ArrayList<>();


        @CSONValue
        public LinkedList<ArrayDeque<ArrayList<String>>> family  = new LinkedList<>();
    }

    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        TestClass testClass = new TestClass();
        testClass.name = "SnowOrca";
        testClass.age = 18;
        testClass.isMale = true;
        testClass.childrenNames.add("김철수");
        testClass.childrenNames.add("김영희");
        testClass.childrenNames.add("김영수");

        TreeSet list = new TreeSet<>();
        list.add(20180111);
        list.add(20180112);
        list.add(20180113);
        testClass.tourDates.add(list);

        list = new TreeSet<>();
        list.add(20190301);
        list.add(20190302);
        testClass.tourDates.add(list);


        ThreadLocalRandom random = ThreadLocalRandom.current();
        // 무작위 문자열 생성 및 리스트에 추가
        for (int i = 0; i < random.nextInt(3, 4); i++) { // 예를 들어, 5개의 무작위 문자열을 생성하여 추가합니다.
            ArrayDeque<ArrayList<String>> set = new ArrayDeque<>();
            for (int j = 0; j < random.nextInt(3, 4); j++) { // 각 HashSet에 3개의 ArrayDeque를 추가합니다.
                ArrayList<String> randomStrings = new ArrayList<>();
                for (int k = 0; k < random.nextInt(3, 4); k++) { // 각 ArrayDeque에 4개의 무작위 문자열을 추가합니다.
                    String randomString = UUID.randomUUID().toString();
                    if(random.nextBoolean())
                        randomStrings.add(null);
                    else
                        randomStrings.add(randomString);
                }
                set.add(randomStrings);
            }
            testClass.family.add(set);
        }




        CSONObject cson = CSONSerializer.toCSONObject(testClass);
        TestClass newClass = (TestClass) CSONSerializer.fromCSONObject(cson, new TestClass());
        assertEquals(testClass.name, newClass.name);
        assertEquals(testClass.age, newClass.age);
        assertEquals(testClass.isMale, newClass.isMale);
        assertNull(cson.get("nullValue"));
        assertEquals(testClass.nullValue, newClass.nullValue);

        assertEquals(testClass.childrenNames.size(), newClass.childrenNames.size());
        for(int i = 0; i < testClass.childrenNames.size(); i++) {
            assertEquals(testClass.childrenNames.get(i), newClass.childrenNames.get(i));
        }


        //System.out.println(cson.toString());

        assertEquals(testClass.tourDates.size(), newClass.tourDates.size());
        for (int i = 0; i < testClass.tourDates.size(); i++) {
            System.out.println(newClass.tourDates.get(i).getClass());
            assertTrue(newClass.tourDates.get(i) instanceof TreeSet);
            ArrayList<Integer> list1 = new ArrayList<>(testClass.tourDates.get(i));
            ArrayList<Integer> list2 = new ArrayList<>(newClass.tourDates.get(i));
            assertEquals(list1.size(), list2.size());
            for(int j = 0; j < list1.size(); j++) {
                assertEquals(list1.get(j), list2.get(j));
            }
        }

        assertEquals(testClass.family.size(), newClass.family.size());
        Iterator<ArrayDeque<ArrayList<String>>> iterator1 = testClass.family.iterator();
        Iterator<ArrayDeque<ArrayList<String>>> iterator2 = newClass.family.iterator();
        while(iterator1.hasNext()) {
            ArrayDeque<ArrayList<String>> set1 = iterator1.next();
            ArrayDeque<ArrayList<String>> set2 = iterator2.next();
            assertEquals(set1.size(), set2.size());
            Iterator<ArrayList<String>> iterator3 = set1.iterator();
            Iterator<ArrayList<String>> iterator4 = set2.iterator();
            while(iterator3.hasNext()) {
                ArrayList<String> list1 = iterator3.next();
                ArrayList<String> list2 = iterator4.next();
                assertEquals(list1.size(), list2.size());
                for(int i = 0; i < list1.size(); i++) {
                    assertEquals(list1.get(i), list2.get(i));
                }
            }
        }
    }


    @CSON
    public static class User {
        String name;
        int age;
        boolean isMale;


    }

    @CSON
    public static class Address {
        @CSONValue
        String city;
        @CSONValue
        String street;
        @CSONValue
        int zipcode;

        @CSONValue
        HouseType houseType;

        @CSONValue
        TransportationFacilities transportationFacilities;


    }


    @CSON
    public static class TransportationFacilities  {
        @CSONValue
        boolean subway;

        @CSONValue
        boolean bus;


    }


    @CSON
    public static class HouseType {
        @CSONValue
        String type;

        @CSONValue
        float buildingHeight;
    }

    @CSON
    public static class HouseTypeEx{
        @CSONValue
        int totalFloor;

        @CSONValue
        String buildingName;

        @CSONValue
        String tk = null;

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof HouseTypeEx) {
                HouseTypeEx houseTypeEx = (HouseTypeEx) obj;
                return totalFloor == houseTypeEx.totalFloor &&
                        ((buildingName == null || houseTypeEx.buildingName == null) || buildingName.equals(houseTypeEx.buildingName)) &&
                        ((tk == null || houseTypeEx.tk == null) || tk.equals(houseTypeEx.tk));
            }
            return false;
        }
    }

    @CSON
    public static class Home  {

        @CSONValue
        Address address;
        @CSONValue("address.phone")
        String phoneNumber;

        @CSONValue("address.houseType")
        HouseTypeEx houseTypeEx;

        @CSONValue("address.houseType.ex")
        HouseTypeEx houseTypeEx2;

        @CSONValue("address.houseTypeNull")
        HouseTypeEx houseTypeExNull;

    }

    @Test
    public void objectInObjectTest() {
        Home home = new Home();
        home.address = new Address();
        home.address.city = "Seoul";
        home.address.street = "Gangnam";
        home.address.zipcode = 12345;
        home.phoneNumber = "010-1234-5678";
        home.address.houseType = new HouseType();
        home.address.houseType.type = "office";
        home.address.houseType.buildingHeight = 72.5f;
        home.houseTypeEx = new HouseTypeEx();
        home.houseTypeEx.totalFloor = 19;
        home.houseTypeEx.buildingName = "Dreamplus Tower";
        home.houseTypeEx2 = new HouseTypeEx();

        CSONObject cson = CSONSerializer.toCSONObject(home);
        System.out.println(cson.toString(JSONOptions.json5()));
        Home resultHome = CSONSerializer.fromCSONObject(cson, new Home());
        assertEquals(home.address.city, resultHome.address.city);
        assertEquals(home.address.street, resultHome.address.street);
        assertEquals(home.address.zipcode, resultHome.address.zipcode);
        assertEquals(home.phoneNumber, resultHome.phoneNumber);
        assertEquals(home.address.houseType.type, resultHome.address.houseType.type);
        assertEquals(home.address.houseType.buildingHeight, resultHome.address.houseType.buildingHeight, 0.0001f);
        assertEquals(home.houseTypeEx.totalFloor, resultHome.houseTypeEx.totalFloor);
        assertEquals(home.houseTypeEx.buildingName, resultHome.houseTypeEx.buildingName);
        assertNull(resultHome.houseTypeEx.tk);
        assertNull(resultHome.houseTypeExNull);
        assertEquals(home.houseTypeEx2, resultHome.houseTypeEx2);


        System.out.println(CSONSerializer.toCSONObject(resultHome).toString(JSONOptions.json5()));

    }




    @CSON
    public static class SimpleObjectInArray {
        @CSONValue
        ArrayList<HouseType> transportationFacilities = new ArrayList<>();

        @CSONValue
        HashSet<Home> homesNull = null;

        @CSONValue
        HashSet<ArrayDeque<Home>> homes = new HashSet<>();



    }

    @Test
    public void objectInCollectionTest() {
        SimpleObjectInArray simpleObjectInArray = new SimpleObjectInArray();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for(int i = 0; i < random.nextInt(3, 10); i++) {
            HouseType houseType = new HouseType();
            houseType.type = UUID.randomUUID().toString();
            houseType.buildingHeight = random.nextFloat();
            if(random.nextInt(0, 3) == 2) {
                simpleObjectInArray.transportationFacilities.add(null);
            } else {
                simpleObjectInArray.transportationFacilities.add(houseType);
            }
        }
        CSONObject cson = CSONSerializer.toCSONObject(simpleObjectInArray);
        System.out.println(cson.toString(JSONOptions.json5()));
        System.out.println(CSONSerializer.toCSONObject(CSONSerializer.fromCSONObject(cson, new SimpleObjectInArray())).toString(JSONOptions.json5()));


        assertEquals(cson.toString(JSONOptions.json5()), CSONSerializer.toCSONObject(CSONSerializer.fromCSONObject(cson, new SimpleObjectInArray())).toString(JSONOptions.json5()));


    }


    @CSON
    public static class ArrayItemKey {
        @CSONValue("key.list[10][10].ok")
        public String key;

        @CSONValue("key.list[10][10].nullValue")
        public String nullValue;
    }

    @Test
    public void arrayItemKeyTest() {
        ArrayItemKey arrayItemKey = new ArrayItemKey();
        arrayItemKey.key = "test";

        CSONObject cson = CSONSerializer.toCSONObject(arrayItemKey);
        System.out.println(cson);

        ArrayItemKey result =  CSONSerializer.fromCSONObject(cson, new ArrayItemKey());
        assertEquals(arrayItemKey.key, result.key);
        assertNull(result.nullValue);
    }

    @Test
    public void mapObjectDeserialize() {
        CSONObject csonObject = new CSONObject();
        ArrayItemKey arrayItemKey = new ArrayItemKey();
        arrayItemKey.key = "test";
        csonObject.put("key", arrayItemKey);

    }




}
