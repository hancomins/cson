package com.hancomins.cson.serializer;

import com.hancomins.cson.format.json.JSON5Parser;
import com.hancomins.cson.options.JsonParsingOptions;
import com.hancomins.cson.util.NoSynchronizedStringReader;

public class CSONM {

     public <T> T toObject(String json, T object) {
        NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
        ObjectSchemaContainer.ObjectSchemaContainerFactory factory = new ObjectSchemaContainer.ObjectSchemaContainerFactory(object);
        ObjectSchemaContainer container = (ObjectSchemaContainer) factory.create();
        JSON5Parser.parse(reader,   JsonParsingOptions.json5(), container, factory, null);
        return object;



    }

}
