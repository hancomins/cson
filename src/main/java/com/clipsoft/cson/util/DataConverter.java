package com.clipsoft.cson.util;

import com.clipsoft.cson.Base64;
import com.clipsoft.cson.CSONArray;
import com.clipsoft.cson.CSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataConverter {



	public static CSONArray toArray(Object value) {
		if(value instanceof CSONArray) {
			return (CSONArray)value;
		}
		return null; 
	}

	public static CSONObject toObject(Object value) {
		if(value instanceof CSONObject) {
			return (CSONObject)value;
		}
		return null; 
	}

	public static int toInteger(Object value) {
		return toInteger(value, 0);
	}
	
	@SuppressWarnings("UnnecessaryUnboxing")
	public static int toInteger(Object value, int def) {
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
		return toShort(value, (short) 0);
	}

	public static short toShort(Object value, short def) {
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
		return def;
	}

	public static byte toByte(Object value) {
		return toByte(value, (byte) 0);
	}

	public static byte toByte(Object value, byte def) {
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
		return def;
	}


	public static float toFloat(Object value) {
		return toFloat(value, 0);
	}
	
	@SuppressWarnings({"SameParameterValue", "UnnecessaryUnboxing"})
	public static float toFloat(Object value, float def) {
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
		return def;
	}

	public static double toDouble(Object value) {
		return toDouble(value, 0);
	}
	
	@SuppressWarnings("SameParameterValue")
	public static double toDouble(Object value, double def) {
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
				return new BigDecimal(number.toString());
			} else if(type == BigInteger.class) {
				return new BigInteger(number.toString());
			}
		}
		else if(value instanceof String) {
			try {
				Number no = NumberConversionUtil.stringToNumber((String) value);
				return toBoxingNumberOfType(no, type);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}


	public static long toLong(Object value) {
		return toLong(value, 0L);
	}
	
	
	@SuppressWarnings("UnnecessaryUnboxing")
	public static long toLong(Object value, long def) {

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
		return def;
	}

	public static char toChar(Object value) {
		return toChar(value, '\0');
	}
	@SuppressWarnings("UnnecessaryUnboxing")
	public static char toChar(Object value, char def) {
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
  			return Base64.encode(buffer);
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



	public static byte[] toByteArray(Object obj) {
		if(obj == null || obj instanceof NullValue) return null;
		if(obj instanceof byte[]) {
			return (byte[])obj;
		}
		else if(obj instanceof CharSequence) {
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

	
}
