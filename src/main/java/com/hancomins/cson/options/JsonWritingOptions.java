package com.hancomins.cson.options;

public class JsonWritingOptions extends WritingOptions<JsonWritingOptions> {
    private boolean pretty = false;
    private boolean skipComments = false;
    private boolean isUnprettyArray = false;
    private int space = 4;
    private String depthString = "    ";

    private String keyQuote = "\"";
    private String valueQuote = "\"";


    private boolean allowUnquoted = false;
    private boolean allowSingleQuotes = false;
    private boolean isAllowLineBreak = false;


    public JsonWritingOptions() {
    }


    public static JsonWritingOptions json5() {
        JsonWritingOptions jsonWritingOptions = new JsonWritingOptions();
        jsonWritingOptions.setAllowNaN(true);
        jsonWritingOptions.setAllowInfinity(true);
        jsonWritingOptions.setAllowHexadecimal(true);
        jsonWritingOptions.setLeadingZeroOmission(true);
        jsonWritingOptions.setAllowPositiveSing(true);
        jsonWritingOptions.setAllowHexadecimal(true);
        jsonWritingOptions.setPretty(true);
        jsonWritingOptions.setUnprettyArray(false);
        jsonWritingOptions.setSpace(4);
        jsonWritingOptions.setKeyQuote("");
        jsonWritingOptions.setValueQuote("'");
        jsonWritingOptions.setAllowUnquoted(true);
        jsonWritingOptions.setAllowSingleQuotes(true);
        jsonWritingOptions.setAllowLineBreak(true);
        return jsonWritingOptions;
    }

    public static JsonWritingOptions json() {
        JsonWritingOptions jsonWritingOptions = new JsonWritingOptions();
        jsonWritingOptions.setAllowNaN(false);
        jsonWritingOptions.setAllowInfinity(false);
        jsonWritingOptions.setAllowHexadecimal(true);
        jsonWritingOptions.setLeadingZeroOmission(false);
        jsonWritingOptions.setAllowPositiveSing(false);
        jsonWritingOptions.setIgnoreNonNumeric(true);
        jsonWritingOptions.setAllowHexadecimal(true);
        jsonWritingOptions.setPretty(false);
        jsonWritingOptions.setUnprettyArray(false);
        jsonWritingOptions.setSpace(4);
        jsonWritingOptions.setKeyQuote("\"");
        jsonWritingOptions.setValueQuote("'");
        jsonWritingOptions.setAllowUnquoted(false);
        jsonWritingOptions.setAllowSingleQuotes(false);
        jsonWritingOptions.setAllowLineBreak(false);
        return jsonWritingOptions;
    }

    public static JsonWritingOptions prettyJson() {
        JsonWritingOptions jsonWritingOptions = JsonWritingOptions.json();
        jsonWritingOptions.setPretty(true);
        jsonWritingOptions.setSpace(4);
        jsonWritingOptions.setUnprettyArray(false);
        return jsonWritingOptions;
    }





    public boolean isSkipComments() {
        return skipComments;
    }

    public JsonWritingOptions setSkipComments(boolean skipComments) {
        this.skipComments = skipComments;
        return this;
    }

    public boolean isPretty() {
        return pretty;
    }

    public JsonWritingOptions setPretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }

    public boolean isUnprettyArray() {
        return isUnprettyArray;
    }

    public JsonWritingOptions setUnprettyArray(boolean isUnprettyArray) {
        this.isUnprettyArray = isUnprettyArray;
        return this;
    }

    public int getSpace() {
        return space;
    }

    public JsonWritingOptions setSpace(int space) {
        if(space == this.space) return this;
        if(space < 1) space = 1; // minimum space is 1
        else if(space > 10) space = 10; // maximum space is 8
        this.space = space;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < space; i++) {
            sb.append(' ');
        }
        this.depthString = sb.toString();
        return this;
    }



    public String getDepthString() {
        return depthString;
    }


    public String getKeyQuote() {
        return keyQuote;
    }

    public JsonWritingOptions setKeyQuote(String keyQuote) {
        this.keyQuote = keyQuote;
        return this;
    }

    public String getValueQuote() {
        return valueQuote;
    }

    public JsonWritingOptions setValueQuote(String valueQuote) {
        if(valueQuote.length() > 1)
            throw new IllegalArgumentException("valueQuote can not be more than one character");
        this.valueQuote = valueQuote;
        return this;
    }

    public boolean isAllowUnquoted() {
        return allowUnquoted;
    }

    public JsonWritingOptions setAllowUnquoted(boolean allowUnquoted) {
        this.allowUnquoted = allowUnquoted;
        return this;
    }

    public boolean isAllowSingleQuotes() {
        return allowSingleQuotes;
    }

    public JsonWritingOptions setAllowSingleQuotes(boolean allowSingleQuotes) {
        this.allowSingleQuotes = allowSingleQuotes;
        return this;
    }

    public boolean isAllowLineBreak() {
        return isAllowLineBreak;
    }

    public JsonWritingOptions setAllowLineBreak(boolean allowLineBreak) {
        this.isAllowLineBreak = allowLineBreak;
        return this;
    }


}
