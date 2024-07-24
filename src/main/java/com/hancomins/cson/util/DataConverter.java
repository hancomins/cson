package com.hancomins.cson.util;

import com.hancomins.cson.CSONArray;
import com.hancomins.cson.CSONException;
import com.hancomins.cson.CSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DataConverter {


	public static String getTypeName(Object value) {
		if(value == null || value == NullValue.Instance) return "null";
		if(value instanceof String) return "String";
		if(value instanceof Number) return "Number";
		if(value instanceof Boolean) return "Boolean";
		if(value instanceof Character) return "Character";
		if(value instanceof byte[]) return "byte[]";
		if(value instanceof CSONArray) return "CSONArray";
		if(value instanceof CSONObject) return "CSONObject";
		return value.getClass().getSimpleName();
	}


	public static CSONArray toArray(Object value, boolean allowFromData) {
		if(value instanceof CSONArray) {
			return (CSONArray)value;
		}
		if(allowFromData && value instanceof String) {
			try {
				return CSONArray.fromJson((String) value);
			} catch (CSONException ignored) {}
		}
		if(allowFromData && value instanceof byte[]) {
			try {
				return CSONArray.fromBinaryCSON((byte[]) value);
			} catch (CSONException ignored) {}
		}
		return null;
	}

	public static CSONObject toObject(Object value, boolean allowFromData) {
		if(value instanceof CSONObject) {
			return (CSONObject)value;
		}
		if(allowFromData && value instanceof String) {
			try {
				return CSONObject.fromJson((String) value);
			} catch (CSONException ignored) {}
		}
		if(allowFromData && value instanceof byte[]) {
			try {
				return CSONObject.fromBinaryCSON((byte[]) value);
			} catch (CSONException ignored) {}
		}

		return null;
	}

	public static Object convertValue(Class<?> objectType,  Object value) {
		if(value == null) return null;
		if(objectType == null) return value;
		if(objectType == String.class) {
			return toString(value);
		}
		else if(objectType == Integer.class || objectType == int.class) {
			return toInteger(value);
		}
		else if(objectType == Long.class || objectType == long.class) {
			return toLong(value);
		}
		else if(objectType == Short.class || objectType == short.class) {
			return toShort(value);
		}
		else if(objectType == Byte.class || objectType == byte.class) {
			return toByte(value);
		}
		else if(objectType == Float.class || objectType == float.class) {
			return toFloat(value);
		}
		else if(objectType == Double.class || objectType == double.class) {
			return toDouble(value);
		}
		else if(objectType == Boolean.class || objectType == boolean.class) {
			return toBoolean(value);
		}
		else if(objectType == Character.class || objectType == char.class) {
			return toChar(value);
		}
		else if(objectType == byte[].class) {
			return toByteArray(value);
		}
		else if(objectType == CSONArray.class) {
			return toArray(value, false);
		}
		else if(objectType == CSONObject.class) {
			return toObject(value, false);
		}
		else if(objectType == BigDecimal.class) {
			return new BigDecimal(toString(value));
		}
		else if(objectType == BigInteger.class) {
			return new BigInteger(toString(value));
		}
		return value;

	}

	public static int toInteger(Object value) {
			return toInteger(value, 0, null);
	}

	public static int toInteger(Object value, int def) {
		return toInteger(value, def, null);
	}

	public static int toInteger(Object value, OnConvertFail onConvertFail) {
		return toInteger(value, 0, onConvertFail);
	}



	@SuppressWarnings("UnnecessaryUnboxing")
	public static int toInteger(Object value, int def, OnConvertFail onConvertFail) {
		try {
			if (value instanceof Number) {
				return ((Number) value).intValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			} else if (value instanceof String) {
				return Integer.parseInt( ((String)value).trim());
			} else if (value instanceof byte[] && ((byte[]) value).length > 3) {
				return ByteBuffer.wrap((byte[]) value).getInt();
			}
		} catch (Throwable e) {
			Double v = toInfinityOrNaN(value);
			if(v != null) {
				return v.intValue();
			}
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, int.class);
		}
		return def;
	}

	public static Double toInfinityOrNaN(Object value) {
		if("Infinity".equalsIgnoreCase((String) value) || "+Infinity".equalsIgnoreCase((String) value)) {
			return Double.POSITIVE_INFINITY;
		} else if("-Infinity".equalsIgnoreCase((String) value)) {
			return Double.NEGATIVE_INFINITY;
		} else if("NaN".equalsIgnoreCase((String) value)) {
			return Double.NaN;
		}
		return null;
	}

	public static short toShort(Object value) {
		return toShort(value, (short) 0, null);
	}

	public static short toShort(Object value, short def) {
		return toShort(value, def, null);
	}

	public static short toShort(Object value, OnConvertFail onConvertFail) {
		return toShort(value, (short) 0, onConvertFail);
	}

	public static short toShort(Object value, short def, OnConvertFail onConvertFail) {
		try {
			if (value instanceof Number) {
				return ((Number) value).shortValue();
			} else if (value instanceof Character) {
				return (short) ((Character) value).charValue();
			} else if (value instanceof String) {
				return Short.parseShort((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 1) {
				return ByteBuffer.wrap((byte[]) value).getShort();
			}
		} catch (Throwable e) {
			Double v = toInfinityOrNaN(value);
			if(v != null) {
				return v.shortValue();
			}
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, short.class);
		}
		return def;
	}

	public static byte toByte(Object value) {
		return toByte(value, (byte) 0, null);
	}

	public static byte toByte(Object value, byte def) {
		return toByte(value, def, null);
	}

	public static byte toByte(Object value, byte def, OnConvertFail onConvertFail) {
		try {
			if (value instanceof Number) {
				return ((Number) value).byteValue();
			} else if (value instanceof Character) {
				return (byte)((Character) value).charValue();
			} else if (value instanceof String) {
				return Byte.parseByte((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 1) {
				return ((byte[])value)[0];
			}
		} catch (Throwable e) {
			Double v = toInfinityOrNaN(value);
			if(v != null) {
				return v.byteValue();
			}
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, byte.class);
		}
		return def;
	}


	public static float toFloat(Object value) {
		return toFloat(value, Float.NaN);
	}

	public static float toFloat(Object value, float def) {
		return toFloat(value, def, null);
	}

	public static float toFloat(Object value, OnConvertFail onConvertFail) {
		return toFloat(value, Float.NaN, onConvertFail);
	}

	@SuppressWarnings({"SameParameterValue", "UnnecessaryUnboxing"})
	public static float toFloat(Object value, float def, OnConvertFail onConvertFail) {
		try {
			if (value instanceof Number) {
				return ((Number) value).floatValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			} else if (value instanceof String) {
				return Float.parseFloat((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 3) {
				return ByteBuffer.wrap((byte[]) value).getFloat();
			}
		}catch (Throwable e) {
			Double v = toInfinityOrNaN(value);
			if(v != null) {
				return v.floatValue();
			}
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, float.class);
		}
		return def;
	}

	public static double toDouble(Object value) {
		return toDouble(value, Double.NaN, null);
	}

	public static double toDouble(Object value, OnConvertFail onConvertFail) {
		return toDouble(value, Double.NaN, onConvertFail);
	}

	public static double toDouble(Object value, double def) {
		return toDouble(value, def, null);
	}

	@SuppressWarnings("SameParameterValue")
	public static double toDouble(Object value, double def, OnConvertFail onConvertFail) {
		try {
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			} else if (value instanceof Character) {
				//noinspection UnnecessaryUnboxing
				return ((Character) value).charValue();
			} else if (value instanceof String) {
				return Double.parseDouble((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 7) {
				return ByteBuffer.wrap((byte[]) value).getDouble();
			}
		} catch (Throwable e) {
			Double v = toInfinityOrNaN(value);
			if(v != null) {
				return v;
			}
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, double.class);
		}
		return def;
	}

	public static Number toBoxingNumberOfType(Object value, Class<? extends Number> type) {
		if(value.getClass() == type) {
			return (Number)value;
		}
		else if(value instanceof Number) {
			Number number = (Number)value;
			if(type == Integer.class) {

				return number.intValue();
			}
			else if(type == Long.class) {
				return number.longValue();
			}
			else if(type == Short.class) {
				return number.shortValue();
			}
			else if(type == Byte.class) {
				return number.byteValue();
			}
			else if(type == Float.class) {
				return number.floatValue();
			}
			else if(type == Double.class) {
				return number.doubleValue();
			}
			else if(type == BigDecimal.class) {
				if (number instanceof BigDecimal) {
					return number;
				} else if (number instanceof BigInteger) {
					return new BigDecimal((BigInteger) number);
				} else {
					return BigDecimal.valueOf(number.doubleValue());
				}
			} else if(type == BigInteger.class) {
				if (number instanceof BigInteger) {
					return number;
				} else if (number instanceof BigDecimal) {
					return ((BigDecimal) number).toBigInteger();
				} else {
					return BigInteger.valueOf(number.longValue());
				}
			}
		}
		else if(value instanceof String) {
			try {
				Number no = NumberConversionUtil.stringToNumber((String)value, NumberConversionUtil.DEFAULT_NUMBER_CONVERSION_OPTION);
				return toBoxingNumberOfType(no, type);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}


	public static long toLong(Object value) {
		return toLong(value, 0L, null);
	}

	public static long toLong(Object value, OnConvertFail onConvertFail) {
		return toLong(value, 0L, onConvertFail);
	}

	public static long toLong(Object value, long def) {
		return toLong(value, def, null);
	}


	@SuppressWarnings("UnnecessaryUnboxing")
	public static long toLong(Object value, long def, OnConvertFail onConvertFail) {

		try {
			if(value instanceof Long) {
				return (Long)value;
			}
			else if (value instanceof Number) {
				return ((Number) value).longValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			} else if (value instanceof String) {
				return Long.parseLong((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 7) {
				return ByteBuffer.wrap((byte[]) value).getLong();
			}
		} catch (Throwable e) {
			Double v = toInfinityOrNaN(value);
			if(v != null) {
				return v.longValue();
			}
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, long.class);
		}
		return def;
	}

	public static char toChar(Object value) {
		return toChar(value, '\0', null);
	}
	public static char toChar(Object value, OnConvertFail onConvertFail) {
		return toChar(value, '\0', onConvertFail);
	}

	public static char toChar(Object value, char def) {
		return toChar(value, def, null);
	}

	@SuppressWarnings("UnnecessaryUnboxing")
	public static char toChar(Object value, char def, OnConvertFail onConvertFail) {
		if(value instanceof Number) {
			return (char)((Number)value).shortValue();
		}
		else if(value instanceof Character) {
			return ((Character)value).charValue();
		}
		else if(value instanceof Boolean) {
			return (char)(((Boolean)value) ? 1 : 0);
		}
		else if(value instanceof String) {
			if(((String)value).length() == 1) {
				return ((String) value).charAt(0);
			}
			return (char) Short.parseShort((String) value);
		}
		else if(value instanceof byte[] && ((byte[])value).length > 1 ) {
			return (char) ByteBuffer.wrap((byte[])value).getShort();
		}
		if(onConvertFail != null) {
			onConvertFail.onFail(value, char.class);
		}

		return def;
	}



	public static  String toString(Object value) {
		if(value == null  || value instanceof NullValue) return null;
		if(value instanceof String) {
			return (String) value;
		}
		if(value instanceof Number) {
			return value.toString();
		}
		else if(value instanceof byte[]) {
			byte[] buffer = (byte[])value;
			return Base64.getEncoder().encodeToString(buffer);
		}

		return value + "";
	}

	public static  boolean toBoolean(Object value) {
		return toBoolean(value, false);

	}

	public static  boolean toBoolean(Object value, boolean def) {
		try {
			if (value instanceof Boolean) {
				return ((Boolean) value);
			} else if (value instanceof Number) {
				return ((Number) value).intValue() > 0;
			} else if (value instanceof String) {
				String strValue = ((String) value).trim();
				return ("true".equalsIgnoreCase(strValue) || "1".equals(strValue));
			}
		}catch (Throwable ignored) {}
		return def;
	}

	public static <T extends Enum<T>> T toEnum(Class<T> enumType, Object value) {
		if(value == null || value instanceof NullValue) return null;
		if(enumType.isInstance(value)) {
			return (T)value;
		}
		if(value instanceof Number) {
			int ordinal = ((Number)value).intValue();
			T[] values = enumType.getEnumConstants();
			if(ordinal >= 0 && ordinal < values.length) {
				return values[ordinal];
			}
		}
		if(value instanceof String) {
			// 대소문자 가리지 않고 enum을 찾는다.
			String strValue = ((String)value).trim();
			T[] values = enumType.getEnumConstants();
			for(T t : values) {
				if(t.name().equalsIgnoreCase(strValue)) {
					return t;
				}
			}
		}
		return null;
	}



	public static byte[] toByteArray(Object obj) {
		if(obj == null || obj instanceof NullValue) return null;
		if(obj instanceof byte[]) {
			return (byte[])obj;
		}
		else if(obj instanceof CharSequence) {
			String strValue = obj.toString();
			int index = strValue.indexOf(',');
			if(index < 0) {
				index = strValue.indexOf(':');
			}
			if(index > 0) {
				String prefix = strValue.substring(0, index);
				if (prefix.equalsIgnoreCase("base64")) {
					return Base64.getDecoder().decode(strValue.substring(index + 1));
				}
			}


			return ((String)obj).getBytes(StandardCharsets.UTF_8);
		}
		else if(obj instanceof Boolean) {
			return ByteBuffer.allocate(1).put((byte)(((Boolean)obj) ? 1 : 0)).array();
		}
		else if(obj instanceof Character) {
			return ByteBuffer.allocate(2).putChar(((Character)obj)).array();
		}
		else if(obj instanceof Double) {
			return ByteBuffer.allocate(8).putDouble(((Double)obj)).array();
		}
		else if(obj instanceof Short) {
			return ByteBuffer.allocate(2).putShort(((Short)obj)).array();
		}
		else if(obj instanceof Byte) {
			return ByteBuffer.allocate(1).put(((Byte)obj)).array();
		}
		else if(obj instanceof Float) {
			return ByteBuffer.allocate(4).putFloat(((Float)obj)).array();
		}
		else if(obj instanceof Integer) {
			return ByteBuffer.allocate(4).putInt(((Integer)obj)).array();
		}
		else if(obj instanceof Long) {
			return ByteBuffer.allocate(8).putLong(((Long)obj)).array();
		}
		return null;

	}


	public interface OnConvertFail {
		void onFail(Object value, Class<?> type);
	}


}
