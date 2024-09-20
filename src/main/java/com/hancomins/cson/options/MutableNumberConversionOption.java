package com.hancomins.cson.options;

public interface MutableNumberConversionOption<T> extends NumberConversionOption {


    public  T setAllowNaN(boolean enable);
    public  T  setAllowInfinity(boolean enable);
    public  T  setAllowHexadecimal(boolean enable);
    public  T  setLeadingZeroOmission(boolean enable);
    public  T  setAllowPositiveSing(boolean enable);
    public  T  setIgnoreNonNumeric(boolean enable);
}
