package com.hancomins.cson;


import com.hancomins.cson.format.*;
import com.hancomins.cson.format.cson.BinaryCSONParser;
import com.hancomins.cson.format.cson.BinaryCSONWriter;
import com.hancomins.cson.format.json.JSON5Writer;
import com.hancomins.cson.util.DataConverter;
import com.hancomins.cson.util.NoSynchronizedStringReader;
import com.hancomins.cson.util.NullValue;
import com.hancomins.cson.options.*;
import com.hancomins.cson.format.json.JSON5Parser;
import com.hancomins.cson.serializer.CSONSerializer;


import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class CSONObject extends CSONElement implements Cloneable {


	protected Map<String, java.lang.Object> dataMap = new LinkedHashMap<>();
	private Map<String, CommentObject> keyValueCommentMap;




	public static CSONObject fromObject(java.lang.Object obj) {
		return CSONSerializer.toCSONObject(obj);
	}

	@SuppressWarnings("unused")
	public static CSONObject fromObject(java.lang.Object obj, WritingOptions<?> writingOptions) {
		CSONObject csonObject = CSONSerializer.toCSONObject(obj);
		csonObject.setWritingOptions(writingOptions);
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
		super(ElementType.Object);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryCSON);
		BinaryCSONParser parser = new BinaryCSONParser(CSONObject.KeyValueDataContainerFactory, CSONArray.ArrayDataContainerFactory);
		try {
			parser.parse(byteArrayInputStream, new CSONObject.CSONKeyValueDataContainer(this));
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}



	public CSONObject(byte[] binaryCSON, int offset, int length) {
		super(ElementType.Object);
		CSONObject csonObject = (CSONObject) BinaryCSONParser.parse(binaryCSON, offset, length);
		this.dataMap = csonObject.dataMap;
	}



	public Set<Entry<String, java.lang.Object>> entrySet() {
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


	public Map<String, java.lang.Object> toMap() {
		Map<String, java.lang.Object> results = new HashMap<>();
		for (Entry<String, java.lang.Object> entry : this.entrySet()) {
			java.lang.Object value;
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
		parse(reader, ParsingOptions.getDefaultParsingOptions());
		reader.close();
	}

	public CSONObject(Reader reader) {
		super(ElementType.Object);
		parse(reader, ParsingOptions.getDefaultParsingOptions());
	}


	public CSONObject(WritingOptions<?> writingOptions) {
		super(ElementType.Object, writingOptions);
	}

	public CSONObject(String json, ParsingOptions<?> options) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader = new NoSynchronizedStringReader(json);
		parse(reader, options);
		reader.close();
	}

	public CSONObject(String json, WritingOptions<?> writingOptions) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader = new NoSynchronizedStringReader(json);
		parse(reader, ParsingOptions.getDefaultParsingOptions());
		reader.close();
		this.setWritingOptions(writingOptions);

	}

	public CSONObject(Reader reader, ParsingOptions<?> options) {
		super(ElementType.Object);
		parse(reader, options);
	}

	private void parse(Reader stringReader, ParsingOptions<?> options) {
		StringFormatType type = options.getFormatType();
		/*if(JsonParsingOptions.isPureJSONOption(options)) {
			PureJSONParser.parsePureJSON(stringReader, this, options);
		} else {*/
			//JSON5Parser.parsePureJSON(stringReader, this, (JsonParsingOptions)options);

			//JSON5Parser
			//JSON5ParserV parserV = new JSON5ParserV((JsonParsingOptions) options);
			//parserV.parsePureJSON(stringReader, this);
			//parserV.reset();

			//new( (JsonParsingOptions)options).parsePureJSON(stringReader, this);

		JSON5Parser.parse(stringReader, (JsonParsingOptions) options, new CSONKeyValueDataContainer(this), CSONObject.KeyValueDataContainerFactory, CSONArray.ArrayDataContainerFactory);



		//}


	}





	public CSONObject() {
		super(ElementType.Object);
	}



	@SuppressWarnings("unused")
	public boolean remove(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().remove(key);
		}
		return dataMap.remove(key) != null;
	}

	private java.lang.Object getFromDataMap(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().get(key);
		}
		java.lang.Object obj = dataMap.get(key);
		copyHeadTailCommentToValueObject(key, obj);
		return obj;
	}

	private void copyHeadTailCommentToValueObject(String key, java.lang.Object obj) {
		if(keyValueCommentMap != null && obj instanceof CSONElement && !keyValueCommentMap.isEmpty()) {
			CommentObject commentObject = keyValueCommentMap.get(key);
			if(commentObject == null) return;
			CommentObject copiedCommentObject = commentObject.copy();
			((CSONElement)obj).setHeaderComment(copiedCommentObject.getComment(CommentPosition.BEFORE_VALUE));
			((CSONElement)obj).setFooterComment(copiedCommentObject.getComment(CommentPosition.AFTER_VALUE));
		}
	}



	private void putToDataMap(String key, java.lang.Object value) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			this.getCsonPath().put(key, value);
			return;
		}
		dataMap.put(key, value);
	}


	public void putByParser(String key, java.lang.Object value) {
		dataMap.put(key, value);
	}




	public CSONObject put(String key, java.lang.Object value) {
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
			for(java.lang.Object obj : (Collection<?>)value) {
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
		java.lang.Object obj = getFromDataMap(key);
		return obj instanceof NullValue;
	}



	public java.lang.Object get(String key) {
		java.lang.Object obj =  getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		else if(obj == null) throw new CSONIndexNotFoundException(ExceptionMessages.getCSONObjectKeyNotFound(key));
		return obj;
	}

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
		java.lang.Object obj = get(key);
		T result =  DataConverter.toEnum(enumType, obj);
		if(result == null) {
			throw new CSONException(key, obj, enumType.getTypeName());
		}

		return result;
	}


	@SuppressWarnings("DuplicatedCode")
	public Boolean getBoolean(String key) {
		 java.lang.Object obj = get(key);
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
		java.lang.Object number = get(key);
        return DataConverter.toByte(number, (byte) 0, ((value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		}));
	}


	public byte[] getByteArray(String key) {
		java.lang.Object obj = get(key);
		byte[] byteArray = DataConverter.toByteArray(obj);
		if(byteArray == null) {
			throw new CSONException(key, obj, byte[].class.getTypeName());
		}
		return byteArray;
	}


	public char getChar(String key) {
		java.lang.Object number = get(key);
		return DataConverter.toChar(number, (char)0, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}



	public short getShort(String key) {
		java.lang.Object number = get(key);
		return DataConverter.toShort(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}


	public int getInt(String key) {
		java.lang.Object number = get(key);
		return DataConverter.toInteger(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}


	public float getFloat(String key) {
		java.lang.Object number = get(key);
		return DataConverter.toFloat(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}


	public long getLong(String key) {
		java.lang.Object number = get(key);
		return DataConverter.toLong(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}



	public double getDouble(String key) {
		java.lang.Object number = get(key);
		return DataConverter.toDouble(number, (value, type) -> {
			throw new CSONException(key, value, type.getTypeName());
		});
	}

	public String getString(String key) {
		java.lang.Object obj = get(key);
		if(obj == null) {
			return null;
		}

		return DataConverter.toString(obj);
	}

	@SuppressWarnings("DuplicatedCode")
    public CSONObject getCSONObject(String key) {
		java.lang.Object obj = get(key);
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
		java.lang.Object obj = get(key);
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
			return CSONSerializer.csonArrayToList(csonArray, valueType, csonArray.getWritingOptions(), false, null);
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

	public java.lang.Object opt(String key) {
		java.lang.Object obj = getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	public boolean optBoolean(String key, boolean def) {
		java.lang.Object obj = opt(key);
		return DataConverter.toBoolean(obj, def);
	}

	public byte optByte(String key) {
		return optByte(key, (byte)0);
	}

	public byte optByte(String key, byte def) {
		java.lang.Object number = opt(key);
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
		java.lang.Object obj = opt(key);
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
		java.lang.Object number = opt(key);
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
		java.lang.Object obj = opt(key);
		if(obj == null) {
			return def;
		}

		return DataConverter.toChar(obj, def);

	}


	public int optInt(String key) {
		return optInt(key, 0);
	}

	public int optInt(String key, int def) {
		java.lang.Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toInteger(number, def);

	}

	public float optFloat(String key) {
		return optFloat(key, Float.NaN);
	}


	public float optFloat(String key, float def) {
		java.lang.Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toFloat(obj, def);
	}

	public long optLong(String key) {
		return optLong(key, 0);
	}

	public long optLong(String key, long def) {
		java.lang.Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toLong(number, def);
	}

	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}

	public double optDouble(String key, double def) {
		java.lang.Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toDouble(obj, def);
	}

	public String optString(String key) {
		return optString(key, null);
	}

	public String optString(String key,String def) {
		java.lang.Object obj = opt(key);
		return obj == null ? def : DataConverter.toString(obj);
	}

	public CSONArray optCSONArray(String key) {
		return optCSONArray(key, null);
	}

	public CSONArray optCSONArray(String key, CSONArray def) {
		@SuppressWarnings("DuplicatedCode")
		java.lang.Object obj = opt(key);
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
		java.lang.Object object = opt(key);
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
		java.lang.Object obj = opt(key);
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
			return CSONSerializer.csonArrayToList(csonArray, valueType, csonArray.getWritingOptions(), true, defaultValue);
		} catch (Exception e) {
			if(defaultValue != null) {
				List<T> result = new ArrayList<>();
				result.add(defaultValue);
				return result;
			} else {
                //noinspection unchecked
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
			java.lang.Object value = csonObject.get(key);
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
			java.lang.Object value = csonObject.get(key);
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
			java.lang.Object value = csonObject.get(key);
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
	@SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
	public int getInteger(String key) {
		return getInt(key);
	}


	public CSONObject setComment(CommentPosition position, String key, String comment) {
		CommentObject commentObject = getOrCreateCommentObject(key);
		commentObject.setComment(position, comment);
		return this;
	}


	public CSONObject setCommentForKey(String key, String comment) {
		setComment(CommentPosition.BEFORE_KEY, key, comment);
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentForValue(String key, String comment) {
		setComment(CommentPosition.BEFORE_VALUE, key, comment);
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentAfterValue(String key, String comment) {
		setComment(CommentPosition.AFTER_VALUE, key, comment);
		return this;
	}


	@SuppressWarnings("UnusedReturnValue")
	public CSONObject setCommentAfterKey(String key, String comment) {
		setComment(CommentPosition.AFTER_KEY, key, comment);
		return this;
	}

	@SuppressWarnings("unused")
	public CSONObject setComment(String key, String comment) {
		return setCommentForKey(key, comment);
	}

	public String getComment(CommentPosition position, String key) {
		if(keyValueCommentMap == null) return null;
		CommentObject commentObject = keyValueCommentMap.get(key);
		if(commentObject == null) return null;
		return commentObject.getComment(position);
	}

	public String getCommentForKey(String key) {
		return getComment(CommentPosition.BEFORE_KEY, key);
	}

	public String getCommentAfterKey(String key) {
		return getComment(CommentPosition.AFTER_KEY, key);
	}


	public String getCommentForValue(String key) {
		return getComment(CommentPosition.BEFORE_VALUE, key);
	}


	public String getCommentAfterValue(String key) {
		return getComment(CommentPosition.AFTER_VALUE, key);
	}




	private CommentObject getOrCreateCommentObject(String key) {
		if(keyValueCommentMap == null) {
			keyValueCommentMap = new LinkedHashMap<>();
		}
        return keyValueCommentMap.computeIfAbsent(key, k -> CommentObject.forKeyValueContainer());
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
	public String toString() {
		return toString(getWritingOptions());
	}

	@Override
	public String toString(WritingOptions<?> writingOptions) {
		if(writingOptions instanceof JSON5WriterOption) {
			JSON5Writer jsonWriter = new JSON5Writer((JSON5WriterOption)writingOptions);
			write(jsonWriter);
			return jsonWriter.toString();
		}
		return toString();

	}

	@Override
	public void clear() {
		dataMap.clear();
	}

	public boolean containsValue(java.lang.Object value) {
		boolean result = dataMap.containsValue(value);
		if(value == null && !result) {
			return dataMap.containsValue(NullValue.Instance);
		}
		return result;
	}

	public boolean containsValueNoStrict(java.lang.Object value) {
		Collection<java.lang.Object> values = dataMap.values();
		return containsNoStrict(values, value);

	}




	public byte[] toBytes() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			BinaryCSONWriter writer = new BinaryCSONWriter(outputStream);
			writer.write(new CSONKeyValueDataContainer(this));
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}



	@Override
	protected void write(FormatWriter writer) {
		writer.write(new CSONKeyValueDataContainer(this));

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
		for (Entry<String, java.lang.Object> entry : dataMap.entrySet()) {
			String key = entry.getKey();
			java.lang.Object obj = entry.getValue();
			if (obj instanceof CSONArray) csonObject.put(key, ((CSONArray) obj).clone());
			else if (obj instanceof CSONObject) csonObject.put(key, ((CSONObject) obj).clone());
			else if (obj instanceof CharSequence) csonObject.put(key, ((CharSequence) obj).toString());
			else if (obj == NullValue.Instance) csonObject.put(key,NullValue.Instance);
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
	public boolean equals(java.lang.Object obj) {
		if(!(obj instanceof CSONObject)) {
			return false;
		}
		CSONObject csonObject = (CSONObject)obj;
		if(csonObject.size() != size()) {
			return false;
		}
		for (Entry<String, java.lang.Object> entry : dataMap.entrySet()) {
			String key = entry.getKey();
			java.lang.Object compareValue = entry.getValue();
			java.lang.Object value = this.getFromDataMap(key);
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

	public Collection<java.lang.Object> values() {
		return dataMap.values();
	}

	public Iterator<Entry<String, java.lang.Object>> iteratorEntry() {
		return new Iterator<Entry<String, java.lang.Object>>() {
			final Iterator<Entry<String, java.lang.Object>> entryIter = new ArrayList<>(dataMap.entrySet()).iterator();
			String key = null;
			@Override
			public boolean hasNext() {
				return entryIter.hasNext();
			}

			@Override
			public Entry<String, java.lang.Object> next() {
				Entry<String, java.lang.Object> entry = entryIter.next();
				java.lang.Object object = entry.getValue();
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
	public Iterator<java.lang.Object> iterator() {


		return new Iterator<java.lang.Object>() {
			final Iterator<Entry<String, java.lang.Object>> entryIterator = iteratorEntry();

			@Override
			public boolean hasNext() {
				return entryIterator.hasNext();
			}

			@Override
			public java.lang.Object next() {
				return entryIterator.next().getValue();
			}

			@Override
			public void remove() {
				entryIterator.remove();
			}
		};
	}



	final static KeyValueDataContainerFactory KeyValueDataContainerFactory = () -> new CSONKeyValueDataContainer(new CSONObject());


	static class CSONKeyValueDataContainer implements KeyValueDataContainer {

		final CSONObject csonObject;
		private String lastKey;

		CSONKeyValueDataContainer(CSONObject csonObject) {
			this.csonObject = csonObject;
		}

		@Override
		public void put(String key, Object value) {
			if(value instanceof CSONKeyValueDataContainer) {
				csonObject.put(key, ((CSONKeyValueDataContainer) value).csonObject);
				return;
			} else if(value instanceof ArrayDataContainer) {
				csonObject.put(key, ((CSONArray.CSONArrayDataContainer) value).array);
				return;
			}
			lastKey = key;
			csonObject.dataMap.put(key, value);
		}

		@Override
		public Object get(String key) {
			lastKey = key;
			return csonObject.dataMap.get(key);
		}

		@Override
		public String getLastAccessedKey() {
			return lastKey;
		}

		@Override
		public void remove(String key) {
			lastKey = key;
			csonObject.dataMap.remove(key);
		}

		@Override
		public void setComment(String key, String comment, CommentPosition type) {
			switch (type) {
				case DEFAULT:
				case BEFORE_KEY:
					csonObject.setCommentForKey(key, comment);
					break;
				case BEFORE_VALUE:
					csonObject.setCommentForValue(key, comment);
					break;
				case AFTER_KEY:
					csonObject.setCommentAfterKey(key, comment);
					break;
				case AFTER_VALUE:
					csonObject.setCommentAfterValue(key, comment);
					break;
				case HEADER:
					csonObject.setHeaderComment(comment);
					break;
				case FOOTER:
					csonObject.setFooterComment(comment);
					break;

			}

		}

		@Override
		public String getComment(String key, CommentPosition type) {
			switch (type) {
				case DEFAULT:
				case BEFORE_KEY:
					return csonObject.getCommentForKey(key);
				case BEFORE_VALUE:
					return csonObject.getCommentForValue(key);
				case AFTER_KEY:
					return csonObject.getCommentAfterKey(key);
				case AFTER_VALUE:
					return csonObject.getCommentAfterValue(key);
				case HEADER:
					return csonObject.getHeaderComment();
				case FOOTER:
					return csonObject.getFooterComment();
			}
			return null;
		}

		@Override
		public CommentObject getCommentObject(String key) {
			if(csonObject.keyValueCommentMap == null) {
				return null;
			}
			return csonObject.keyValueCommentMap.get(key);
		}

		@Override
		public int size() {
			return csonObject.dataMap.size();
		}

		@Override
		public void setSourceFormat(FormatType formatType) {
			switch (formatType) {
				case JSON:
					csonObject.setWritingOptions(WritingOptions.json());
					break;
				case JSON5:
					csonObject.setWritingOptions(WritingOptions.json5());
					break;
				case JSON5_PRETTY:
					csonObject.setWritingOptions(WritingOptions.json5Pretty());
					break;
				case JSON_PRETTY:
					csonObject.setWritingOptions(WritingOptions.jsonPretty());
					break;
			}

		}

		@Override
		public DataIterator<?> iterator() {
			return new EntryDataIterator(csonObject.dataMap.entrySet().iterator(), csonObject.dataMap.size(), true);
		}
	}

	private static class EntryDataIterator extends DataIterator<Entry<String, Object>>{

		public EntryDataIterator(Iterator<Entry<String, Object>> iterator, int size, boolean isEntryValue) {
			super(iterator, size, isEntryValue);
		}

		@Override
		public Entry<String, Object> next() {
			Entry<String, Object> originEntry = super.next();
			Entry<String, Object> entry = new AbstractMap.SimpleEntry<>(originEntry.getKey(), originEntry.getValue());
			Object value = entry.getValue();
			if(value instanceof NullValue) {
				entry.setValue(null);
			} else if(value instanceof CSONObject) {
				entry.setValue(new CSONKeyValueDataContainer((CSONObject)value));
			} else if(value instanceof CSONArray) {
				entry.setValue(new CSONArray.CSONArrayDataContainer((CSONArray)value));
			} else {
				entry.setValue(value);
			}
			return entry;
		}
	}

}
