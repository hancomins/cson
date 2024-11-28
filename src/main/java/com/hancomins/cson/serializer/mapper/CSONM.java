package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.container.ArrayDataContainerFactory;
import com.hancomins.cson.container.ArrayDataContainerWrapper;
import com.hancomins.cson.container.json.JSON5Parser;
import com.hancomins.cson.options.JsonParsingOptions;
import com.hancomins.cson.util.NoSynchronizedStringReader;

public class CSONM {

     public <T> T toObject(String json, T object) {
        NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
        ObjectSchemaContainer.ObjectSchemaContainerFactory factory = new ObjectSchemaContainer.ObjectSchemaContainerFactory(object);
         ArrayDataContainerFactory arrayDataContainerFactory =  ArrayDataContainerWrapper.newFactory(null);
        ObjectSchemaContainer container = (ObjectSchemaContainer) factory.create();
        JSON5Parser.parse(reader,   JsonParsingOptions.json5(), container, factory, arrayDataContainerFactory);
        return object;



    }

}
