package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONArray;
import com.hancomins.cson.CSONElement;
import com.hancomins.cson.CSONObject;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Utils {

    static Class<?> primitiveTypeToBoxedType(Class<?> primitiveType) {
        if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        } else if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == void.class) {
            return Void.class;
        } else {
            return primitiveType;
        }
    }


    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    static Object convertCollectionValue(Object origin, List<CollectionItem> resultCollectionItemList, SchemaType returnType) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if(origin == null) {
            return null;
        }
        Collection resultCollectionOfCurrent = resultCollectionItemList.get(0).collectionConstructor.newInstance();
        Collection result = resultCollectionOfCurrent;
        ArrayDeque<Iterator> collectionIterators = new ArrayDeque<>();
        ArrayDeque<Collection> resultCollections = new ArrayDeque<>();
        int collectionItemIndex = 0;

        Iterator currentIterator = ((Collection<?>)origin).iterator();
        resultCollections.add(resultCollectionOfCurrent);
        collectionIterators.add(currentIterator);
        while(currentIterator.hasNext()) {
            Object next = currentIterator.next();
            if(next instanceof Collection) {
                ++collectionItemIndex;
                Collection newCollection = resultCollectionItemList.get(collectionItemIndex).collectionConstructor.newInstance();
                resultCollections.add(newCollection);
                resultCollectionOfCurrent.add(newCollection);
                resultCollectionOfCurrent = newCollection;
                currentIterator = ((Collection<?>)next).iterator();
                collectionIterators.add(currentIterator);
            } else {
                resultCollectionOfCurrent.add(convertValue(next,returnType));
            }

            while(!currentIterator.hasNext()) {
                collectionIterators.removeLast();
                if(collectionIterators.isEmpty()) {
                    return result;
                }
                --collectionItemIndex;
                resultCollections.removeLast();
                resultCollectionOfCurrent =  resultCollections.getLast();
                currentIterator = collectionIterators.getLast();
            }

        }

        return result;

    }

    static Object convertValue(Object origin, SchemaType returnType) {
        try {
            if(origin instanceof String) {
                return convertValueFromString((String)origin,returnType);
            } else if(origin instanceof Number) {
                return convertValueFromNumber((Number)origin,returnType);
            }

        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    static Object convertValueFromString(String origin, SchemaType returnType) {
        if(origin == null) {
            return null;
        }
        if(returnType == SchemaType.String) {
            return origin;
        } else if(returnType == SchemaType.Byte) {
            return Byte.valueOf(origin);
        } else if(returnType == SchemaType.Short) {
            return Short.valueOf(origin);
        } else if(returnType == SchemaType.Integer) {
            return Integer.valueOf(origin);
        } else if(returnType == SchemaType.Long) {
            return Long.valueOf(origin);
        } else if(returnType == SchemaType.Float) {
            return Float.valueOf(origin);
        } else if(returnType == SchemaType.Double) {
            return Double.valueOf(origin);
        } else if(returnType == SchemaType.Character) {
            return origin.charAt(0);
        } else if(returnType == SchemaType.Boolean) {
            return Boolean.valueOf(origin);
        } else if(returnType == SchemaType.BigDecimal) {
            return new BigDecimal(origin);
        } else if(returnType == SchemaType.BigInteger) {
            return new java.math.BigInteger(origin);
        }


        return null;

    }

    static Object convertValueFromNumber(Number origin, SchemaType returnType) {
        if(origin == null) {
            return null;
        }
        if(origin instanceof BigDecimal && returnType == SchemaType.BigDecimal) {
            return origin;
        } else if(origin instanceof Double && returnType == SchemaType.Double) {
            return origin;
        } else if(origin instanceof Float && returnType == SchemaType.Float) {
            return origin;
        } else if(origin instanceof Long && returnType == SchemaType.Long) {
            return origin;
        } else if(origin instanceof Integer && returnType == SchemaType.Integer) {
            return origin;
        } else if(origin instanceof Short && returnType == SchemaType.Short) {
            return origin;
        } else if(origin instanceof Byte && returnType == SchemaType.Byte) {
            return origin;
        }

        if(returnType == SchemaType.Byte) {
            return origin.byteValue();
        } else if(returnType == SchemaType.Short) {
            return origin.shortValue();
        } else if(returnType == SchemaType.Integer) {
            return origin.intValue();
        } else if(returnType == SchemaType.Long) {
            return origin.longValue();
        } else if(returnType == SchemaType.Float) {
            return origin.floatValue();
        } else if(returnType == SchemaType.Double) {
            return origin.doubleValue();
        } else if(returnType == SchemaType.Character) {
            return (char)origin.intValue();
        } else if(returnType == SchemaType.Boolean) {
            return origin.intValue() != 0;
        } else if(returnType == SchemaType.BigDecimal) {
            return new BigDecimal(origin.toString());
        } else if(returnType == SchemaType.BigInteger) {
            return new java.math.BigInteger(origin.toString());
        } else if(returnType == SchemaType.String) {
            return origin.toString();
        } else if(returnType == SchemaType.ByteArray) {
            return new byte[]{origin.byteValue()};
        }
        return null;
    }

    static Object optFrom(CSONElement cson, Object key, SchemaType valueType) {
        boolean isArrayType = cson instanceof CSONArray;
        if(isArrayType && ((CSONArray)cson).isNull((int)key)) {
            return null;
        } else if(!isArrayType && ((CSONObject)cson).isNull((String)key)) {
            return null;
        }
        if(SchemaType.Boolean == valueType) {
            return isArrayType ? ((CSONArray) cson).optBoolean((int)key) : ((CSONObject)cson).optBoolean((String)key);
        } else if(SchemaType.Byte == valueType) {
            return  isArrayType ? ((CSONArray) cson).optByte((int)key) : ((CSONObject)cson).optByte((String)key);
        } else if(SchemaType.Character == valueType) {
            return  isArrayType ? ((CSONArray) cson).optChar((int)key, '\0') : ((CSONObject)cson).optChar((String)key, '\0');
        } else if(SchemaType.Short == valueType) {
            return  isArrayType ? ((CSONArray) cson).optShort((int)key) : ((CSONObject)cson).optShort((String)key);
        } else if(SchemaType.Integer == valueType) {
            return  isArrayType ? ((CSONArray) cson).optInt((int)key) : ((CSONObject)cson).optInt((String)key);
        } else if(SchemaType.Float == valueType) {
            return  isArrayType ? ((CSONArray) cson).optFloat((int)key) : ((CSONObject)cson).optFloat((String)key);
        } else if(SchemaType.Double == valueType) {
            return  isArrayType ? ((CSONArray) cson).optDouble((int)key) : ((CSONObject)cson).optDouble((String)key);
        } else if(SchemaType.String == valueType) {
            return  isArrayType ? ((CSONArray) cson).optString((int)key) : ((CSONObject)cson).optString((String)key);
        }  else if(SchemaType.ByteArray == valueType) {
            return  isArrayType ? ((CSONArray) cson).optByteArray((int)key) : ((CSONObject)cson).optByteArray((String)key);
        } else {
            return  isArrayType ? ((CSONArray) cson).opt((int)key) : ((CSONObject)cson).opt((String)key);
        }
    }

}
