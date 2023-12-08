package com.snoworca.cson;

public class PureJSONOption implements StringFormatOption {
    @Override
    public StringFormatType getFormatType() {
        return StringFormatType.PureJSON;
    }
}
