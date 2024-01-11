package com.clipsoft.cson;

public class PureJSONOption implements StringFormatOption<PureJSONOption> {
    @Override
    public StringFormatType getFormatType() {
        return StringFormatType.PureJSON;
    }

    private boolean isAllowNaN = false;
    private boolean isAllowInfinity = false;
    private boolean isAllowHexadecimal = true;
    private boolean isLeadingZeroOmission = true;
    private boolean isAllowPositiveSing = true;
    private boolean isIgnoreNonNumeric = true;



    @Override
    public boolean isAllowNaN() {
        return isAllowNaN;
    }

    @Override
    public boolean isAllowInfinity() {
        return isAllowInfinity;
    }

    @Override
    public boolean isAllowHexadecimal() {
        return isAllowHexadecimal;
    }

    @Override
    public boolean isLeadingZeroOmission() {
        return isLeadingZeroOmission;
    }

    @Override
    public boolean isAllowPositiveSing() {
        return isAllowPositiveSing;
    }

    @Override
    public boolean isIgnoreNonNumeric() {
        return isIgnoreNonNumeric;
    }


    @Override
    public PureJSONOption setAllowNaN(boolean enable) {
        this.isAllowNaN = enable;
        return  this;
    }

    
    @Override
    public  PureJSONOption setAllowInfinity(boolean enable) {
        this.isAllowInfinity = enable;
        return  this;
    }

    
    @Override
    public  PureJSONOption setAllowHexadecimal(boolean enable) {
        this.isAllowHexadecimal = enable;
        return  this;
    }

    
    @Override
    public  PureJSONOption setLeadingZeroOmission(boolean enable) {
        this.isLeadingZeroOmission = enable;
        return  this;
    }

    
    @Override
    public  PureJSONOption setAllowPositiveSing(boolean enable) {
        this.isAllowPositiveSing = enable;
        return  this;
    }

    
    @Override
    public  PureJSONOption setIgnoreNonNumeric(boolean enable) {
        this.isIgnoreNonNumeric = enable;
        return  this;
    }
}
