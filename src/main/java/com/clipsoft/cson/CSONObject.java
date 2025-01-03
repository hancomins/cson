package com.clipsoft.cson;

import com.clipsoft.cson.serializer.CSONSerializer;
import com.clipsoft.cson.util.DataConverter;
import com.clipsoft.cson.util.NoSynchronizedStringReader;
import com.clipsoft.cson.util.NullValue;

import java.io.Reader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
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
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().has(key);
		}
		return dataMap.containsKey(key);
	}


	public Map<String, Object> toMap() {
		Map<String, Object> results = new HashMap<>();
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

	public CSONObject(StringFormatOption<?> stringFormatOption) {
		super(ElementType.Object);
		this.defaultJSONOptions = stringFormatOption;
	}



	public CSONObject(String json, StringFormatOption<?> options) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
		parse(reader, options);
		reader.close();
	}
	public CSONObject(Reader reader, StringFormatOption<?> options) {
		super(ElementType.Object);
		parse(reader, options);
	}

	private void parse(Reader stringReader, StringFormatOption<?> options) {
		StringFormatType type = options.getFormatType();
		if(type == StringFormatType.PureJSON) {
			PureJSONParser.parsePureJSON(stringReader, this, options);
		} else {
			new JSONParser(new JSONTokener(stringReader, (JSONOptions)options)).parseObject(this);
		}
	}



	CSONObject(JSONTokener x) throws CSONException {
		super(ElementType.Object);
		defaultJSONOptions = x.getJsonOption();
		new JSONParser(x).parseObject(this);
	}



	public CSONObject() {
		super(ElementType.Object);
	}

	public static CSONObject fromObject(Object obj) {
		return CSONSerializer.toCSONObject(obj);
	}

	@SuppressWarnings("unused")
	public static CSONObject fromObject(Object obj, StringFormatOption<?> stringFormatOption) {
		CSONObject csonObject = CSONSerializer.toCSONObject(obj);
		csonObject.setStringFormatOption(stringFormatOption);
		return csonObject;
	}

	@SuppressWarnings("unused")
	public static <T> T toObject(CSONObject csonObject, Class<T> clazz) {
		return CSONSerializer.fromCSONObject(csonObject, clazz);
	}
	@SuppressWarnings("unused")
	public static <T> T toObject(CSONObject csonObject, T object) {
		return CSONSerializer.fromCSONObject(csonObject, object);
	}


	@SuppressWarnings("unused")
	public boolean remove(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().remove(key);
		}
		return dataMap.remove(key) != null;
	}

	private Object getFromDataMap(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().get(key);
		}
		return dataMap.get(key);

	}

	private void putToDataMap(String key, Object value) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			this.getCsonPath().put(key, value);
			return;
		}
		dataMap.put(key, value);
	}


	public CSONObject put(String key, Object value) {
		if(value == null) {
			putToDataMap(key, NullValue.Instance);
			return this;
		}
		else if(value instanceof Number) {
			putToDataMap(key, value);
		} else if(value instanceof CharSequence) {
			putToDataMap(key, value);
		}  else if(value instanceof CSONElement) {
			if(value == this) {
				value = CSONElement.clone((CSONElement) value);
			}
			putToDataMap(key, value);
		} else if(value instanceof Character || value instanceof Boolean || value instanceof byte[] || value instanceof NullValue) {
			putToDataMap(key, value);
		} else if(value.getClass().isArray()) {
			CSONArray csonArray = new CSONArray();
			int length = Array.getLength(value);
			for(int i = 0; i < length; i++) {
				csonArray.put(Array.get(value, i));
			}
			putToDataMap(key, csonArray);
		} else if(value instanceof Collection) {
			CSONArray csonArray = new CSONArray();
            for(Object obj : (Collection<?>)value) {
				csonArray.put(obj);
			}
			putToDataMap(key, csonArray);
		}
		else if(CSONSerializer.serializable(value.getClass())) {
			CSONObject csonObject = CSONSerializer.toCSONObject(value);
			putToDataMap(key, csonObject);
		}
		else if(isAllowRawValue()) {
			putToDataMap(key, value);
		} else if(isUnknownObjectToString()) {
			putToDataMap(key, value + "");
		}
		return this;
	}

	public <T> T getObject(String key, Class<T> clazz) {
		CSONObject csonObject = getCSONObject(key);
		return CSONSerializer.fromCSONObject(csonObject, clazz);
	}
	@SuppressWarnings("unused")
	public <T> T optObject(String key, Class<T> clazz, T defaultObject) {
		CSONObject obj = optCSONObject(key);
		if(obj == null) return defaultObject;
		try {
			return CSONSerializer.fromCSONObject(obj, clazz);
		} catch (Exception ignored) {
			return defaultObject;
		}
	}




	public String optString(String key) {
		Object obj = getFromDataMap(key);
		return DataConverter.toString(obj);
	}



	@SuppressWarnings("unused")
	public String optString(String key, String def) {
		Object obj = getFromDataMap(key);
		if(obj == null) return def;
		return DataConverter.toString(obj);
	}

	public Set<String> keySet() {
		return this.dataMap.keySet();
	}

	public String getString(String key) {
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toString(obj);
	}


	public boolean isNull(String key) {
		Object obj = getFromDataMap(key);
		return obj instanceof NullValue;
	}

	public Object opt(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public Object get(String key) {
		Object obj =  getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		else if(obj == null) throw new CSONIndexNotFoundException();
		return obj;

	}

	@SuppressWarnings("unused")
	public Object opt(String key, Object def) {
		Object result = getFromDataMap(key);
		if(result instanceof NullValue) return null;
		else if(result == null) return def;
		return result;
	}


	public void merge(CSONObject csonObject) {
		Set<String> keys = csonObject.keySet();
		for(String key : keys) {
			Object value = csonObject.get(key);
			if(value instanceof CSONObject) {
				CSONObject childObject = optCSONObject(key);
				if(childObject == null) {
					childObject = new CSONObject();
					put(key, childObject);
				}
				childObject.merge((CSONObject)value);
			} else if(value instanceof CSONArray) {
				CSONArray childArray = optCSONArray(key);
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
		Object obj = getFromDataMap(key);
		if(obj == null) return def;
		return DataConverter.toInteger(obj,def);
	}

	public int optInteger(String key) {
		return optInteger(key, 0);
	}

	public long optLong(String key, long def) {
		Object obj = getFromDataMap(key);
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
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toInteger(obj);
	}

	public float optFloat(String key) {
		return optFloat(key, Float.NaN);
	}

	public float optFloat(String key, float def) {
		Object obj = getFromDataMap(key);
		if(obj == null) return def;
		return DataConverter.toFloat(obj);
	}

	public double optDouble(String key, double def) {
		Object obj = getFromDataMap(key);
		if(obj == null) return def;
		return DataConverter.toDouble(obj);
	}

	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}


	public float getFloat(String key) {
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toFloat(obj);
	}

	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	public boolean optBoolean(String key, boolean def) {
		Object obj = getFromDataMap(key);
		if(obj  == null) return def;
		return DataConverter.toBoolean(obj);
	}

	public boolean getBoolean(String key) {
		Object obj = getFromDataMap(key);
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
        //return keyValueCommentMap.computeIfAbsent(key, k -> new KeyValueCommentObject());
		// for java1.6
		KeyValueCommentObject keyValueCommentObject = keyValueCommentMap.get(key);
		//noinspection Java8MapApi
		if(keyValueCommentObject == null) {
			keyValueCommentObject = new KeyValueCommentObject();
			keyValueCommentMap.put(key, keyValueCommentObject);
		}
		return keyValueCommentObject;
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
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toLong(obj);
	}

	public double getDouble(String key) {
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toDouble(obj);
	}

	public char getChar(String key) {
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toChar(obj);
	}

	public char optChar(String key, char def) {
		Object obj = getFromDataMap(key);
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
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toShort(obj);
	}

	@SuppressWarnings("unused")
	public short optShort(String key) {
		return optShort(key, (short)0);
	}

	public short optShort(String key, short def) {
		Object obj = getFromDataMap(key);
		if(obj == null) return def;
		return DataConverter.toShort(obj, def);
	}


	public byte getByte(String key) {
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toByte(obj);
	}

	public byte[] getByteArray(String key) {
		Object obj = getFromDataMap(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toByteArray(obj);
	}

	public byte[] optByteArray(String key,byte[] byteArray) {
		Object obj = getFromDataMap(key);
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
		Object obj = getFromDataMap(key);
		if(obj == null) return def;
		return DataConverter.toByte(obj, def);
	}


	@SuppressWarnings("unused")
	public CSONArray optWrapCSONArray(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		} else if(obj == null) {
			return new CSONArray();
		}
		return new CSONArray().put(obj);
	}

	public CSONArray optCSONArray(String key, CSONArray def) {
		Object obj = getFromDataMap(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		}
		return def;
	}

	public CSONArray optCSONArray(String key) {
		return optCSONArray(key, null);
	}



	public CSONArray getCSONArray(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		}
		throw new CSONIndexNotFoundException("key: " + key + ", value: " + obj);
	}

	/**
	 * @deprecated use optWrapCSONArray instead of this method @see optWrapCSONArray
	 */
	@Deprecated
	public CSONArray optWrapArrayf(String key) {
		return optWrapCSONArray(key);
	}

	/**
	 * @deprecated use optCSONArray instead of this method @see optCSONArray
	 */
	@Deprecated
	public CSONArray optArray(String key, CSONArray def) {
		return optCSONArray(key, def);
	}

	/**
	 * @deprecated use optCSONArray instead of this method @see optCSONArray
	 */
	@Deprecated
	public CSONArray optArray(String key) {
		return optCSONArray(key, null);
	}


	/**
	 * @deprecated use optCSONArray instead of this method @see optCSONArray
	 */
	@Deprecated
	public CSONArray getArray(String key) {
		return getCSONArray(key);
	}

	public CSONObject optCSONObject(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		return null;
	}

	@SuppressWarnings("unused")
	public CSONObject optCSONObject(String key, CSONObject def) {
		Object obj = getFromDataMap(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		return def;
	}


	public CSONObject getCSONObject(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		throw new CSONIndexNotFoundException("key: " + key + ", value: " + obj);
	}


	/**
	 * @deprecated use optCSONObject instead of this method @see optCSONObject
	 */
	@Deprecated
	public CSONObject optObject(String key) {
		return optCSONObject(key);
	}

	/**
	 * @deprecated use optCSONObject instead of this method @see optCSONObject
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public CSONObject optObject(String key, CSONObject def) {
		return optCSONObject(key, def);
	}

	/**
	 * @deprecated use getCSONObject instead of this method @see getCSONObject
	 */
	@Deprecated
	public CSONObject getObject(String key) {
		return getCSONObject(key);
	}

	public <T> List<T> getList(String key, Class<T> clazz) {
		CSONArray csonArray = getCSONArray(key);
		return CSONSerializer.csonArrayToList(csonArray, clazz, csonArray.getStringFormatOption(), false, null);
	}

	public <T> List<T> optList(String key, Class<T> clazz) {
		CSONArray csonArray = optCSONArray(key);
		if(csonArray == null) return null;
		return CSONSerializer.csonArrayToList(csonArray, clazz, csonArray.getStringFormatOption(), true, null);
	}


	public <T> List<T> optList(String key, Class<T> clazz, T defaultValue) {
		CSONArray csonArray = optCSONArray(key);
		if(csonArray == null) return null;
		return CSONSerializer.csonArrayToList(csonArray, clazz, csonArray.getStringFormatOption(), true, defaultValue);
	}


	@Override
	public void setStringFormatOption(StringFormatOption<?> defaultJSONOptions) {
		super.setStringFormatOption(defaultJSONOptions);
		for(Entry<String, Object> entry : dataMap.entrySet()) {
			Object obj = entry.getValue();
			if(obj instanceof CSONElement) {
				((CSONElement) obj).setStringFormatOption(defaultJSONOptions);
			}
		}
	}


	@Override
	public String toString() {
		return toString(defaultJSONOptions);
	}

	public String toString(StringFormatOption<?> stringFormatOption) {
		if(stringFormatOption instanceof  JSONOptions) {
			JSONWriter jsonWriter = new JSONWriter((JSONOptions) stringFormatOption);
			write(jsonWriter, true);
			return jsonWriter.toString();
		}
		return toString(JSONOptions.json());

	}

	@Override
	public void clear() {
		dataMap.clear();
	}

	public boolean containsValue(Object value) {
		boolean result = dataMap.containsValue(value);
		if(value == null && !result) {
			return dataMap.containsValue(NullValue.Instance);
		}
		return result;
	}

	public boolean containsValueNoStrict(Object value) {
		Collection<Object> values = dataMap.values();
		return containsNoStrict(values, value);

	}




	/**
	 * @deprecated use toBinary instead of this method @see toBinary
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public byte[] toBytes() {
		return toBinary();
	}

	public byte[] toBinary() {
		BinaryCSONWriter writer = new BinaryCSONWriter();
		write(writer);
		return writer.toByteArray();
	}

	void write(BinaryCSONWriter writer) {
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
			else if(obj instanceof BigInteger) writer.key(key).value(((BigInteger)obj));
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
			else if(obj instanceof BigInteger) writer.key(key).value(obj);
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
			else if (obj == NullValue.Instance) csonObject.put(key,null);
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



	// 0.9.29
	public <T> T toObject(Class<T> clazz) {
		return CSONSerializer.fromCSONObject(this, clazz);
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
            Object value = this.getFromDataMap(key);
            if ((value == null || value instanceof NullValue) && (compareValue != null && !(compareValue instanceof NullValue))) {
                return false;
            } else if (value instanceof CharSequence && (!(compareValue instanceof CharSequence) || !value.toString().equals(compareValue.toString()))) {
                return false;
            } else if (value instanceof Boolean && (!(compareValue instanceof Boolean) || value != compareValue)) {
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
            } else if (value instanceof CSONArray && (!(compareValue instanceof CSONArray) || !(value).equals(compareValue))) {
                return false;
            } else if (value instanceof CSONObject && (!(compareValue instanceof CSONObject) || !(value).equals(compareValue))) {
                return false;
            } else if (value instanceof byte[] && (!(compareValue instanceof byte[]) || !Arrays.equals((byte[]) value, (byte[]) compareValue))) {
                return false;
            }
        }
		return true;
	}

	public Collection<Object> values() {
		return dataMap.values();
	}

	public Iterator<Entry<String, Object>> iteratorEntry() {
		return new Iterator<Entry<String, Object>>() {
			final Iterator<Entry<String, Object>> entryIter = new ArrayList<>(dataMap.entrySet()).iterator();
			String key = null;
			@Override
			public boolean hasNext() {
				return entryIter.hasNext();
			}

			@Override
			public Entry<String, Object> next() {
				Entry<String, Object> entry = entryIter.next();
				Object object = entry.getValue();
				key = entry.getKey();
				if(object == NullValue.Instance) {
					entry.setValue(null);
				}
				return entry;
			}

			@Override
			public void remove() {
				if(key == null) {
					throw new IllegalStateException();
				}
				dataMap.remove(key);
			}
		};



	}



	@Override
	public Iterator<Object> iterator() {



		return new Iterator<Object>() {
			final Iterator<Entry<String, Object>> entryIterator = iteratorEntry();

			@Override
			public boolean hasNext() {
				return entryIterator.hasNext();
			}

			@Override
			public Object next() {
				return entryIterator.next().getValue();
			}

			@Override
			public void remove() {
				entryIterator.remove();
			}
		};
	}
}
