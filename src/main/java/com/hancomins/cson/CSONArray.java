package com.hancomins.cson;



import com.hancomins.cson.options.*;
import com.hancomins.cson.serializer.CSONSerializer;
import com.hancomins.cson.util.DataConverter;
import com.hancomins.cson.util.NoSynchronizedStringReader;
import com.hancomins.cson.util.NullValue;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;


@SuppressWarnings("ALL")
public class CSONArray extends CSONElement  implements Collection<Object>, Cloneable {

	private ArrayList<Object> list = new ArrayList<>();
	private ArrayList<CommentObject> commentObjectList = null;


	public static CSONArray fromCollection(Collection<?> collection) {
		return CSONSerializer.collectionToCSONArray(collection);
	}

	public static CSONArray fromCollection(Collection<?> collection, WritingOptions<?> writingOptions) {
		CSONArray csonArray = CSONSerializer.collectionToCSONArray(collection);
		csonArray.setWritingOptions(writingOptions);
		return csonArray;
	}

	public static <T> Collection<T> toCollection(CSONArray csonArray, Class<T> clazz) {
		return CSONSerializer.csonArrayToList(csonArray, clazz, csonArray.getWritingOptions(), false, null);
	}

	public static <T> Collection<T> toCollection(CSONArray csonArray, Class<T> clazz, boolean ignoreError) {
		return CSONSerializer.csonArrayToList(csonArray, clazz, csonArray.getWritingOptions(), ignoreError, null);
	}


	public CSONArray() {
		super(ElementType.Array);
	}



	public CSONArray(Reader stringSource) throws CSONException {
		super(ElementType.Array);
		parse(stringSource, ParsingOptions.getDefaultParsingOptions());
	}

	public CSONArray(Reader stringSource, WritingOptions<?> writingOptions) {
		super(ElementType.Array);
		parse(stringSource, ParsingOptions.getDefaultParsingOptions());
		this.setWritingOptions(writingOptions);
	}

	public CSONArray(Reader source, ParsingOptions<?> options) throws CSONException {
		super(ElementType.Array);
		parse(source, options);
	}


	public CSONArray(String jsonArray) throws CSONException {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(jsonArray);
		parse(noSynchronizedStringReader, ParsingOptions.getDefaultParsingOptions());
		noSynchronizedStringReader.close();;
	}

	public CSONArray(String jsonArray, ParsingOptions<?> options) throws CSONException {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(jsonArray);
		parse(noSynchronizedStringReader, options);
		noSynchronizedStringReader.close();
	}



	public CSONArray(WritingOptions<?> writingOptions) {

		super(ElementType.Array, writingOptions);
	}


	private void parse(Reader stringReader, ParsingOptions<?> options) {
		StringFormatType type = options.getFormatType();
		/*if(JsonParsingOptions.isPureJSONOption(options)) {
			PureJSONParser.parsePureJSON(stringReader, this, options);
		} else {*/
			//new JSONParser(new JSONTokener(stringReader, (JsonParsingOptions)options)).parseArray(this);
			//new JSON5ParserV((JsonParsingOptions) options).parsePureJSON(stringReader, this);
			 JSON5ParserX.parse(stringReader, this, (JsonParsingOptions) options);

		//}
	}


	public CSONArray(int capacity) {
		super(ElementType.Array);
		this.list.ensureCapacity(capacity);
	}

	public CSONArray(Collection<?> objects) {
		super(ElementType.Array);
		list.addAll(objects);
	}

	public CSONArray(byte[] binaryJson) {
		super(ElementType.Array);
		this.list = ((CSONArray) BinaryCSONParser.parse(binaryJson)).list;
	}


