package com.hancomins.cson;

import com.hancomins.cson.serializer.CSONSerializer;
import com.hancomins.cson.util.*;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class CSONObject extends CSONElement implements Cloneable {



	protected Map<String, Object> dataMap = new LinkedHashMap<>();
	private Map<String, KeyValueCommentObject> keyValueCommentMap;



	public static CSONObject fromJson(String value,StringFormatOption<?> stringFormatOption)  {
		return new CSONObject(value, stringFormatOption);
	}

	public static CSONObject fromJson(String value)  {
		return new CSONObject(value, getDefaultStringFormatOption());
	}

	public static CSONObject fromJson(Path path, Charset charset, StringFormatOption<?> stringFormatOption) throws IOException {
		try (Reader reader = Files.newBufferedReader(path, charset)) {
			return new CSONObject(reader, stringFormatOption);
		}
	}

	public static CSONObject fromJson(Path path, Charset charset) throws IOException {
		try (Reader reader = Files.newBufferedReader(path, charset)) {
			return new CSONObject(reader, getDefaultStringFormatOption());
		}
	}

	public static CSONObject fromJson(InputStream inputStream) throws IOException {
		try(InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
			return new CSONObject(inputStreamReader, getDefaultStringFormatOption());
		}
	}


	public static CSONObject fromJson(Reader reader) {
		return new CSONObject(reader, getDefaultStringFormatOption());
	}


	public static CSONObject fromBinaryCSON(Path path) throws IOException {
		try(InputStream inputStream = Files.newInputStream(path)) {
			return fromBinaryCSON(inputStream);
		}
	}

	public static CSONObject fromBinaryCSON(InputStream inputStream) throws IOException {
		try {
			return (CSONObject) BinaryCSONParser.parse(inputStream);
		} catch (DataReadFailException e) {
			Throwable cause = e.getCause();
			if(cause instanceof IOException) {
				throw (IOException)cause;
			}
			throw e;
		}
	}

	public static CSONObject fromBinaryCSON(ByteBuffer byteBuffer) {
		return ((CSONObject)BinaryCSONParser.parse(byteBuffer));
	}


	public static CSONObject fromBinaryCSON(byte[] binaryCSON, int offset, int len) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(binaryCSON, offset, len);
		return fromBinaryCSON(byteBuffer);
	}

	public static CSONObject fromBinaryCSON(byte[] binaryCSON)  {
		return fromBinaryCSON(binaryCSON, 0, binaryCSON.length);
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



	public CSONObject(byte[] binaryCSON) {
		super(ElementType.Object, getDefaultStringFormatOption());
		CSONObject csonObject = (CSONObject) BinaryCSONParser.parse(binaryCSON);
		this.dataMap = csonObject.dataMap;
	}



	public CSONObject(byte[] binaryCSON, int offset, int length) {
		super(ElementType.Object, getDefaultStringFormatOption());
		CSONObject csonObject = (CSONObject) BinaryCSONParser.parse(binaryCSON, offset, length);
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
		super(ElementType.Object, getDefaultStringFormatOption());
		NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
		parse(reader, getStringFormatOption());
		reader.close();
	}

	public CSONObject(StringFormatOption<?> stringFormatOption) {
		super(ElementType.Object, stringFormatOption);
	}

	public CSONObject(String json, StringFormatOption<?> options) {
		super(ElementType.Object, options);
		NoSynchronizedStringReader reader = new NoSynchronizedStringReader(json);
		parse(reader, options);
		reader.close();
	}
	public CSONObject(Reader reader, StringFormatOption<?> options) {
		super(ElementType.Object, options);
		parse(reader, options);
	}

	private void parse(Reader stringReader, StringFormatOption<?> options) {
		StringFormatType type = options.getFormatType();
		if(type == StringFormatType.PureJSON) {
			PureJSONParser.parsePureJSON(stringReader, this, options);
		} else {
			//JSON5Parser.parsePureJSON(stringReader, this, (JSONOptions)options);

			//JSON5ParserX
			//JSON5ParserV parserV = new JSON5ParserV((JSONOptions) options);
			//parserV.parsePureJSON(stringReader, this);
			//parserV.reset();

			//new( (JSONOptions)options).parsePureJSON(stringReader, this);
			JSON5ParserX.parse(stringReader, this, (JSONOptions) options);



		}


	}





	public CSONObject() {
		super(ElementType.Object, getDefaultStringFormatOption());
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
		Object obj = dataMap.get(key);
		copyHeadTailCommentToValueObject(key, obj);
		return obj;
	}

	private void copyHeadTailCommentToValueObject(String key, Object obj) {
		if(keyValueCommentMap != null && obj instanceof CSONElement && !keyValueCommentMap.isEmpty()) {
			KeyValueCommentObject keyValueCommentObject = keyValueCommentMap.get(key);
			CommentObject valueCommentObject = keyValueCommentObject.getValueCommentObject();
			if(valueCommentObject != null) {
				((CSONElement)obj).setTailComment(valueCommentObject.getTrailingComment());
				((CSONElement)obj).setHeadComment(valueCommentObject.getLeadingComment());
			}
		}
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
				value = clone((CSONElement) value);
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




	public Set<String> keySet() {
		return this.dataMap.keySet();
	}


	public boolean isNull(String key) {
		Object obj = getFromDataMap(key);
		return obj instanceof NullValue;
	}



	public Object get(String key) {
		Object obj =  getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		else if(obj == null) throw new CSONIndexNotFoundException(ExceptionMessages.getCSONObjectKeyNotFound(key));
		return obj;
	}

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
		Object obj = get(key);

		T result =  DataConverter.toEnum(enumType, obj);
		if(result == null) {
			throw new CSONException(key, obj, enumType.getTypeName());
		}

		return result;
	}


	@SuppressWarnings("DuplicatedCode")
	public Boolean getBoolean(String key) {
		 Object obj = get(key);
		if(obj instanceof Boolean) {
			return (Boolean)obj;
		} else if("true".equalsIgnoreCase(obj + "")) {
			return true;
		} else if("false".equalsIgnoreCase(obj + "")) {
			return false;
		}
		throw new CSONException(key, obj, boolean.class.getTypeName());
	}



	public byte getByte(String key) {
		Object number = get(key);
        return DataConverter.toByte(number, (byte) 0, ((value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		}));
	}


	public byte[] getByteArray(String key) {
		Object obj = get(key);
		byte[] byteArray = DataConverter.toByteArray(obj);
		if(byteArray == null) {
			throw new CSONException(key, obj, byte[].class.getTypeName());
		}
		return byteArray;
	}


	public char getChar(String key) {
		Object number = get(key);
		return DataConverter.toChar(number, (char)0, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}



	public short getShort(String key) {
		Object number = get(key);
		return DataConverter.toShort(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}


	public int getInt(String key) {
		Object number = get(key);
		return DataConverter.toInteger(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}


	public float getFloat(String key) {
		Object number = get(key);
		return DataConverter.toFloat(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}


	public long getLong(String key) {
		Object number = get(key);
		return DataConverter.toLong(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}



	public double getDouble(String key) {
		Object number = get(key);
		return DataConverter.toDouble(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}

	public String getString(String key) {
		Object obj = get(key);
		if(obj == null) {
			return null;
		}

		return DataConverter.toString(obj);
	}

	@SuppressWarnings("DuplicatedCode")
    public CSONObject getCSONObject(String key) {
		Object obj = get(key);
		if(obj == null) {
			return null;
		}
		CSONObject csonObject = DataConverter.toObject(obj, true);
		if(csonObject == null) {
			throw new CSONException(key, obj, CSONObject.class.getTypeName());
		}
		return csonObject;
	}


	public CSONArray getCSONArray(String key) {
		Object obj = get(key);
		CSONArray csonArray = DataConverter.toArray(obj, true);
		if(csonArray == null) {
			throw new CSONException(key, obj, CSONArray.class.getTypeName());
		}
		return csonArray;
	}

	@SuppressWarnings("DuplicatedCode")
    public <T> List<T> getList(String key, Class<T> valueType) {
		CSONArray csonArray = getCSONArray(key);
		if(csonArray == null) {
			return null;
		}
		try {
			return CSONSerializer.csonArrayToList(csonArray, valueType, csonArray.getStringFormatOption(), false, null);
		} catch (Throwable e) {
			throw new CSONException(key, csonArray, "List<" + valueType.getTypeName() + ">", e);
		}

	}

	public <T> T getObject(String key, Class<T> clazz) {
		CSONObject csonObject = getCSONObject(key);
		try {
			return CSONSerializer.fromCSONObject(csonObject, clazz);
		} catch (Throwable e) {
			throw new CSONException(key, csonObject, clazz.getTypeName(),e);
		}
	}

	public Object opt(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	public boolean optBoolean(String key, boolean def) {
		Object obj = opt(key);
		return DataConverter.toBoolean(obj, def);
	}

	public byte optByte(String key) {
		return optByte(key, (byte)0);
	}

	public byte optByte(String key, byte def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toByte(number, def);
	}


	public byte[] optByteArray(String key) {
		return optByteArray(key, null);
	}

	@SuppressWarnings("unused")
	public byte[] optByteArray(String key,byte[] def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		byte[] buffer =  DataConverter.toByteArray(obj);
		if(buffer == null) {
			return def;
		}
		return buffer;

	}


	public short optShort(String key) {
		return optShort(key, (short)0);
	}

	public short optShort(String key, short def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toShort(number, def);
	}

	@SuppressWarnings("unused")
	public char optChar(String key) {
		return optChar(key, '\0');
	}

	public char optChar(String key, char def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toChar(obj,def);
	}


	public int optInt(String key) {
		return optInt(key, 0);
	}

	public int optInt(String key, int def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toInteger(number, def);

	}

	public float optFloat(String key) {
		return optFloat(key, Float.NaN);
	}


	public float optFloat(String key, float def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toFloat(obj, def);
	}

	public long optLong(String key) {
		return optLong(key, 0);
	}

	public long optLong(String key, long def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toLong(number, def);
	}

	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}

	public double optDouble(String key, double def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toDouble(obj, def);
	}

	public String optString(String key) {
		return optString(key, null);
	}

	public String optString(String key,String def) {
		Object obj = opt(key);
		return obj == null ? def : DataConverter.toString(obj);
	}

	public CSONArray optCSONArray(String key) {
		return optCSONArray(key, null);
	}

	public CSONArray optCSONArray(String key, CSONArray def) {
		@SuppressWarnings("DuplicatedCode")
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		CSONArray csonArray = DataConverter.toArray(obj, true);
		if(csonArray == null) {
			return def;
		}
		return csonArray;
	}

	public CSONArray optWrapCSONArray(String key) {
		@SuppressWarnings("DuplicatedCode")
		Object object = opt(key);
		if(object == null) {
			return new CSONArray();
		}
		CSONArray csonArray = DataConverter.toArray(object, true);
		if(csonArray == null) {
			return new CSONArray().put(object);
		}
		return csonArray;
	}

	public CSONObject optCSONObject(String key) {
		return optCSONObject(key, null);
	}

	public CSONObject optCSONObject(String key, CSONObject def) {
		@SuppressWarnings("DuplicatedCode")
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		CSONObject csonObject = DataConverter.toObject(obj, true);
		if(csonObject == null) {
			return def;
		}
		return csonObject;
	}


	public <T> T optObject(String key, Class<T> clazz) {
		return optObject(key, clazz, null);
	}

	public <T> T optObject(String key, Class<T> clazz, T defaultObject) {
		try {
			CSONObject csonObject = optCSONObject(key);
			return CSONSerializer.fromCSONObject(csonObject, clazz);
		} catch (Exception e) {
			return defaultObject;
		}
	}


	public <T> List<T> optList(String key, Class<T> valueType) {
		return optList(key, valueType, null);
	}

	public <T> List<T> optList(String key, Class<T> valueType, T defaultValue) {
		try {
			CSONArray csonArray = optCSONArray(key);
			if(csonArray == null) {
				return null;
			}
			return CSONSerializer.csonArrayToList(csonArray, valueType, csonArray.getStringFormatOption(), true, defaultValue);
		} catch (Exception e) {
			if(defaultValue != null) {
				List<T> result = new ArrayList<>();
				result.add(defaultValue);
				return result;
			} else {
				return Collections.EMPTY_LIST;
			}
		}
	}




	/**
	 * 다른 CSONObject를 병합한다.
	 * @param csonObject 병합할 CSONObject
	 */
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

	/**
	 * 교집합을 반환한다.
	 * @param csonObject 교집합을 구할 CSONObject
	 * @return 교집합
	 */
	public CSONObject intersect(CSONObject csonObject) {
		CSONObject result = new CSONObject();
		Set<String> keys = csonObject.keySet();
		for(String key : keys) {
			Object value = csonObject.get(key);
			if(value instanceof CSONObject) {
				CSONObject childObject = optCSONObject(key);
				if(childObject == null) {
					continue;
				}
				result.put(key, childObject.intersect((CSONObject)value));
			} else if(value instanceof CSONArray) {
				CSONArray childArray = optCSONArray(key);
				if(childArray == null) {
					continue;
				}
				result.put(key, childArray.intersect((CSONArray)value));
			} else {
				if(has(key)) {
					result.put(key, value);
				}
			}
		}
		return result;
	}

	/**
	 * 교집합을 제외한 부분을 반환한다.
	 * @param csonObject 교집합을 제외할 CSONObject
	 * @return 교집합을 제외한 부분
	 */
	public CSONObject subtractIntersection(CSONObject csonObject) {
		CSONObject result = new CSONObject();
		Set<String> keys = csonObject.keySet();
		for(String key : keys) {
			Object value = csonObject.get(key);
			if(value instanceof CSONObject) {
				CSONObject childObject = optCSONObject(key);
				if(childObject == null) {
					continue;
				}
				result.put(key, childObject.subtractIntersection((CSONObject)value));
			} else if(value instanceof CSONArray) {
				CSONArray childArray = optCSONArray(key);
				if(childArray == null) {
					continue;
				}
				result.put(key, childArray.subtractIntersection((CSONArray)value));
			} else {
				if(!has(key)) {
					result.put(key, value);
				}
			}
		}
		return result;

	}



	public boolean isEmpty() {
		return dataMap.isEmpty();
	}




	/**
	 * @deprecated use {@link #optInt(String)} instead
	 *
	 */
	@Deprecated
	public int getInteger(String key) {
		return getInt(key);
	}




	KeyValueCommentObject getKeyCommentObject(String key) {
		if(keyValueCommentMap == null) return null;
		return keyValueCommentMap.get(key);
	}

	public CSONObject setCommentForKey(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(!keyValueCommentObject.isNullOrEmptyKeyCommentObject()) {
			keyValueCommentObject.getKeyCommentObject().setLeadingComment(comment);
		} else {
			keyValueCommentObject.setKeyCommentObject(new CommentObject(comment, null));
		}
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentForValue(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(!keyValueCommentObject.isNullOrEmptyValueCommentObject()) {
			keyValueCommentObject.getValueCommentObject().setLeadingComment(comment);
		} else {
			keyValueCommentObject.setValueCommentObject(new CommentObject(comment, null));
		}
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentAfterValue(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(!keyValueCommentObject.isNullOrEmptyValueCommentObject()) {
			keyValueCommentObject.getValueCommentObject().setTrailingComment(comment);
		} else {
			keyValueCommentObject.setValueCommentObject( new CommentObject(null, comment));
		}
		return this;
	}


	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentAfterKey(String key, String comment) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(!keyValueCommentObject.isNullOrEmptyKeyCommentObject()) {
			keyValueCommentObject.getKeyCommentObject().setTrailingComment(comment);
		} else {
			keyValueCommentObject.setKeyCommentObject(new CommentObject(null, comment));
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
		return keyValueCommentObject.isNullOrEmptyKeyCommentObject() ? null : keyValueCommentObject.getKeyCommentObject().getLeadingComment();
	}

	public String getCommentAfterKey(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.isNullOrEmptyKeyCommentObject() ? null : keyValueCommentObject.getKeyCommentObject().getTrailingComment();
	}

	@SuppressWarnings("unused")
	public String getCommentForValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.isNullOrEmptyValueCommentObject() ? null : keyValueCommentObject.getValueCommentObject().getLeadingComment();
	}

	@SuppressWarnings("unused")
	public String getCommentAfterValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.isNullOrEmptyValueCommentObject() ? null : keyValueCommentObject.getValueCommentObject().getTrailingComment();
	}



	protected void setCommentObjects(String key, CommentObject keyCommentObject, CommentObject valueCommentObject) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyCommentObject != null && keyCommentObject.isCommented()) {
            do {
                keyValueCommentObject.setKeyCommentObject(keyCommentObject);
            } while (keyValueCommentObject.isNullOrEmptyKeyCommentObject());
		}
		keyValueCommentObject.setValueCommentObject(valueCommentObject);
	}


	protected CommentObject getCommentObjectOfKey(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.getKeyCommentObject();
	}

	protected CommentObject getCommentObjectOfValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getKeyCommentObject(key);
		if(keyValueCommentObject == null) return null;
		return keyValueCommentObject.getValueCommentObject();
	}


	@SuppressWarnings("unused")
	protected CommentObject getOrCreateCommentObjectOfValue(String key) {
		KeyValueCommentObject keyValueCommentObject = getOrCreateCommentObject(key);
		if(keyValueCommentObject.isNullOrEmptyValueCommentObject()) {
			keyValueCommentObject.setValueCommentObject(new CommentObject());
		}
		return keyValueCommentObject.getValueCommentObject();
	}

	private KeyValueCommentObject getOrCreateCommentObject(String key) {
		if(keyValueCommentMap == null) {
			keyValueCommentMap = new LinkedHashMap<>();
		}
        return keyValueCommentMap.computeIfAbsent(key, k -> new KeyValueCommentObject());
	}


	public String getCommentBeforeKey(String key) {
		CommentObject commentObject = getCommentObjectOfKey(key);
		return commentObject == null ? null : commentObject.getLeadingComment();
	}

	public String getCommentBeforeValue(String key) {
		CommentObject commentObject = getCommentObjectOfValue(key);
		return commentObject == null ? null : commentObject.getLeadingComment();
	}

	@Deprecated
	public String getCommentOfKey(String key) {
		CommentObject commentObject = getCommentObjectOfKey(key);
		return commentObject == null ? null : commentObject.getLeadingComment();
	}

	@Deprecated
	public String getCommentOfValue(String key) {
		CommentObject commentObject = getCommentObjectOfValue(key);
		return commentObject == null ? null : commentObject.getLeadingComment();
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
		return toString(getStringFormatOption());
	}

	public String toString(StringFormatOption<?> stringFormatOption) {
		if(stringFormatOption instanceof JSONOptions) {
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

	/**
	 * @deprecated use toBinary instead of this method @see toCSONBinary
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public byte[] toBinary() {
		return toCSONBinary();
	}

	@Override
	public byte[] toCSONBinary() {
		try {
			BinaryCSONWriter writer = new BinaryCSONWriter();
			write(writer);
			return writer.toByteArray();
		}
		// 사실상 발생하지 않는다.
		catch (IOException e) {
			throw new CSONException(e);
		}
	}

	@Override
	public void writeCSONBinary(OutputStream outputStream) throws IOException {
		BinaryCSONWriter writer = new BinaryCSONWriter(outputStream);
		write(writer);
	}

	void write(BinaryCSONWriter writer) throws IOException {
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


	Map<String, KeyValueCommentObject> getKeyValueCommentMap() {
		return keyValueCommentMap;
	}


	@Override
	protected void write(JSONWriter writer, boolean root) {
		JSONWriter.writeJSONElement(this, writer);
		/*Iterator<Entry<String, Object>> iter = dataMap.entrySet().iterator();
		boolean isComment = writer.isComment() && keyValueCommentMap != null;

		// root 오브젝트가 아닌 경우에는 주석을 무시한다.
		if(root) {
			writer.writeComment(getHeadComment(), false,"","\n" );
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
			writer.writeComment(getTailComment(), false,"\n","" );
		}*/

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
