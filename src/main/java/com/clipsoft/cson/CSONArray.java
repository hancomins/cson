package com.clipsoft.cson;



import com.clipsoft.cson.util.NoSynchronizedStringReader;

import java.io.Reader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;


public class CSONArray  extends CSONElement  implements Collection<Object>, Cloneable {

	private ArrayList<Object> list = new ArrayList<>();
	private ArrayList<CommentObject> commentObjectList = null;



	public CSONArray() {
		super(ElementType.Array);
	}


	public CSONArray(StringFormatOption stringFormatOption) {
		super(ElementType.Object);
		this.defaultJSONOptions = stringFormatOption;
	}


	protected CSONArray(JSONTokener x) {
		super(ElementType.Array);
		this.defaultJSONOptions = x.getJsonOption();
		new JSONParser(x).parseArray(this);
	}

	public CSONArray(JSONOptions jsonOptions) {
		super(ElementType.Array);
		this.defaultJSONOptions = jsonOptions;
	}


	private void parse(Reader stringReader, StringFormatOption options) {
		StringFormatType type = options.getFormatType();
		if(type == StringFormatType.PureJSON) {
			PureJSONParser.parsePureJSON(stringReader, this);
		} else {
			new JSONParser(new JSONTokener(stringReader, (JSONOptions)options)).parseArray(this);
		}
	}





	public CSONArray(int size) {
		super(ElementType.Array);
		this.list.ensureCapacity(size);
	}

	public CSONArray(Collection<?> objects) {
		super(ElementType.Array);
		list.addAll(objects);
	}

	public CSONArray(byte[] buffer) {
		super(ElementType.Array);
		this.list = ((CSONArray) BinaryCSONParser.parse(buffer)).list;
	}

	public CSONArray(byte[] buffer,int offset, int len) {
		super(ElementType.Array);
		this.list = ((CSONArray) BinaryCSONParser.parse(buffer, offset, len)).list;
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
		return list.contains(o);
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
		return commentObject.getBeforeComment();
	}

