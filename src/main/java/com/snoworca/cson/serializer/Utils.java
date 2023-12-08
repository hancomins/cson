package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.math.BigDecimal;

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
    static Object convertCollectionValue(Object origin, List<CollectionItems> resultCollectionItemsList, Types returnType) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if(origin == null) {
            return null;
        }
        Collection resultCollectionOfCurrent = resultCollectionItemsList.get(0).collectionConstructor.newInstance();
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
                Collection newCollection = resultCollectionItemsList.get(collectionItemIndex).collectionConstructor.newInstance();
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

    static Object convertValue(Object origin, Types returnType) {
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

    static Object convertValueFromString(String origin, Types returnType) {
        if(origin == null) {
            return null;
        }
        if(returnType == Types.String) {
            return origin;
        } else if(returnType == Types.Byte) {
            return Byte.valueOf(origin);
        } else if(returnType == Types.Short) {
            return Short.valueOf(origin);
        } else if(returnType == Types.Integer) {
            return Integer.valueOf(origin);
        } else if(returnType == Types.Long) {
            return Long.valueOf(origin);
        } else if(returnType == Types.Float) {
            return Float.valueOf(origin);
        } else if(returnType == Types.Double) {
            return Double.valueOf(origin);
        } else if(returnType == Types.Character) {
            return origin.charAt(0);
        } else if(returnType == Types.Boolean) {
            return Boolean.valueOf(origin);
        } else if(returnType == Types.BigDecimal) {
            return new java.math.BigDecimal(origin);
        }
        return null;

    }

    static Object convertValueFromNumber(Number origin, Types returnType) {
        if(origin == null) {
            return null;
        }
        if(origin instanceof BigDecimal && returnType == Types.BigDecimal) {
            return origin;
        } else if(origin instanceof Double && returnType == Types.Double) {
            return origin;
        } else if(origin instanceof Float && returnType == Types.Float) {
            return origin;
        } else if(origin instanceof Long && returnType == Types.Long) {
            return origin;
        } else if(origin instanceof Integer && returnType == Types.Integer) {
            return origin;
        } else if(origin instanceof Short && returnType == Types.Short) {
            return origin;
        } else if(origin instanceof Byte && returnType == Types.Byte) {
            return origin;
        }

        if(returnType == Types.Byte) {
            return origin.byteValue();
        } else if(returnType == Types.Short) {
            return origin.shortValue();
        } else if(returnType == Types.Integer) {
            return origin.intValue();
        } else if(returnType == Types.Long) {
            return origin.longValue();
        } else if(returnType == Types.Float) {
            return origin.floatValue();
        } else if(returnType == Types.Double) {
            return origin.doubleValue();
        } else if(returnType == Types.Character) {
            return (char)origin.intValue();
        } else if(returnType == Types.Boolean) {
            return origin.intValue() != 0;
        } else if(returnType == Types.BigDecimal) {
            return new java.math.BigDecimal(origin.toString());
        } else if(returnType == Types.String) {
            return origin.toString();
        } else if(returnType == Types.ByteArray) {
            return new byte[]{origin.byteValue()};
        }
        return null;
    }

    static Object optFrom(CSONElement cson, Object key, Types valueType) {



        boolean isArrayType = cson instanceof CSONArray;
        if(isArrayType && ((CSONArray)cson).isNull((int)key)) {
            return null;
        } else if(!isArrayType && ((CSONObject)cson).isNull((String)key)) {
            return null;
        }
        if(Types.Boolean == valueType) {
            return isArrayType ? ((CSONArray) cson).optBoolean((int)key) : ((CSONObject)cson).optBoolean((String)key);
        } else if(Types.Byte == valueType) {
            return  isArrayType ? ((CSONArray) cson).optByte((int)key) : ((CSONObject)cson).optByte((String)key);
        } else if(Types.Character == valueType) {
            return  isArrayType ? ((CSONArray) cson).optChar((int)key, '\0') : ((CSONObject)cson).optChar((String)key, '\0');
        } else if(Types.Short == valueType) {
            return  isArrayType ? ((CSONArray) cson).optShort((int)key) : ((CSONObject)cson).optShort((String)key);
        } else if(Types.Integer == valueType) {
            return  isArrayType ? ((CSONArray) cson).optInt((int)key) : ((CSONObject)cson).optInt((String)key);
        } else if(Types.Float == valueType) {
            return  isArrayType ? ((CSONArray) cson).optFloat((int)key) : ((CSONObject)cson).optFloat((String)key);
        } else if(Types.Double == valueType) {
            return  isArrayType ? ((CSONArray) cson).optDouble((int)key) : ((CSONObject)cson).optDouble((String)key);
        } else if(Types.String == valueType) {
            return  isArrayType ? ((CSONArray) cson).optString((int)key) : ((CSONObject)cson).optString((String)key);
        }  else if(Types.ByteArray == valueType) {
            return  isArrayType ? ((CSONArray) cson).optByteArray((int)key) : ((CSONObject)cson).optByteArray((String)key);
        }
        return null;

    }

}
