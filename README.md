# cson introduction
  - JAVA 1.8 or higher environment is supported. 
  - API usage is similar to JSON-JAVA (https://github.com/stleary/JSON-java) 
  - Data structures that can be represented in JSON can be serialized(or deserialized) in binary format. The binary structure can have a smaller data size than the json string type, and has a performance advantage in parsing.
  - Supports [JSON5](https://json5.org/) format. Comments are parsed alongside the data..
  - Provides object serialization and deserialization using Java annotations. In addition to this, Collection and Map can also be used for JSON type serialization and deserialization.
  - Provides a way to access specific values in a CSON object. This is similar to XPath for XML.


## Add dependency to your project (gradle)

```groovy
dependencies {
    implementation group: 'io.github.clipsoft-rnd', name: 'cson', version: '0.9.26'
}
```

## Basic usage (JSON)
* Pure JSON is the default. And it performs the best. easiest to use. This usage method is the same as [JAVA-JSON](https://github.com/stleary/JSON-java).
   ```java
    CSONObject obj = new CSONObject();
    // You can change the format options. 
    // default is StringFormatOption.jsonPure()
    // CSONObject obj = new CSONObject(StringFormatOption.jsonPure());
    
    obj.put("name", "John");
    obj.put("age", 25);
    CSONArray friends = new CSONArray();
    friends.put("Nancy");
    friends.put("Mary");
    friends.put("Tom", "Jerry");
    obj.put("friends", friends);
    String json = obj.toString();
    // {"name":"John","age":25,"friends":["Nancy","Mary","Tom","Jerry"]}
  
    // Parsing
    CSONObject parsed = new CSONObject(json);
    String name = parsed.getString("name");
    // If "age" does not exist or cannot be converted to String, it returns "unknown".
    String age = parsed.optString("age", "unknown");
    ```
* If you want to print the json string pretty, do as follows.
   ```java
    JSONOptions jsonOptions = StringFormatOption.json();
    jsonOptions.setPretty(true);
    // Only arrays can be displayed on one line.
    jsonOptions.setUnprettyArray(true);
    String prettyJSONString = csonObjectPure.toString(jsonOptions);
    //{
    //  "name": "John",
    //  "age": 25,
    //  "friends": [ "Nancy", "Mary", "Tom", "Jerry" ]
    //}
    ```
   
## JSON5 usage
  * JSON5 is a superset of JSON that allows comments, trailing commas, and more. key names can be unquoted if they’re valid identifiers. Single and multi-line strings are allowed, as well as escaped newlines, tabs, and Unicode characters in strings and names. [JSON5 Document](https://json5.org/) 
    ```java
    CSONObject obj = new CSONObject(StringFormatOption.json5());
  
    // You can change the default options. (It will be applied to all CSONObject and CONSArray)
    // CSONObject.setDefaultJSONOptions(StringFormatOption.json5());
    // Even if you change the default options, you can specify the options when creating the object.
  
    obj.put("name", "John");
    obj.put("age", 25);
    CSONArray friends = new CSONArray();
    friends.put("Nancy");
    friends.put("Mary");
    friends.put("Tom", "Jerry");
    obj.put("friends", friends);
  
    // You can add comments before and after the key, or before and after the value.
    obj.setCommentForKey("friends", "Lists only people's names.");
    obj.setCommentAfterValue("friends", "A total of 4 friends");
  
    obj.setCommentThis("This is a comment for this object.");
    obj.setCommentAfterThis("This is a comment after this object.");
  
    String yourInfo = obj.toString();
    System.out.println(yourInfo);
    //  //This is a comment for this object.
    //  {
    //      name:'John',
    //      age:25,
    //      //Lists only people's names.
    //      friends:['Nancy','Mary','Tom','Jerry']/* A total of 4 friends */
    //  }
    //  //This is a comment after this object.
    ```       
## Binary conversion
  * The binary format is a format that can be converted to a byte array. It is a format that can be used for object serialization and deserialization.
    ```java
    CSONObject obj = new CSONObject();
    obj.put("name", "John");
    obj.put("age", 25);
    //...
    byte[] bytes = obj.toBytes();
    CSONObject parsed = new CSONObject(bytes, 0, bytes.length);
    ```
## CSON Path
 * CSONPath is a way to access specific values in a CSON object. This is similar to XPath for XML.
   ```java
    String json5 = "{user: { name: 'John',  age: 25,  friends: [ 'Nancy', 'Mary', 'Tom', 'Jerry' ], addr: { city: 'seoul', zipCode: '06164'  } }}";
    CSONObject user = new CSONObject(json5, JSONOptions.json5());

    String firstFriend = user.optString("$.user.friends[0]");
    String city = user.optString("$.user.addr.city");

    // other options
    // user.setAllowJsonPathKey(false)
    // String firstFriend = user.getCsonPath().optString("user.friends[0]");
    // String city = user.getCsonPath().optString("user.addr.city");

    System.out.println("firstFriend: "  + firstFriend);
    System.out.println("city: "  + city);
    // firstFriend: Nancy
    // city: seoul

    user.put("$.user.friends[4]", "Suji");
    user.put("$.user.addr.city", "Incheon");

    //user.getCsonPath().put("user.friends[4]", "Suji");
    //user.getCsonPath().put("user.addr.city", "Incheon");
    
    System.out.println(user);
    // {"user":{"name":"John","age":25,"friends":["Nancy","Mary","Tom","Jerry","Suji"],"addr":{"city":"Incheon","zipCode":"06164"}}}
   ```
## Object serialization/deserialization
Convert objects to JSON without much effort. The type of object being serialized is cached as a schema upon first execution. Therefore, the initial execution is slow, but thereafter it is fast.
   * **The types that can be serialized and deserialized into JSON are as follows.**
     * Primitive types (int, long, float, double, boolean, char, byte, short)
     * Boxed types (Integer, Long, Float, Double, Boolean, Character, Byte, Short)
     * String
     * Enum
     * CSONObject
     * CSONArray
     * Class, Interface with @CSON annotation
     * byte[]: to Base64 String
     * Collection<?> (List, Set, Queue, Deque, etc.)
       * A Collection can be used as an element within a Collection.
       * A class annotated with @CSON can be used as an element within a Collection.
       * A <Generic> type can be used as an element within a Collection.
       * Other basic types are available.
       * However, a Map cannot be used as an element within a Collection.
     * Map<String, ?> (HashMap, TreeMap, LinkedHashMap, etc.)
       * The key must always be of the String type.
       * Map and Collection types cannot be used as Values.
       * A class annotated with @CSON can be used as a Value.
       * A <Generic> type can be used as a Value.
       * Other basic types are available.
     * Annotation list
       * @CSON: Indicates that the class can be serialized and deserialized into JSON.
       * @CSONValue: Indicates that the field can be serialized and deserialized into JSON.
       
       * @CSONValueGetter
         * You can use this annotation to retrieve the value corresponding to a key through a method. 
         * The method must have a return value and should not have any arguments.
         * If you want to use a method with a different name, you can specify the key value as an argument.
         * The argument type of @CSONValueSetter method and the return type of @CSONValueGette r must always match!
     
       * @CSONValueSetter
         * You can use this annotation to set the value corresponding to a key through a method. 
         * The method must have one argument. 
         * If you want to use a method with a different name, you can specify the key value as an argument.
         * The argument type of @CSONValueSetter method and the return type of @CSONValueGette r must always match!
     
     * @ObtainTypeValue
       * During deserialization, it is possible to obtain the actual value of a generic type or interface type.
         * It can have two arguments of type JSONObject.
           * The first argument is the json object for the field. If the type is not an object type, a COSNObject object of the form {"$value": value} is taken as an argument.
           * The second argument is an object of CSONObject representing the entire JSONObject.
           * The return value is the actual value of the generic type or interface type.
       * Example
         ```java
           @CSON
           public static class Message<K,V> {
            @CSONValue
            private K key;

            private List<V> list;

            @CSONValueSetter
            public void setData(V data) {
                this.value = data;
            }
            @ObtainTypeValue(fieldNames = {"value"}, deserializeAfter = false)
            public K obtainKeyValue(CSONObject object, CSONObject root) {
                return (K) object.optString("$value");
            }

            @ObtainTypeValue(setterMethodNames = {"setData"}, deserializeAfter = true)
            public V obtainValue(CSONObject object, CSONObject root) {
                return (V) new ValueObject();
            }
          }
      
           CSONObject rawObject = ...
           Message<String, ValueObject> message = CSONObject.toObject(rawObject,Message.class);
         ```
  * **How can serialization and deserialization be performed between Object and JSON types?**
     ```java
     @CSON
     public static class Addr {
         @CSONValue
         private String city;
         @CSONValue
         private String zipCode;
     }
    
     @CSON
     public static class User {
         @CSONValue
         private String name;
         @CSONValue
         private int age;
         @CSONValue
         private List<String> friends;
         @CSONValue
         private Addr addr;
    
         private User() {}
         public User(String name, int age, String... friends) {
             this.name = name;
             this.age = age;
             this.friends = Arrays.asList(friends);
         }
    
         public void setAddr(String city, String zipCode) {
             this.addr = new Addr();
             this.addr.city = city;
             this.addr.zipCode = zipCode;
         }
     }
    
     @CSON(comment = "Users", commentAfter = "Users end.")
     public static class Users {
         @CSONValue(key = "users", comment = "key: user id, value: user object")
         private HashMap<String, User> idUserMap = new HashMap<>();
     }
     ```
   * Serialization
     ```java
     Users users = new Users();
     User user1 = new User("MinJun", 28, "YoungSeok", "JiHye", "JiHyeon", "MinSu");
     user1.setAddr("Seoul", "04528");
     User user2 = new User("JiHye", 27, "JiHyeon","Yeongseok","Minseo");
     user2.setAddr("Cheonan", "31232");
     users.idUserMap.put("qwrd", user1);
     users.idUserMap.put("ffff", user2);

     CSONObject csonObject = CSONObject.fromObject(users, StringFormatOption.json5());
     //CSONObject csonObject = CSONObject.fromObject(users);
     //csonObject.setStringFormatOption(StringFormatOption.json5());
     System.out.println(csonObject);
     // Output
     /*
          //Users
          {
              //key: user id, value: user object
              users:{
               qwrd:{
                   name:'MinJun',
                   age:28,
                   friends:['YoungSeok','JiHye','JiHyeon','MinSu'],
                   addr:{
                       city:'Seoul',
                       zipCode:'04528'
                   }
               },
               ffff:{
                   name:'JiHye',
                   age:27,
                   friends:['JiHyeon','Yeongseok','Minseo'],
                   addr:{
                       city:'Cheonan',
                       zipCode:'31232'
                   }
               }
              }
          }
          //Users end.
      */


     // Deserialization 
     // Option 1.
     Users parsedUsers = CSONObject.toObject(csonObject, Users.class);

     // Option 2. Can be used even without a default constructor.
     //Users parsedUsers = new Users();
     //CSONObject.toObject(csonObject, parsedUsers);
     ```
   * However, there are some conditions.
     1. Should have a default constructor 'if possible'. It doesn’t matter if it’s private.
        ```java
        @CSON
        public class User {
           public User(String name) {}
           // Create a default constructor regardless of Java accessors.
           private User() {} 
        }
        ```
     2. Collection and Map cannot use RAW type. You must use generics.
        ```java
        // Error
        @CSONValue
        Map idUserMap = new HashMap();
        @CSONValue
        ArrayList names = new ArrayList();
        // OK
        @CSONValue
        ArrayList<User> users = new ArrayList<>();
        ```
     3. You can nest a Collection within a Collection. However, Map cannot be placed inside Collection. If you want to put a map inside a Collection, put a class that wraps Map.
           ```java
           // OK
           @CSONValue
           List<HashSet<User>> users = new ArrayList<>();
           // Error
           @CSONValue
           List<HashMap<String, User>> users = new ArrayList<>();
           ```
     4. The Key of Map must be of 'String' type. Collection and MAP cannot be used as values. If you need to put a Collection in a Map, create a class that wraps the Collection.
           ```java
           // OK
           @CSONValue
           Map<String, User> idUserMap = new HashMap<>();
           // OK
           @CSONValue
           Map<User, String> userAddrMap = new HashMap<>();
           // Error
           @CSONValue
           Map<String, List<User>> userListMap = new HashMap<>();
           ```
     5. Beware of circular references. For example, if classes A and B both declare fields of each other's types, you could end up in an infinite loop!!
           ```java 
           // Error. Circular reference (StackOverflowError) 
           @CSON
           public class A {
               @CSONValue
               private B b;
           }
           @CSON
           public class B {
               @CSONValue
               private A a;
           }
           ```
     6. @CSONValueSetter can return anything. But it must have one parameter. @CSONValueGetter must have no parameters. But it must have a return value.
           ```java
           import java.util.ArrayList;import java.util.List;@CSON
           public class User {
            
               private List<String> friends;
        
        
               // Setter starts with 'set'. Any name other than 'set' becomes the key value.  
               @CSONValueSetter
               public User setFriends(List<String> friends) {
                   this.friends = friends;
                   return this; 
               }
   
               // Getter starts with 'get' or 'is'.
               @CSONValueGetter
               public List<String> getFriends() {
                   return friends;
               }
   
               // 'set Friends(List<String> friends) method defined earlier.' Can be used with methods.  
               @CSONValueSetter("friends")
               public User setFriendsAndRemoveDuplicate(Set<String> friends) {
                 this.friends = new ArrayList<>(friends);
                 return this;
               }
           }
           ```
        8. Object serialization/deserialization also includes values from parent classes. 
        9. Arrays are not supported. Use collections. However, byte[] is converted to Base64 String.


  * You can also import an object with a certain key value in a JSON object by converting it to a Java object.
    ```java
    String json5 = "{user: { name: 'John',  age: 25,  friends: [ 'Nancy', 'Mary', 'Tom', 'Jerry' ], addr: { city: 'seoul', zipCode: '06164'  } }}";
    CSONObject user = new CSONObject(json5, JSONOptions.json5());
    //User user = user.getObject("user", User.class);
    // or
    User user = user.optObject("user", User.class, null);
    // 
    ```
  * If you put in CSONObject a Collection containing objects of a @CSON annotated class, it will be serialized as JSONArray data. 
    ```java
    @CSON
    public static class User {
        @CSONValue
        private String name;
    
        private User() {}
        public User(String name) {
            this.name = name;
        }
    }
    
    CSONObject obj = new CSONObject();
    List<User> users = new ArrayList<>();
    users.add(new User("John"));
    users.add(new User("Mary"));
    obj.put("users", users);
    
    System.out.println(obj);
    // {"users":[{"name":"John"},{"name":"Mary"}]}
    ```
  * It is also possible to import a JSON Array as a List<T> object. However, Collection, Map, or primitive types cannot be used.
    ```java
    CSONObject obj = new CSONObject();
    CSONArray users = new CSONArray();
    for(int i = 0; i < 10; i++) { 
       // The User class created in the previous example.
       User user = new User("name" + i);
       users.put(user);
    }
    obj.put("users", users);
    System.out.println(obj);
    // {"users":[{"name":"name0"},{"name":"name1"},{"name":"name2"},{"name":"name3"},{"name":"name4"},{"name":"name5"},{"name":"name6"},{"name":"name7"},{"name":"name8"},{"name":"name9"}]}
    // ...
    // The last argument is the default value when deserialization fails.
    List<User> users = obj.optList("users", User.class, null);
    ```
    
          




