package com.hancomins.cson.options;

@SuppressWarnings("UnusedReturnValue")
public interface IMutableINumberConversionOption<T> extends INumberConversionOption {
    T  setAllowNaN(boolean enable);
    T  setAllowInfinity(boolean enable);
    T  setAllowHexadecimal(boolean enable);
    T  setLeadingZeroOmission(boolean enable);
    T  setAllowPositiveSing(boolean enable);
    T  setIgnoreNonNumeric(boolean enable);
}
