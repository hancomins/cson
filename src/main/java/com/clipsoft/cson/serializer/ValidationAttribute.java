package com.clipsoft.cson.serializer;

import com.clipsoft.cson.util.DataConverter;
import com.clipsoft.cson.util.NullValue;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

// 0.9.28
public class ValidationAttribute {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final boolean required;
    private final Pattern pattern;
    private final String message;
    private final boolean notNull;
    private final double min;
    private final double max;
    private final boolean typeMatch;

    public String getInvalidMessage() {
        return message;
    }


    static ValidationAttribute of(CSONValidation csonValidation) {
        if(csonValidation == null) {
            return null;
        }
        return new ValidationAttribute(csonValidation);
    }

    static boolean isValid(ValidationAttribute validationAttribute,Object value, Types types) {
        if(validationAttribute == null) {
            return true;
        }
        Types valueType = null;
        if(value == NullValue.Instance || value == null) {
            value = null;
        } else {
            valueType = Types.of(value.getClass());
        }
        if(value == null && validationAttribute.notNull) {
            return false;
        }

        if(validationAttribute.pattern != null && value != null && !validationAttribute.pattern.matcher(value.toString()).matches()) {
            return false;
        }

        if(validationAttribute.typeMatch &&
                valueType != null &&
                !(valueType == types || valueType.isNumber() == types.isNumber()))  {

                return false;
        }


        switch (types) {
            case String:
                return isValidString(value == null ? null : value.toString(),validationAttribute);
            case Boolean:
                value = value == null ? null :
                        value instanceof Boolean ? (Boolean)value : Boolean.valueOf(value.toString());
                return isValidBoolean((Boolean)value,validationAttribute);
            case Byte:
            case Character:
            case Short:
            case Integer:
            case Float:
            case Long:
            case Double:
            case BigInteger:
            case BigDecimal:
                value = value == null ? null :
                        value instanceof Number ? (Number)value : DataConverter.toDouble(value.toString());
                return isValidNumber((Number)value,validationAttribute);
            case ByteArray:
                value = value == null ? null :
                        value instanceof byte[] ? (byte[])value : DataConverter.toByteArray(value.toString());
                return isValidByteArray((byte[])value,validationAttribute);
            case Collection:
            case CSONArray:
                value = value == null ? null :
                        value instanceof Collection ? (Collection<?>)value : Collections.EMPTY_LIST;
                return isValidCollection((Collection<?>)value,validationAttribute);

        }

        return true;
    }

    private static boolean isValidByteArray(byte[] value, ValidationAttribute validationAttribute) {
        if(value == null) {
            if(validationAttribute.required) {
                return false;
            }
            value = EMPTY_BYTE_ARRAY;
        }

        if(validationAttribute.min > -1 && value.length < validationAttribute.min) {
            return false;
        }
        return !(validationAttribute.max > -1) || !(value.length > validationAttribute.max);
    }

    private static boolean isValidCollection(Collection<?> value, ValidationAttribute validationAttribute) {
        if(validationAttribute.required && value == null) {
            return false;
        }

        if(validationAttribute.min > -1 && value.size() < validationAttribute.min) {
            return false;
        }
        return !(validationAttribute.max > -1) || !(value.size() > validationAttribute.max);
    }



    private static boolean isValidBoolean(Boolean value,ValidationAttribute validationAttribute) {
        if(validationAttribute.required && value == null) {
            return false;
        }
        return true;
    }

    private static boolean isValidNumber(Number value,ValidationAttribute validationAttribute) {
        if(validationAttribute.required && value == null) {
            return false;
        }
        if(validationAttribute.min > -1 && value.doubleValue() < validationAttribute.min) {
            return false;
        }
        return !(validationAttribute.max > -1) || !(value.doubleValue() > validationAttribute.max);
    }


    private static boolean isValidString(String value,ValidationAttribute validationAttribute) {
        if(value == null || value.isEmpty()) {
            if(validationAttribute.required) {
                return false;
            }
            value = "";
        }

        int length = value.length();
        if(validationAttribute.min > -1 && length < validationAttribute.min) {
            return false;
        }
        if(validationAttribute.max > -1 && length > validationAttribute.max) {
            return false;
        }
        if(validationAttribute.pattern != null) {
            return validationAttribute.pattern.matcher(value).matches();
        }
        return true;
    }


    ValidationAttribute(CSONValidation csonValidation) {
        this.required = csonValidation.required();
        String regex = csonValidation.pattern();
        if(regex != null && !regex.isEmpty()) {
            this.pattern = Pattern.compile(regex);
        } else {
            this.pattern = null;
        }
        this.message = csonValidation.message();
        this.notNull = csonValidation.notNull();
        this.min = csonValidation.min();
        this.max = csonValidation.max();
        this.typeMatch = csonValidation.typeMatch();
    }







}
