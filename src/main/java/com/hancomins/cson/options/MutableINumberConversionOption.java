package com.hancomins.cson.options;

@SuppressWarnings("ALL")
public class MutableINumberConversionOption<T> implements IMutableINumberConversionOption<T> {

    private boolean allowNaN = true;
    private boolean allowInfinity = true;
    private boolean allowHexadecimal = true;
    private boolean leadingZeroOmission = true;
    private boolean allowPositiveSing = true;
    private boolean ignoreNonNumeric = true;



    @Override
    public T setAllowNaN(boolean enable) {
        allowNaN = enable;
        return (T) this;
    }

    @Override
    public T setAllowInfinity(boolean enable) {
        allowInfinity = enable;
        return (T) this;
    }

    @Override
    public T setAllowHexadecimal(boolean enable) {
        allowHexadecimal = enable;
        return (T) this;
    }

    @Override
    public T setLeadingZeroOmission(boolean enable) {
        leadingZeroOmission = enable;
        return (T) this;
    }

    @Override
    public T setAllowPositiveSing(boolean enable) {
        allowPositiveSing = enable;
        return (T) this;
    }

    @Override
    public T setIgnoreNonNumeric(boolean enable) {
        ignoreNonNumeric = enable;
        return (T) this;
    }

    @Override
    public boolean isAllowNaN() {
        return allowNaN;
    }

    @Override
    public boolean isAllowInfinity() {
        return allowInfinity;
    }

    @Override
    public boolean isAllowHexadecimal() {
        return allowHexadecimal;
    }

    @Override
    public boolean isLeadingZeroOmission() {
        return leadingZeroOmission;
    }

    @Override
    public boolean isAllowPositiveSing() {
        return allowPositiveSing;
    }

    @Override
    public boolean isIgnoreNonNumeric() {
        return ignoreNonNumeric;
    }
}
