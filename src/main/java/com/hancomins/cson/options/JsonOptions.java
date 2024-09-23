package com.hancomins.cson.options;

public class JsonOptions implements CSONOptions {

    private static JsonWritingOptions DEFAULT_WRITING_OPTIONS = JsonWritingOptions.json();
    private static JsonParsingOptions DEFAULT_PARSING_OPTIONS = JsonParsingOptions.json5();


    private JsonParsingOptions parsingOptions = DEFAULT_PARSING_OPTIONS;
    private JsonWritingOptions writingOptions = DEFAULT_WRITING_OPTIONS;

    public static JsonOptions fromJson5ToJson() {
        JsonOptions jsonOptions = new JsonOptions();
        jsonOptions.setJSONParsingOptions(JsonParsingOptions.json5());
        jsonOptions.setJSONWritingOptions(JsonWritingOptions.json();
        return jsonOptions;
    }

    public static JsonOptions fromJson5ToJsonPretty() {
        JsonOptions jsonOptions = new JsonOptions();
        jsonOptions.setJSONParsingOptions(JsonParsingOptions.json5());
        jsonOptions.setJSONWritingOptions(JsonWritingOptions.prettyJson());
        return jsonOptions;
    }

    public static JsonOptions fromJson5ToJson5() {
        JsonOptions jsonOptions = new JsonOptions();
        jsonOptions.setJSONParsingOptions(JsonParsingOptions.json5());
        jsonOptions.setJSONWritingOptions(JsonWritingOptions.json5());
        return jsonOptions;
    }


    @SuppressWarnings("unchecked")
    @Override
    public JsonParsingOptions getParsingOptions() {
        return parsingOptions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonWritingOptions getWritingOptions() {
        return writingOptions;
    }


    public void setJSONParsingOptions(JsonParsingOptions options) {
        parsingOptions = options;
    }

    public void setJSONWritingOptions(JsonWritingOptions options) {
        writingOptions = options;
    }

    public static void setDefaultJSONWritingOptions(JsonWritingOptions options) {
        DEFAULT_WRITING_OPTIONS = options;
    }

    public static void setDefaultJSONParsingOptions(JsonParsingOptions options) {
        DEFAULT_PARSING_OPTIONS = options;
    }

}