	@SuppressWarnings("unused")
	public String getCommentAfterValue(int index) {
		CommentObject commentObject = getCommentObject(index);
		if(commentObject == null) return null;
		return commentObject.getAfterComment();
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public CSONArray setCommentForValue(int index, String comment) {
		CommentObject commentObject = getCommentObject(index, true);
		commentObject.setBeforeComment(comment);
		return this;
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public CSONArray setCommentAfterValue(int index, String comment) {
		CommentObject commentObject = getCommentObject(index, true);
		commentObject.setAfterComment(comment);
		return this;
	}



	public boolean isNull(int index) {
		Object obj = list.get(index);
		return obj == null || obj instanceof NullValue;
	}


	public CommentObject getCommentObject(int index) {
		if(commentObjectList == null) return null;
		if(index >= commentObjectList.size()) return null;
		return commentObjectList.get(index);
	}

	public CommentObject getCommentObject(int index, boolean createIfNotExists) {
		if(commentObjectList == null) {
			if(!createIfNotExists) return null;
			commentObjectList = new ArrayList<>();
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
		if(commentObjectList.size() <= index) {
			ensureCapacityOfCommentObjects(index);
		}
		if(index >= list.size()) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + list.size());
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


	public CSONArray(Reader stringSource) throws CSONException {
		super(ElementType.Array);
		parse(stringSource, defaultJSONOptions);
	}

	public CSONArray(Reader source, StringFormatOption options) throws CSONException {
		super(ElementType.Array);
		parse(source, options);
	}


	public CSONArray(String source) throws CSONException {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(source);
		parse(noSynchronizedStringReader, defaultJSONOptions);
		noSynchronizedStringReader.close();;
	}

	public CSONArray(String source, StringFormatOption options) throws CSONException {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(source);
		parse(noSynchronizedStringReader, options);
		noSynchronizedStringReader.close();;
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
		}
		else if(isAllowRawValue()) {
			return e;
		}
		return isUnknownObjectToString() ? e + "" : null;
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


	public Object get(int index) {
		Object obj = list.get(index);
		if(obj instanceof NullValue) return null;
		return obj;
	}
	
	public Object opt(int index) {
		try {
			Object obj = list.get(index);
			if(obj instanceof NullValue) return null;
			return obj;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public CSONArray getArray(int index) {
		try {
			return DataConverter.toArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
		
	}

	public CSONArray optArray(int index, CSONArray def) {
		try {
			return DataConverter.toArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public CSONArray optArray(int index) {
		return optArray(index, null);
	}

	@SuppressWarnings("unused")
	public CSONArray optArrayWrap(int index) {
		try {
			Object object = list.get(index);
			if (object instanceof CSONArray) {
				return (CSONArray) object;
			} else if (object == null) {
				return new CSONArray();
			}
			return new CSONArray().put(object);
		} catch (IndexOutOfBoundsException e) {
			return new CSONArray();
		}

	}
	
	public CSONObject getObject(int index) {
		try {
			return DataConverter.toObject(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public CSONObject optObject(int index) {
		try {
			return DataConverter.toObject(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}




	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public int getInteger(int index) {
		return getInt(index);
	}

	public int getInt(int index) {
		try {
			return DataConverter.toInteger(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
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



	public int optInt(int index) {
		return optInt(index, 0);
	}

	public int optInt(int index, int def) {
		try {
			return DataConverter.toInteger(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public long getLong(int index) {
		try {
			return DataConverter.toLong(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public long optLong(int index) {
		return optLong(index, 0);
	}

	public long optLong(int index, long def) {
		try {
			return DataConverter.toLong(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public short getShort(int index) {
		try {			
			return DataConverter.toShort(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public short optShort(int index) {
		return optShort(index, (short)0);
	}
	
	public short optShort(int index, short def) {
		try {			
			return DataConverter.toShort(list.get(index),def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public char getChar(int index) {
		try {
			return DataConverter.toChar(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	@SuppressWarnings("unused")
	public char optChar(int index) {
		return optChar(index, '\0');
	}

	public char optChar(int index, char def) {
		try {
			return DataConverter.toChar(list.get(index),def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}


	public short getByte(int index) {
		try {
			return DataConverter.toByte(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	@SuppressWarnings("unused")
	public byte optByte(int index, byte def) {
		try {
			return DataConverter.toByte(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public byte optByte(int index) {
		try {
			return DataConverter.toByte(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}


	public double getDouble(int index) {
		try {
			return DataConverter.toDouble(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public double optDouble(int index) {
		return optDouble(index, 0);
	}


	public double optDouble(int index, double def) {
		try {
			return DataConverter.toDouble(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public float getFloat(int index) {
		try {			
			return DataConverter.toFloat(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	@SuppressWarnings("unused")
	public float optFloat(int index, float def) {
		try {			
			return DataConverter.toFloat(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public float optFloat(int index) {
		try {			
			return DataConverter.toFloat(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return Float.NaN;
		}
	}
	
	
	public String getString(int index) {
		try {			
			return DataConverter.toString(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public String optString(int index,String def) {
		try {			
			return DataConverter.toString(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public String optString(int index) {
		return optString(index, null);
	}
	
	public byte[] getByteArray(int index) {
		try {			
			@SuppressWarnings("UnnecessaryLocalVariable")
			byte[] buffer = (byte[]) list.get(index);
			return buffer;
		} catch (Exception e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public byte[] optByteArray(int index) {
		try {
			return DataConverter.toByteArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	public byte[] optByteArray(int index,byte[] def) {
		try {
			return DataConverter.toByteArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	
	public boolean getBoolean(int index) {
		try {			
			return DataConverter.toBoolean(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public boolean optBoolean(int index, boolean def) {
		try {			
			return DataConverter.toBoolean(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.containsAll(c);
	}


	@Override
	public boolean addAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.addAll(c);
	}

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
		
		
		public CSONArray getArray() {
			return array.getArray(index++);
		}

		@SuppressWarnings("unused")
		public CSONArray optArray() {
			return array.optArray(index++);
		}

		@SuppressWarnings("unused")
		public int getInteger() {
			return array.getInteger(index++);
		}

		@SuppressWarnings("unused")
		public int optInteger() {
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
	
	public byte[] toByteArray() {
		BinaryCSONWriter writer = new BinaryCSONWriter();
		write(writer);
		return writer.toByteArray();
	}
	
	@SuppressWarnings("ForLoopReplaceableByForEach")
	protected void write(BinaryCSONWriter writer) {
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
		if(root) {
			writer.writeComment(getCommentThis(), false,"","\n" );
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
			else if(obj instanceof byte[]) writer.add((byte[])obj);
			else if(obj instanceof BigDecimal) writer.add((BigDecimal)obj);
			else  writer.add(obj.toString());
		}

		writer.closeArray();
		if(root) {

			writer.writeComment(getCommentAfterThis(), false, "\n", "");
		}

	}

	
	@Override
	public String toString() {
		return toString(defaultJSONOptions);
	}

	public String toString(StringFormatOption stringFormatOption) {
		if(stringFormatOption instanceof JSONOptions) {
			JSONWriter jsonWriter  = new JSONWriter((JSONOptions) stringFormatOption);
			write(jsonWriter, true);
			return jsonWriter.toString();
		}
		return this.toString(StringFormatOption.json());
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
			else if(value instanceof Boolean && (!(compareValue instanceof Boolean) || (Boolean)value != (Boolean)compareValue)) {
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
			else if(value instanceof CSONArray && (!(compareValue instanceof CSONArray) || !((CSONArray)value).equals(compareValue))) {
				return false;
			}
			else if(value instanceof CSONObject && (!(compareValue instanceof CSONObject) || !((CSONObject)value).equals(compareValue))) {
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

}
