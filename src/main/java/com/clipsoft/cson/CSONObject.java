package com.clipsoft.cson;




import com.clipsoft.cson.util.NoSynchronizedStringReader;

import java.io.Reader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class CSONObject extends CSONElement implements Cloneable {



	protected LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
	private LinkedHashMap<String, KeyValueCommentObject> keyValueCommentMap;





	public CSONObject(byte[] binaryCSON) {
		super(ElementType.Object);
		CSONObject csonObject = (CSONObject) BinaryCSONParser.parse(binaryCSON);
		this.dataMap = csonObject.dataMap;
	}



	public CSONObject(byte[] buffer, int offset, int length) {
		super(ElementType.Object);
		CSONObject csonObject = (CSONObject) BinaryCSONParser.parse(buffer, offset, length);
		this.dataMap = csonObject.dataMap;
	}



	protected Set<Entry<String, Object>> entrySet() {
		return this.dataMap.entrySet();
	}


	@SuppressWarnings("unused")
	public boolean has(String key) {
		return dataMap.containsKey(key);
	}


	public Map<String, Object> toMap() {
		Map<String, Object> results = new HashMap<String, Object>();
		for (Entry<String, Object> entry : this.entrySet()) {
			Object value;
			if (entry.getValue() == null) {
				value = null;
			} else if (entry.getValue() instanceof CSONObject) {
				value = ((CSONObject) entry.getValue()).toMap();
			} else if (entry.getValue() instanceof CSONArray) {
				value = ((CSONArray) entry.getValue()).toList();
			} else {
				value = entry.getValue();
			}
			results.put(entry.getKey(), value);
		}
		return results;
	}

	public CSONObject(String json) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
		parse(reader, defaultJSONOptions);
		reader.close();
	}

	public CSONObject(StringFormatOption stringFormatOption) {
		super(ElementType.Object);
		this.defaultJSONOptions = stringFormatOption;
	}



	public CSONObject(String json, StringFormatOption options) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
		parse(reader, options);
		reader.close();
	}
	public CSONObject(Reader reader, StringFormatOption options) {
		super(ElementType.Object);
		parse(reader, options);
	}

	private void parse(Reader stringReader, StringFormatOption options) {
		StringFormatType type = options.getFormatType();
		if(type == StringFormatType.PureJSON) {
			PureJSONParser.parsePureJSON(stringReader, this);
		} else {
			new JSONParser(new JSONTokener(stringReader, (JSONOptions)options)).parseObject(this);
		}
	}



	protected CSONObject(JSONTokener x) throws CSONException {
		super(ElementType.Object);
		defaultJSONOptions = x.getJsonOption();
		new JSONParser(x).parseObject(this);
	}



	public CSONObject() {
		super(ElementType.Object);
	}

	public CSONObject(JSONOptions jsonOptions) {
		super(ElementType.Object);
		this.defaultJSONOptions = jsonOptions;
	}

	@SuppressWarnings("unused")
	public boolean remove(String key) {
		return dataMap.remove(key) != null;
	}


	public CSONObject put(String key, Object value) {
		if(value == null) {
			dataMap.put(key, NullValue.Instance);
			return this;
		}
		else if(value instanceof Number) {
			dataMap.put(key, value);
		} else if(value instanceof String) {
			dataMap.put(key, value);
		} else if(value instanceof CharSequence) {
			dataMap.put(key, value);
		} else if(value instanceof CSONElement) {
			if(value == this) {
				value = CSONElement.clone((CSONElement) value);
			}
			dataMap.put(key, value);
		} else if(value instanceof Character || value instanceof Boolean || value instanceof byte[] || value instanceof NullValue) {
			dataMap.put(key, value);
		} else if(value.getClass().isArray()) {
			CSONArray csonArray = new CSONArray();
			int length = Array.getLength(value);
			for(int i = 0; i < length; i++) {
				csonArray.put(Array.get(value, i));
			}
			dataMap.put(key, csonArray);
		}
		else if(isAllowRawValue()) {
			dataMap.put(key, value);
		} else if(isUnknownObjectToString()) {
			dataMap.put(key, value + "");
		}



		return this;
	}


	public String optString(String key) {
		Object obj = dataMap.get(key);
		return DataConverter.toString(obj);
	}



	@SuppressWarnings("unused")
	public String optString(String key, String def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toString(obj);
	}

	public Set<String> keySet() {
		return this.dataMap.keySet();
	}

	public String getString(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toString(obj);
	}


	public boolean isNull(String key) {
		Object obj = dataMap.get(key);
		return obj instanceof NullValue;
	}

	public Object opt(String key) {
		Object obj = dataMap.get(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public Object get(String i) {
		Object obj =  dataMap.get(i);
		if(obj instanceof NullValue) return null;
		else if(obj == null) throw new CSONIndexNotFoundException();
		return obj;

	}

	@SuppressWarnings("unused")
	public Object opt(String key, Object def) {
		Object result = dataMap.get(key);
		if(result instanceof NullValue) return null;
		else if(result == null) return def;
		return result;
	}


	public void merge(CSONObject csonObject) {
		Set<String> keys = csonObject.keySet();
		for(String key : keys) {
			Object value = csonObject.get(key);
			if(value instanceof CSONObject) {
				CSONObject childObject = optObject(key);
				if(childObject == null) {
					childObject = new CSONObject();
					put(key, childObject);
				}
				childObject.merge((CSONObject)value);
			} else if(value instanceof CSONArray) {
				CSONArray childArray = optArray(key);
				if(childArray == null) {
					childArray = new CSONArray();
					put(key, childArray);
				}
				childArray.merge((CSONArray)value);
			} else {
				put(key, value);
			}
		}
	}



	public int optInteger(String key, int def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toInteger(obj,def);
	}

	public int optInteger(String key) {
		return optInteger(key, 0);
	}

	public long optLong(String key, long def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toLong(obj,def);
	}

	public boolean isEmpty() {
		return dataMap.isEmpty();
	}

	@SuppressWarnings("unused")
	public long optLong(String key) {
		return optLong(key, 0);

	}

	public int getInt(String key) {
		return getInteger(key);
	}

	public int optInt(String key) {
		return optInteger(key);
	}

	@SuppressWarnings("unused")
	public int optInt(String key,int def) {
		return optInteger(key, def);
	}

	public int getInteger(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toInteger(obj);
	}

	public float optFloat(String key) {
		return optFloat(key, Float.NaN);
	}

	public float optFloat(String key, float def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toFloat(obj);
	}

	public double optDouble(String key, double def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toDouble(obj);
	}

	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}


	public float getFloat(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toFloat(obj);
	}

	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	public boolean optBoolean(String key, boolean def) {
		Object obj = dataMap.get(key);
		if(obj  == null) return def;
		return DataConverter.toBoolean(obj);
	}

	public boolean getBoolean(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toBoolean(obj);
	}


	KeyValueCommentObject getKeyCommentObject(String key) {
		if(keyValueCommentMap == null) return null;
		return keyValueCommentMap.get(key);
	}

	public CSONObject setCommentForKey(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyValueCommentObject.keyCommentObject != null) {
			keyValueCommentObject.keyCommentObject.setBeforeComment(comment);
		} else {
			keyValueCommentObject.keyCommentObject = new CommentObject(comment, null);
		}
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentForValue(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyValueCommentObject.valueCommentObject != null) {
			keyValueCommentObject.valueCommentObject.setBeforeComment(comment);
		} else {
			keyValueCommentObject.valueCommentObject = new CommentObject(comment, null);
		}
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentAfterValue(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyValueCommentObject.valueCommentObject != null) {
			keyValueCommentObject.valueCommentObject.setAfterComment(comment);
		} else {
			keyValueCommentObject.valueCommentObject = new CommentObject(null, comment);
		}
		return this;
	}


	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentAfterKey(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyValueCommentObject.keyCommentObject != null) {
			keyValueCommentObject.keyCommentObject.setAfterComment(comment);
		} else {
			keyValueCommentObject.keyCommentObject = new CommentObject(null, comment);
		}
		return this;
	}

	@SuppressWarnings("unused")
	public CSONObject setComment(String key, String comment) {
		return setCommentForKey(key, comment);
	}

	public String getCommentForKey(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.keyCommentObject == null ? null : keyValueCommentObject.keyCommentObject.getBeforeComment();
	}

	public String getCommentAfterKey(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.keyCommentObject == null ? null : keyValueCommentObject.keyCommentObject.getAfterComment();
	}

	@SuppressWarnings("unused")
	public String getCommentForValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.valueCommentObject == null ? null : keyValueCommentObject.valueCommentObject.getBeforeComment();
	}

	@SuppressWarnings("unused")
	public String getCommentAfterValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.valueCommentObject == null ? null : keyValueCommentObject.valueCommentObject.getAfterComment();
	}



	protected void setCommentObjects(String key, CommentObject keyCommentObject, CommentObject valueCommentObject) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		keyValueCommentObject.keyCommentObject = keyCommentObject;
		keyValueCommentObject.valueCommentObject = valueCommentObject;
	}


	protected CommentObject getCommentObjectOfKey(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.keyCommentObject;
	}

	protected CommentObject getCommentObjectOfValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.valueCommentObject;
	}


	@SuppressWarnings("unused")
	protected CommentObject getOrCreateCommentObjectOfValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyValueCommentObject.valueCommentObject == null) {
			keyValueCommentObject.valueCommentObject = new CommentObject();
		}
		return keyValueCommentObject.valueCommentObject;
	}

	private KeyValueCommentObject getOrCreateCommentObject(String key) {
		if(keyValueCommentMap == null) {
			keyValueCommentMap = new LinkedHashMap<>();
		}
		KeyValueCommentObject commentObject = keyValueCommentMap.get(key);
		if(commentObject == null) {
			commentObject = new KeyValueCommentObject();
			keyValueCommentMap.put(key, commentObject);
		}
		return commentObject;
	}


	public String getCommentOfKey(String key) {
		CommentObject commentObject = getCommentObjectOfKey(key);
		return commentObject == null ? null : commentObject.getBeforeComment();
	}

	public String getCommentOfValue(String key) {
		CommentObject commentObject = getCommentObjectOfValue(key);
		return commentObject == null ? null : commentObject.getBeforeComment();
	}









	public long getLong(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toLong(obj);
	}

	public double getDouble(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toDouble(obj);
	}

	public char getChar(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toChar(obj);
	}

	public char optChar(String key, char def) {
		Object obj = dataMap.get(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toChar(obj);
	}

	@SuppressWarnings("unused")
	public char optChar(String key) {
		return optChar(key, '\0');
	}


	public short getShort(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toShort(obj);
	}

	@SuppressWarnings("unused")
	public short optShort(String key) {
		return optShort(key, (short)0);
	}

	public short optShort(String key, short def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toShort(obj, def);
	}


	public byte getByte(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toByte(obj);
	}

	public byte[] getByteArray(String key) {
		Object obj = dataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toByteArray(obj);
	}

	public byte[] optByteArray(String key,byte[] byteArray) {
		Object obj = dataMap.get(key);
		if(obj == null) return byteArray;
		return DataConverter.toByteArray(obj);
	}

	@SuppressWarnings("unused")
	public byte[] optByteArray(String key) {
		return optByteArray(key, null);
	}

	public byte optByte(String key) {
		return optByte(key, (byte)0);
	}

	public byte optByte(String key, byte def) {
		Object obj = dataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toByte(obj, def);
	}


	@SuppressWarnings("unused")
	public CSONArray optWrapArray(String key) {
		Object obj = dataMap.get(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		} else if(obj == null) {
			return new CSONArray();
		}
		return new CSONArray().put(obj);
	}

	public CSONArray optArray(String key, CSONArray def) {
		Object obj = dataMap.get(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		}
		return def;
	}

	public CSONArray optArray(String key) {
		return optArray(key, null);
	}


	public CSONArray getArray(String key) {
		Object obj = dataMap.get(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		}
		throw new CSONIndexNotFoundException();
	}

	public CSONObject optObject(String key) {
		Object obj = dataMap.get(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		return null;
	}

	@SuppressWarnings("unused")
	public CSONObject optObject(String key, CSONObject def) {
		Object obj = dataMap.get(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		return def;
	}


	public CSONObject getObject(String key) {
		Object obj = dataMap.get(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		throw new CSONIndexNotFoundException(key);
	}

	@Override
	public String toString() {
		return toString(defaultJSONOptions);
	}

	public String toString(StringFormatOption stringFormatOption) {
		if(stringFormatOption instanceof  JSONOptions) {
			JSONWriter jsonWriter = new JSONWriter((JSONOptions) stringFormatOption);
			write(jsonWriter, true);
			return jsonWriter.toString();
		}
		return toString(JSONOptions.json());

	}

	@Deprecated
	/**
	 * @Deprecated use toBytes() instead
	 */
	public byte[] toBytes() {
		return toBinary();
	}

	public byte[] toBinary() {
		BinaryCSONWriter writer = new BinaryCSONWriter();
		write(writer);
		return writer.toByteArray();
	}

	protected void write(BinaryCSONWriter writer) {
		Iterator<Entry<String, Object>> iter = dataMap.entrySet().iterator();
		writer.openObject();
		while(iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object obj = entry.getValue();
			if(obj == null || obj instanceof NullValue) writer.key(key).nullValue();
			else if(obj instanceof CSONArray)  {
				writer.key(key);
				((CSONArray)obj).write(writer);
			}
			else if(obj instanceof CSONObject)  {
				writer.key(key);
				((CSONObject)obj).write(writer);
			}
			else if(obj instanceof Byte)	writer.key(key).value((Byte)obj);
			else if(obj instanceof Short)	writer.key(key).value((Short)obj);
			else if(obj instanceof Character) writer.key(key).value((Character)obj);
			else if(obj instanceof Integer) writer.key(key).value((Integer)obj);
			else if(obj instanceof Float) writer.key(key).value((Float)obj);
			else if(obj instanceof Long) writer.key(key).value((Long)obj);
			else if(obj instanceof Double) writer.key(key).value((Double)obj);
			else if(obj instanceof String) writer.key(key).value((String)obj);
			else if(obj instanceof Boolean) writer.key(key).value((Boolean)obj);
			else if(obj instanceof BigDecimal) writer.key(key).value(((BigDecimal)obj));
			else if(obj instanceof byte[]) writer.key(key).value((byte[])obj);
		}
		writer.closeObject();
	}

	@Override
	protected void write(JSONWriter writer, boolean root) {
		Iterator<Entry<String, Object>> iter = dataMap.entrySet().iterator();
		boolean isComment = writer.isComment() && keyValueCommentMap != null;

		// root 오브젝트가 아닌 경우에는 주석을 무시한다.
		if(root) {
			writer.writeComment(getCommentThis(), false,"","\n" );
		}
		writer.openObject();
		while(iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object obj = entry.getValue();
			KeyValueCommentObject keyValueCommentObject = isComment ? keyValueCommentMap.get(key) : null;
			writer.nextCommentObject(keyValueCommentObject == null ? null : keyValueCommentObject.keyCommentObject);
			writer.nextCommentObject(keyValueCommentObject == null ? null : keyValueCommentObject.valueCommentObject);

			if (obj == null || obj instanceof NullValue) writer.key(key).nullValue();
			else if (obj instanceof CSONElement) {
				//writer.key(key);
				//((CSONElement) obj).write(writer, false);
				writer.key(key).value((CSONElement)obj);
			} else if (obj instanceof Byte) {
				writer.key(key).value((byte) obj);
			} else if (obj instanceof Short) writer.key(key).value((short) obj);
			else if (obj instanceof Character) writer.key(key).value((char) obj);
			else if (obj instanceof Integer) writer.key(key).value((int) obj);
			else if (obj instanceof Float) writer.key(key).value((float) obj);
			else if (obj instanceof Long) writer.key(key).value((long) obj);
			else if (obj instanceof Double) writer.key(key).value((double) obj);
			else if (obj instanceof String) writer.key(key).value((String) obj);
			else if (obj instanceof Boolean) writer.key(key).value((boolean) obj);
			else if (obj instanceof BigDecimal) writer.key(key).value(obj);
			else if (obj instanceof byte[]) writer.key(key).value((byte[]) obj);
			else if (isAllowRawValue()) {
				writer.key(key).value(obj.toString());
			}

		}
		writer.closeObject();
		if(root) {
			writer.writeComment(getCommentAfterThis(), false,"\n","" );
		}

	}


	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public CSONObject clone() {

		CSONObject csonObject = new CSONObject();

        for (Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();
            if (obj instanceof CSONArray) csonObject.put(key, ((CSONArray) obj).clone());
            else if (obj instanceof CSONObject) csonObject.put(key, ((CSONObject) obj).clone());
            else if (obj instanceof CharSequence) csonObject.put(key, ((CharSequence) obj).toString());
            else if (obj instanceof byte[]) {
                byte[] bytes = (byte[]) obj;
                byte[] newBytes = new byte[bytes.length];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                csonObject.put(key, newBytes);
            } else csonObject.put(key, obj);
        }
		return csonObject;
	}

	public int size() {
		return dataMap.size();
	}




	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof CSONObject)) {
			return false;
		}
		CSONObject csonObject = (CSONObject)obj;
		if(csonObject.size() != size()) {
			return false;
		}
        for (Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object compareValue = entry.getValue();
            Object value = this.dataMap.get(key);
            if ((value == null || value instanceof NullValue) && (compareValue != null && !(compareValue instanceof NullValue))) {
                return false;
            } else if (value instanceof CharSequence && (!(compareValue instanceof CharSequence) || !value.toString().equals(compareValue.toString()))) {
                return false;
            } else if (value instanceof Boolean && (!(compareValue instanceof Boolean) || (Boolean) value != (Boolean) compareValue)) {
                return false;
            } else if (value instanceof Number) {
                boolean valueIsFloat = (value instanceof Float || value instanceof Double || compareValue instanceof BigDecimal);
                boolean compareValueIsFloat = (compareValue instanceof Float || compareValue instanceof Double || compareValue instanceof BigDecimal);
                if (valueIsFloat != compareValueIsFloat) {
                    return false;
                }
                BigDecimal v1 = BigDecimal.valueOf(((Number) value).doubleValue());
                BigDecimal v2 = BigDecimal.valueOf(((Number) compareValue).doubleValue());
                if (v1.compareTo(v2) != 0) {
                    return false;
                }
            } else if (value instanceof CSONArray && (!(compareValue instanceof CSONArray) || !((CSONArray) value).equals(compareValue))) {
                return false;
            } else if (value instanceof CSONObject && (!(compareValue instanceof CSONObject) || !((CSONObject) value).equals(compareValue))) {
                return false;
            } else if (value instanceof byte[] && (!(compareValue instanceof byte[]) || !Arrays.equals((byte[]) value, (byte[]) compareValue))) {
                return false;
            }
        }
		return true;
	}




}