	public CSONArray(byte[] binary,int offset, int len) {
		super(ElementType.Array);
		this.list = ((CSONArray) BinaryCSONParser.parse(binary, offset, len)).list;
	}





	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		boolean result = list.contains(o);
		if(!result && o == null) {
			return list.contains(NullValue.Instance);
		}
		return result;
	}

	public boolean containsNoStrict(Object value) {
		return containsNoStrict(list, value);
	}


	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return toList().toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return toList().toArray(a);
	}

	public List<Object> toList() {
		List<Object> results = new ArrayList<Object>(this.list.size());
		for (Object element : this.list) {
			if (element == null) {
				results.add(null);
			} else if (element instanceof CSONArray) {
				results.add(((CSONArray) element).toList());
			} else if (element instanceof CSONObject) {
				results.add(((CSONObject) element).toMap());
			} else {
				results.add(element);
			}
		}
		return results;
	}



	@SuppressWarnings("unused")
	public String getCommentForValue(int index) {
		CommentObject commentObject = getCommentObject(index);
		if(commentObject == null) return null;
		return commentObject.getLeadingComment();
	}

	@SuppressWarnings("unused")
	public String getCommentAfterValue(int index) {
		CommentObject commentObject = getCommentObject(index);
		if(commentObject == null) return null;
		return commentObject.getTrailingComment();
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public CSONArray setCommentForValue(int index, String comment) {
		CommentObject commentObject = getCommentObject(index, true);
		commentObject.setLeadingComment(comment);
		return this;
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public CSONArray setCommentAfterValue(int index, String comment) {
		CommentObject commentObject = getCommentObject(index, true);
		commentObject.setTrailingComment(comment);
		return this;
	}


	protected CommentObject getOrCreateCommentObject(int index) {
		CommentObject commentObject = getCommentObject(index);
		if(commentObject == null) {
			commentObject = new CommentObject();
			setCommentObject(index, commentObject);
		}
		return commentObject;
	}


	public CommentObject getCommentObject(int index) {
		if(commentObjectList == null) return null;
		if(index >= commentObjectList.size()) return null;
		return commentObjectList.get(index);
	}

	public CommentObject getCommentObject(int index, boolean createIfNotExists) {
		if(commentObjectList == null) {
			if(!createIfNotExists) return null;
			commentObjectList = new ArrayList<CommentObject>();
		}
		if(index >= commentObjectList.size()) {
			if(!createIfNotExists) return null;
			ensureCapacityOfCommentObjects(index);
		}
		CommentObject commentObject = commentObjectList.get(index);
		if(commentObject == null && createIfNotExists) {
			commentObject = new CommentObject();
			commentObjectList.set(index, commentObject);
		}


		return commentObject;
	}


	@SuppressWarnings("unused")
	public void setCommentObject(int index, CommentObject commentObject) {
		if(commentObjectList == null) {
			commentObjectList = new ArrayList<>();
		}
		if(commentObjectList.size() <= index) {
			ensureCapacityOfCommentObjects(index);
		}
		commentObjectList.set(index, commentObject);
	}


	private void ensureCapacityOfCommentObjects(int index) {
		//commentObjectList.ensureCapacity(list.size());
		for (int i = commentObjectList.size(), n = index + 1; i < n; i++) {
			commentObjectList.add(null);
		}
	}




	protected void addAtJSONParsing(Object value) {
		if(value instanceof String && CSONElement.isBase64String((String)value)) {
			value = CSONElement.base64StringToByteArray((String)value);
		}
		list.add(value);
	}


	protected void addCommentObjects(CommentObject commentObject) {
		if(commentObjectList == null) {
			commentObjectList = new ArrayList<>();
		}
		commentObjectList.add(commentObject);
	}


	public CSONArray put(Object e) {
		if(!add(e)) {
			throw new CSONException("put error. can't put " + e.getClass() + " to CSONArray.");
		}
		return this;
	}

	public CSONArray put(Object... e) {
		for(Object obj : e) {
			if(!add(obj)) {
				throw new CSONException("put error. can't put " + obj.getClass() + " to CSONArray.");
			}
		}
		return this;
	}

	@SuppressWarnings("unused")
	public CSONArray putAll(Object e) {
		if(e instanceof  Collection) {
			for(Object obj : (Collection<?>)e) {
				if(!add(obj)) {
					throw new CSONException("putAll error. can't put " + obj.getClass() + " to CSONArray.");
				}
			}
		} else if(e.getClass().isArray()) {
			for(int i = 0, n = Array.getLength(e); i < n; ++i) {
				Object obj = Array.get(e, i);
				if(!add(obj)) {
					throw new CSONException("putAll error. can't put " + obj.getClass() + " to CSONArray.");
				}
			}
		} else {
			throw new CSONException("putAll error. can't put " + e.getClass()+ " to CSONArray.");
		}
		return this;
	}



	public CSONArray set(int index, Object e) {
		int size = list.size();
		Object value = convert(e);
		if(index >= size) {
			for(int i = size; i < index; i++) {
				add(null);
			}
			list.add(value);
		} else {
			list.set(index, value);
		}
		return this;
	}

	public CSONArray setList(Collection<?> collection) {
		for (Object obj : collection) {
			if(!add(obj)) {
				throw new CSONException("new CSONArray(Collection) error. can't put " + obj.getClass() + " to CSONArray.");
			}
		}
		return this;
	}



	private Object convert(Object e) {
		if(e == null) {
			return NullValue.Instance;
		}
		else if(e instanceof Number) {
			return e;
		} else if(e instanceof CharSequence) {
			return e.toString();
		} else if(e instanceof CSONElement) {
			if(e == this) e = ((CSONArray)e).clone();
			return e;
		}
		else if(e instanceof Character || e instanceof Boolean || e instanceof CSONObject || e instanceof byte[] ) {
			return e;
		} else if(e.getClass().isArray()) {
			CSONArray array = new CSONArray();
			for(int i = 0, n = Array.getLength(e); i < n; ++i) {
				array.add(Array.get(e, i));
			}
			return array;
		} else if(e instanceof  Collection) {
			CSONArray array = new CSONArray();
			for(Object obj : (Collection<?>)e) {
				//noinspection UseBulkOperation
				array.add(obj);
			}
			return array;
		} else if(CSONSerializer.serializable(e.getClass())) {
			return CSONSerializer.toCSONObject(e);
		}
		else if(isAllowRawValue()) {
			return e;
		}
		return isUnknownObjectToString() ? e + "" : null;
	}



	CSONArray addByParser(Object value) {
		list.add(value);
		return this;
	}


	@Override
	public boolean add(Object e) {
		Object value = convert(e);
		if(value == null) {
			return false;
		}
		list.add(value);
		return true;
	}


	@SuppressWarnings("UnusedReturnValue")
	public boolean addAll(Object e) {
		if(e instanceof  Collection) {
			for(Object obj : (Collection<?>)e) {
				if(!add(obj)) {
					return false;
				}
			}
		} else if(e.getClass().isArray()) {
			for(int i = 0, n = Array.getLength(e); i < n; ++i) {
				if(!add(Array.get(e, i))) {
					return false;
				}
			}
		}
		return true;
	}





	/**
	 * @deprecated use {@link #getCSONObject(int)} instead.
	 */
	@Deprecated
	public CSONObject getObject(int index) {
		return getCSONObject(index);
	}








	public boolean isNull(int index) {
		Object obj = list.get(index);
		return obj == null || obj instanceof NullValue;
	}




	public Object get(int index) {
		if(index < 0 || index >= list.size()) {
			throw new CSONIndexNotFoundException(ExceptionMessages.getCSONArrayIndexOutOfBounds(index, list.size()));
		}
		Object obj = list.get(index);
		if(obj instanceof NullValue) return null;
		copyHeadTailCommentToValueObject(index, obj);

		return obj;
	}

	private void copyHeadTailCommentToValueObject(int key, Object obj) {
		if(commentObjectList != null && obj instanceof CSONElement && !commentObjectList.isEmpty()) {
			CommentObject valueCommentObject = commentObjectList.get(key);
			if(valueCommentObject != null) {
				((CSONElement)obj).setTailComment(valueCommentObject.getTrailingComment());
				((CSONElement)obj).setHeadComment(valueCommentObject.getLeadingComment());
			}
		}
	}

	public <T extends Enum<T>> T getEnum(int index, Class<T> enumType) {
		Object obj = get(index);

		T result =  DataConverter.toEnum(enumType, obj);
		if(result == null) {
			throw new CSONException(index, obj, enumType.getTypeName());
		}
		return result;
	}


	public boolean getBoolean(int index) {
		Object obj = get(index);
		if(obj instanceof Boolean) {
			return (Boolean)obj;
		} else if("true".equalsIgnoreCase(obj + "")) {
			return true;
		} else if("false".equalsIgnoreCase(obj + "")) {
			return false;
		}
		throw new CSONException(index, obj, boolean.class.getTypeName());
	}


	public byte getByte(final int index) {
		Object number = get(index);
		return DataConverter.toByte(number, (byte) 0, ((value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		}));
	}




	public byte[] getByteArray(int index) {
		Object obj = get(index);
		byte[] byteArray = DataConverter.toByteArray(obj);
		if(byteArray == null) {
			throw new CSONException(index, obj, byte[].class.getTypeName());
		}
		return byteArray;
	}


	public char getChar(int index) {
		Object number = get(index);

		return DataConverter.toChar(number, (char)0, (value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		});

	}



	public short getShort(int index) {
		Object number = get(index);
		return DataConverter.toShort(number, (value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		});

	}


	public int getInt(int index) {
		Object number = get(index);
		return DataConverter.toInteger(number, (value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		});
	}



	public float getFloat(int index) {
		Object number = get(index);
		return DataConverter.toFloat(number, (value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		});
	}


	public long getLong(int index) {
		Object number = get(index);
		return DataConverter.toLong(number, (value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		});
	}



	public double getDouble(int index) {
		Object number = get(index);
		return DataConverter.toDouble(number, (value, type) -> {
			throw new CSONException(index, value, type.getTypeName());
		});
	}


	public String getString(int index) {
		Object obj = get(index);
		if(obj == null) {
			return null;
		}

		return DataConverter.toString(obj);
	}

	public CSONArray getCSONArray(int index) {
		Object obj = get(index);
		if(obj == null) {
			return null;
		}
		CSONArray csonArray = DataConverter.toArray(obj, true);
		if(csonArray == null) {
			throw new CSONException(index, obj, CSONArray.class.getTypeName());
		}
		return csonArray;
	}

	public CSONObject getCSONObject(int index) {
		Object obj = get(index);
		if(obj == null) {
			return null;
		}
		CSONObject csonObject = DataConverter.toObject(obj, true);
		if(csonObject == null) {
			throw new CSONException(index, obj, CSONObject.class.getTypeName());
		}
		return csonObject;
	}


	public <T> List<T> getList(int index, Class<T> valueType) {
		CSONArray csonArray = getCSONArray(index);
		if(csonArray == null) {
			return null;
		}
		try {
			
			return CSONSerializer.csonArrayToList(csonArray, valueType, csonArray.getWritingOptions(), false, null);
		} catch (Throwable e) {
			throw new CSONException(index, csonArray, "List<" + valueType.getTypeName() + ">", e);
		}
	}



	public <T> T getObject(int index, Class<T> clazz) {
		CSONObject csonObject = getCSONObject(index);
		try {
			return CSONSerializer.fromCSONObject(csonObject, clazz);
		} catch (Throwable e) {
			throw new CSONException(index, csonObject, clazz.getTypeName(),e );
		}
	}


	public Object opt(int index) {
		if(index < 0 || index >= list.size()) {
			return null;
		}
		Object obj = list.get(index);
		if(obj instanceof NullValue) return null;
		copyHeadTailCommentToValueObject(index, obj);
		return obj;
	}


	public boolean optBoolean(int index, boolean def) {
		Object obj = opt(index);
		return DataConverter.toBoolean(obj, def);
	}

	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}


	public byte optByte(int index) {
		return optByte(index, (byte)0);
	}

	@SuppressWarnings("unused")
	public byte optByte(int index, byte def) {
		Object number = opt(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toByte(number, def);
	}



	public byte[] optByteArray(int index) {
		return optByteArray(index, null);
	}

	@SuppressWarnings("unused")
	public byte[] optByteArray(int index,byte[] def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		byte[] buffer =  DataConverter.toByteArray(obj);
		if(buffer == null) {
			return def;
		}
		return buffer;

	}



	public short optShort(int index) {
		return optShort(index, (short)0);
	}

	public short optShort(int index, short def) {
		Object number = opt(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toShort(number, def);
	}

	@SuppressWarnings("unused")
	public char optChar(int index) {
		return optChar(index, '\0');
	}

	public char optChar(int index, char def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toChar(obj,def);
	}


	public int optInt(int index) {
		return optInt(index, 0);
	}

	public int optInt(int index, int def) {
		Object number = opt(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toInteger(number, def);

	}

	public float optFloat(int index) {
		return optFloat(index, Float.NaN);
	}


	public float optFloat(int index, float def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toFloat(obj, def);
	}

	public long optLong(int index) {
		return optLong(index, 0);
	}

	public long optLong(int index, long def) {
		Object number = opt(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toLong(number, def);
	}

	public double optDouble(int index) {
		return optDouble(index, Double.NaN);
	}

	public double optDouble(int index, double def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toDouble(obj, def);
	}

	public String optString(int index) {
		return optString(index, null);
	}

	public String optString(int index,String def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toString(obj);
	}

	public CSONArray optCSONArray(int index) {
		return optCSONArray(index, null);
	}

	public CSONArray optCSONArray(int index, CSONArray def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		CSONArray csonArray = DataConverter.toArray(obj, true);
		if(csonArray == null) {
			return def;
		}
		return csonArray;
	}

	public CSONArray optWrapCSONArray(int index) {
		Object object = opt(index);
		if(object == null) {
			return new CSONArray();
		}
		CSONArray csonArray = DataConverter.toArray(object, true);
		if(csonArray == null) {
			return new CSONArray().put(object);
		}
		return csonArray;
	}

	public CSONObject optCSONObject(int index) {
		return optCSONObject(index, null);
	}

	public CSONObject optCSONObject(int index, CSONObject def) {
		Object obj = opt(index);
		if(obj == null) {
			return def;
		}
		CSONObject csonObject = DataConverter.toObject(obj, true);
		if(csonObject == null) {
			return def;
		}
		return csonObject;
	}


	public <T> T optObject(int index, Class<T> clazz) {
		return optObject(index, clazz, null);
	}

	public <T> T optObject(int index, Class<T> clazz, T defaultObject) {
		try {
			CSONObject csonObject = optCSONObject(index);
			return CSONSerializer.fromCSONObject(csonObject, clazz);
		} catch (Exception e) {
			return defaultObject;
		}
	}


	public <T> List<T> optList(int index, Class<T> valueType) {
		return optList(index, valueType, null);
	}

	public <T> List<T> optList(int index, Class<T> valueType, T defaultValue) {
		try {
			CSONArray csonArray = optCSONArray(index);
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
				return Collections.EMPTY_LIST;
			}
		}
	}













	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	public boolean remove(int index) {
		try {
			list.remove(index);
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public boolean containsAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.containsAll(c);
	}


	@Override
	public boolean addAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		for(Object obj : c) {
			if(!add(obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @Deprecated use {@link #subtractIntersection(CSONArray)} instead.
	 * @param c collection containing elements to be removed from this collection
	 * @return
	 */
	@Deprecated
	@Override
	public boolean removeAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@SuppressWarnings("unused")
	public CsonArrayEnumerator enumeration() {
		return new CsonArrayEnumerator(this);
	}


	public static class CsonArrayEnumerator implements Enumeration<Object>  {
		int index = 0;
		CSONArray array = null;

		private CsonArrayEnumerator(CSONArray array) {
			this.array = array;
		}

		@Override
		public Object nextElement() {
			if(hasMoreElements()) {
				return array.get(index++);
			}
			return null;
		}


		@Deprecated
		public CSONArray getArray() {
			return array.getCSONArray(index++);
		}

		public CSONArray getCSONArray() {
			return array.getCSONArray(index++);
		}

		@Deprecated
		public CSONArray optArray() {
			return array.optCSONArray(index++);
		}

		public CSONArray optCSOMArray() {
			return array.optCSONArray(index++);
		}

		@SuppressWarnings("unused")
		@Deprecated
		public int getInteger() {
			return array.getInteger(index++);
		}

		public int getInt() {
			return array.getInteger(index++);
		}

		@SuppressWarnings("unused")
		@Deprecated
		public int optInteger() {
			return array.optInteger(index++);
		}


		public int optInt() {
			return array.optInteger(index++);
		}

		public short getShort() {
			return array.getShort(index++);
		}

		@SuppressWarnings("unused")
		public int optShort() {
			return array.optShort(index++);
		}

		public float getFloat() {
			return array.getFloat(index++);
		}

		@SuppressWarnings("unused")
		public float optFloat() {
			return array.optFloat(index++);
		}

		public String getString() {
			return array.getString(index++);
		}

		@SuppressWarnings("unused")
		public String optString() {
			return array.optString(index++);
		}

		public boolean getBoolean() {
			return array.getBoolean(index++);
		}

		@SuppressWarnings("unused")
		public boolean optBoolean() {
			return array.optBoolean(index++);
		}


		@Override
		public boolean hasMoreElements() {
			return !(index >= array.size());
		}
	}

	/**
	 * @Deprecated use {@link #toCSONBinary()} instead.
	 */

	@Deprecated
	public byte[] toByteArray() {
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
		catch (IOException ignored) {
			throw new CSONException(ignored);
		}
	}

	@Override
	public void writeCSONBinary(OutputStream outputStream) throws IOException {
		BinaryCSONWriter writer = new BinaryCSONWriter(outputStream);
		write(writer);
	}




	@SuppressWarnings("ForLoopReplaceableByForEach")
	void write(BinaryCSONWriter writer) throws IOException {
		writer.openArray();
		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
			if(obj == null || obj instanceof NullValue) writer.addNull();
			else if(obj instanceof CSONArray)  {
				((CSONArray)obj).write(writer);
			}
			else if(obj instanceof CSONObject)  {
				((CSONObject)obj).write(writer);
			}
			else if(obj instanceof Byte)	writer.add((Byte)obj);
			else if(obj instanceof Short)	writer.add((Short)obj);
			else if(obj instanceof Character) writer.add((Character)obj);
			else if(obj instanceof Integer) writer.add((Integer)obj);
			else if(obj instanceof Float) writer.add((Float)obj);
			else if(obj instanceof Long) writer.add((Long)obj);
			else if(obj instanceof Double) writer.add((Double)obj);
			else if(obj instanceof String) writer.add((String)obj);
			else if(obj instanceof byte[]) writer.add((byte[])obj);
			else if(obj instanceof Boolean) writer.add((Boolean)obj);
		}
		writer.closeArray();

	}


	@Override
	protected void write(JSONWriter writer, boolean root) {
		JSONWriter.writeJSONElement(this, writer);
		/*if(root) {
			writer.writeComment(getHeadComment(), false,"","\n" );
		}
		writer.openArray();
		int commentListEndIndex = commentObjectList == null ? -1 : commentObjectList.size() - 1;

		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
			boolean isListSizeOverCommentObjectListSize = i > commentListEndIndex;
			CommentObject commentObject = isListSizeOverCommentObjectListSize ? null : commentObjectList.get(i);
			writer.nextCommentObject(commentObject);
			if(obj == null || obj instanceof NullValue) writer.addNull();
			else if(obj instanceof CSONElement)  {
				//((CSONElement)obj).write(writer, false);
				writer.add((CSONElement) obj);
			}
			else if(obj instanceof Byte)	writer.add((byte)obj);
			else if(obj instanceof Short)	writer.add((short)obj);
			else if(obj instanceof Character) writer.add((char)obj);
			else if(obj instanceof Integer) writer.add((int)obj);
			else if(obj instanceof Float) writer.add((float)obj);
			else if(obj instanceof Long) writer.add((long)obj);
			else if(obj instanceof Double) writer.add((double)obj);
			else if(obj instanceof String) writer.add((String)obj);
			else if(obj instanceof Boolean) writer.add((boolean) obj);
			else if(obj instanceof byte[]) writer.add((byte[])obj);
			else if(obj instanceof BigDecimal) writer.add((BigDecimal)obj);
			else if(obj instanceof BigInteger) writer.add((BigInteger)obj);
			else  writer.add(obj.toString());
		}

		writer.closeArray();
		if(root) {
			writer.writeComment(getTailComment(), false, "\n", "");
		}*/

	}


	@Override
	public String toString() {
		return toString(getWritingOptions());
	}

	public String toString(WritingOptions<?> writingOptions) {
		if(writingOptions instanceof JsonWritingOptions) {
			JSONWriter jsonWriter  = new JSONWriter((JsonWritingOptions)writingOptions);
			write(jsonWriter, true);
			return jsonWriter.toString();
		}
		return this.toString();
	}



	@Override
	@SuppressWarnings({"MethodDoesntCallSuperMethod", "ForLoopReplaceableByForEach"})
	public CSONArray clone() {
		CSONArray array = new CSONArray();
		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
			if(obj instanceof CSONArray) array.add(((CSONArray)obj).clone());
			else if(obj instanceof CSONObject) array.add(((CSONObject)obj).clone());
			else if(obj == NullValue.Instance) array.add(null);
			else if(obj instanceof CharSequence) array.add(((CharSequence)obj).toString());
			else if(obj instanceof byte[]) {
				byte[] bytes = (byte[])obj;
				byte[] newBytes = new byte[bytes.length];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				array.add(newBytes);
			}
			else array.add(obj);
		}
		return array;
	}

	/**
	 * 다른 CSONArray와 병합한다.
	 * @param csonArray 병합할 CSONArray
	 */
	public void merge(CSONArray csonArray) {
		for(int i = 0, n = csonArray.size(); i < n; ++i) {
			Object newObj = csonArray.get(i);
			Object originObj = opt(i);
			if(originObj == null) {
				add(newObj);
			} else if(originObj instanceof CSONArray && newObj instanceof CSONArray) {
				((CSONArray)originObj).merge((CSONArray)newObj);
			} else if(originObj instanceof CSONObject && newObj instanceof CSONObject) {
				((CSONObject)originObj).merge((CSONObject)newObj);
			} else {
				set(i, newObj);
			}
		}
	}


	/**
	 * 교집합을 반환한다.
	 * @param csonArray 교집합을 구할 CSONArray
	 * @return 교집합
	 */
	public CSONArray intersect(CSONArray csonArray) {
		CSONArray result = new CSONArray();
		for(int i = 0, n = csonArray.size(); i < n; ++i) {
			Object newObj = csonArray.get(i);
			Object originObj = opt(i);
			if(originObj == null) {
				continue;
			} else if(originObj instanceof CSONArray && newObj instanceof CSONArray) {
				result.add(((CSONArray)originObj).intersect((CSONArray)newObj));
			} else if(originObj instanceof CSONObject && newObj instanceof CSONObject) {
				result.add(((CSONObject)originObj).intersect((CSONObject)newObj));
			} else if(originObj.equals(newObj)) {
				result.add(originObj);
			}
		}
		return result;
	}

	/**
	 * 교집합을 제외한 값을 반환한다.
	 * @param csonArray 교집합을 제외할 CSONArray
	 * @return 교집합을 제외한 값
	 */
	public CSONArray subtractIntersection(CSONArray csonArray) {
		CSONArray result = new CSONArray();
		for(int i = 0, n = size(); i < n; ++i) {
			Object originObj = get(i);
			Object newObj = csonArray.opt(i);
			if(originObj == null) {
				continue;
			} else if(originObj instanceof CSONArray && newObj instanceof CSONArray) {
				result.add(((CSONArray)originObj).subtractIntersection((CSONArray)newObj));
			} else if(originObj instanceof CSONObject && newObj instanceof CSONObject) {
				result.add(((CSONObject)originObj).subtractIntersection((CSONObject)newObj));
			} else if(!originObj.equals(newObj)) {
				result.add(originObj);
			}
		}
		return result;

	}




	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof CSONArray)) return false;
		CSONArray csonObject = (CSONArray)obj;
		if(csonObject.size() != size()) return false;

		for(int i = 0, n = list.size(); i < n; ++i) {
			Object compareValue = csonObject.list.get(i);
			Object value = list.get(i);
			if(value instanceof CharSequence && (!(compareValue instanceof CharSequence) || !value.toString().equals(compareValue.toString())) ) {
				return false;
			}
			else if(value instanceof Boolean && (!(compareValue instanceof Boolean) || value != compareValue)) {
				return false;
			}
			else if(value instanceof Number) {
				boolean valueIsFloat = (value instanceof Float || value instanceof Double);
				boolean compareValueIsFloat = (compareValue instanceof Float || compareValue instanceof Double);
				if(valueIsFloat != compareValueIsFloat) {
					return false;
				}
				BigDecimal v1 = BigDecimal.valueOf(((Number)value).doubleValue());
				BigDecimal v2 = BigDecimal.valueOf(((Number)compareValue).doubleValue());
				if(v1.compareTo(v2) != 0) {
					return false;
				}
			}
			else if(value instanceof CSONArray && (!(compareValue instanceof CSONArray) || !value.equals(compareValue))) {
				return false;
			}
			else if(value instanceof CSONObject && (!(compareValue instanceof CSONObject) || !value.equals(compareValue))) {
				return false;
			}
			else if(value instanceof byte[] && (!(compareValue instanceof byte[]) || !Arrays.equals((byte[])value, (byte[])compareValue))) {
				return false;
			} else if(value != compareValue) {
				return false;
			}
		}
		return true;
	}


	/**
	 * @deprecated use {@link #optCSONObject(int)} instead.
	 */
	@Deprecated
	public CSONObject optObject(int index) {
		return optCSONObject(index);
	}



	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public int optInteger(int index) {
		return optInt(index);
	}

	@Deprecated
	public int optInteger(int index, int def) {
		return optInt(index, def);
	}



	/**
	 * @deprecated use {@link #getCSONArray(int)} instead.
	 */
	@Deprecated
	public CSONArray getArray(int index) {
		return getCSONArray(index);
	}

	/**
	 * @deprecated use {@link #optCSONArray(int)} instead.
	 */
	@Deprecated
	public CSONArray optArray(int index) {
		return optCSONArray(index);
	}

	/**
	 * @deprecated use {@link #optCSONArray(int, CSONArray)} instead.
	 */
	@Deprecated
	public CSONArray optArray(int index, CSONArray def) {
		return optCSONArray(index, def);
	}

	/**
	 * @deprecated use {@link #getInt(int)} instead.
	 */

	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public int getInteger(int index) {
		return getInt(index);
	}




}
