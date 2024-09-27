package com.hancomins.cson.internal;

public class CSONException extends RuntimeException{
    private static final long serialVersionUID = 0;

    public CSONException(final String message) {
        super(message);
    }

    public CSONException(int index,Object obj, String typeName) {
        super(ExceptionMessages.getCSONArrayValueConvertError(index, obj, typeName));
    }
    public CSONException(int index,Object obj, String typeName, Throwable e) {
        super(ExceptionMessages.getCSONArrayValueConvertError(index, obj, typeName), e);
    }

    public CSONException(String key,Object obj, String typeName) {
        super(ExceptionMessages.getCSONObjectValueConvertError(key, obj, typeName));
    }

    public CSONException(String key,Object obj, String typeName, Throwable e) {
        super(ExceptionMessages.getCSONObjectValueConvertError(key, obj, typeName), e);
    }

    public CSONException(final String message, final Throwable cause) {
        super(message, cause);
    }


    public CSONException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
