package com.hancomins.cson.container.cson;

import com.hancomins.cson.CSONElement;
import com.hancomins.cson.CSONParseException;
import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.container.*;
import com.hancomins.cson.container.json.ParsingState;
import com.hancomins.cson.util.ArrayStack;
import com.hancomins.cson.util.NullValue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BinaryCSONParser {

	private static final ValueCounter EMPTY_VALUE_COUNTER = new ValueCounter(0, false);

	private static int BUFFER_SIZE = 4096;

	private ArrayStack<List<CommentObject>> commentStack;
	private List<CommentObject<?>> currentCommentList = null;
	private ArrayStack<ValueCounter> containerValueCountStack = new ArrayStack<>();
	private ArrayStack<BaseDataContainer> containerStack = new ArrayStack<>();
	private ArrayStack<DataIterator<?>> iteratorStack = new ArrayStack<>();
	private ValueCounter currentContainerValueCount = EMPTY_VALUE_COUNTER;
	private BaseDataContainer currentContainer;

	private byte[] defaultBuffer = new byte[BUFFER_SIZE];

	private boolean hasComment = false;
	private boolean hasStringTable = false;
	private String[] stringTable;


	private String lastKey;

	private String headerComment;

	private ParsingState currentParsingState = ParsingState.Open;
	private DataInputStream dataInputStream;
	private ByteArrayOutputStream byteArrayOuputStream = new ByteArrayOutputStream();

	public static CSONElement parse(byte[] buffer) {
		BinaryCSONParseIterator binaryCsonParseIterator = new BinaryCSONParseIterator();
		BinaryCSONBufferReader.parse(buffer, binaryCsonParseIterator);
		return binaryCsonParseIterator.release();
	}

	public static CSONElement parse(ByteBuffer buffer) {
		BinaryCSONParseIterator binaryCsonParseIterator = new BinaryCSONParseIterator();
		BinaryCSONBufferReader.parse(buffer, binaryCsonParseIterator);
		return binaryCsonParseIterator.release();
	}

	public static CSONElement parse(byte[] buffer, int offset, int len) {
		BinaryCSONParseIterator binaryCsonParseIterator = new BinaryCSONParseIterator();
		BinaryCSONBufferReader.parse(buffer,offset,len, binaryCsonParseIterator);
		return binaryCsonParseIterator.release();
	}


	BaseDataContainer rootDataContainer;
	KeyValueDataContainerFactory keyValueDataContainerFactory;
	ArrayDataContainerFactory arrayDataContainerFactory;

	public BinaryCSONParser(KeyValueDataContainerFactory keyValueDataContainerFactory, ArrayDataContainerFactory arrayDataContainerFactory) {
		this.keyValueDataContainerFactory = keyValueDataContainerFactory;
		this.arrayDataContainerFactory = arrayDataContainerFactory;
	}



	int currentReadState;


	public BaseDataContainer parse(InputStream inputStream, BaseDataContainer rootDataContainer) throws IOException {
		if(rootDataContainer != null) {
			this.rootDataContainer = rootDataContainer;
		}
		dataInputStream = new DataInputStream(inputStream);
		currentReadState = readHeader();
		newContainer();
		int lastState = -1;
		do {
			if(currentContainerValueCount.isContains()) {
                if (currentContainer instanceof KeyValueDataContainer) {
					lastState = readKeyValueDataContainer();
                } else {
                    readArray();
                }
                if(currentReadState != -1) {
					newContainer();
				}
			} else {
				upParentContainer();
			}
		} while (!containerStack.isEmpty());
		if(hasComment) {
			readFooterComment();
		}
		int footer = dataInputStream.read();
		if(footer != CSONFlag.CSON_FOOTER) {
			throw new CSONParseException("Invalid CSON Footer");
		}

		return null;
	}

	private void upParentContainer() {
		try {
			if(hasComment) {
				readComments();
			}
			containerStack.pop();
			containerValueCountStack.pop();
			currentContainer = containerStack.top();
			if (currentContainer != null) {
				currentContainerValueCount = containerValueCountStack.top();
			}
		} catch (Exception e) {
			// todo : 메시지 수정
			throw new CSONParseException("End of Container", e);
		}
	}


	private int readCommentCount(int commentFlag) throws IOException {
		switch (commentFlag) {
			case CSONFlag.COMMENT_ZERO:
				return 0;
			case CSONFlag.COMMENT_UINT8:
				return dataInputStream.read() & 0xFF;
			case CSONFlag.COMMENT_UINT16:
				return dataInputStream.read() & 0xFFFF;
			case CSONFlag.COMMENT_UINT32:
				return dataInputStream.readInt();
			default:
				throw new CSONParseException("Invalid Comment Type");
		}
	}


	private void readComments() throws IOException {
		boolean arrayComments = currentContainer instanceof ArrayDataContainer;
		int commentFlag = dataInputStream.read();
		int commentCount = readCommentCount(commentFlag);
		for (int i = 0; i < commentCount; i++) {
			byte keyType = (byte)dataInputStream.read();
			CommentObject<?> commentObject;
			if(arrayComments) {

				try {
					int commentIndex = readInteger(keyType);
					commentObject = CommentObject.forArrayContainer(commentIndex);
				} catch (ClassCastException e) {
					throw new CSONParseException("Invalid Comment Index Type");
				}
			} else {
				String commentIndex = readKey(keyType);
				commentObject = CommentObject.forKeyValueContainer(commentIndex);

			}
			byte commentType = dataInputStream.readByte();
			if(commentType == (byte)CSONFlag.FOOTER_COMMENT) {
				currentContainer.setComment(commentObject);
				continue;
			}

			boolean beforeKey = (commentType & CSONFlag.COMMENT_TYPE_BEFORE_KEY) == CSONFlag.COMMENT_TYPE_BEFORE_KEY;
			boolean afterKey = (commentType & CSONFlag.COMMENT_TYPE_AFTER_KEY) == CSONFlag.COMMENT_TYPE_AFTER_KEY;
			boolean beforeValue = (commentType & CSONFlag.COMMENT_TYPE_BEFORE_VALUE) == CSONFlag.COMMENT_TYPE_BEFORE_VALUE;
			boolean afterValue = (commentType & CSONFlag.COMMENT_TYPE_AFTER_VALUE) == CSONFlag.COMMENT_TYPE_AFTER_VALUE;
			String beforeKeyComment = beforeKey ? readString() : null;
			String afterKeyComment = afterKey ? readString() : null;
			String beforeValueComment = beforeValue ? readString() : null;
			String afterValueComment = afterValue ? readString() : null;
			if(beforeKey)
				commentObject.setComment(CommentPosition.BEFORE_KEY, beforeKeyComment);
			if(afterKey)
				commentObject.setComment(CommentPosition.AFTER_KEY, afterKeyComment);
			if(beforeValue)
				commentObject.setComment(CommentPosition.BEFORE_VALUE, beforeValueComment);
			if(afterValue)
				commentObject.setComment(CommentPosition.AFTER_VALUE, afterValueComment);

			currentContainer.setComment(commentObject);
		}

	}





	private int readHeader() throws IOException {
		int value = dataInputStream.readInt();
		if(value != CSONFlag.CSON_HEADER) {
			throw new CSONParseException("Invalid CSON Header");
		}
		int version = dataInputStream.readShort();
		if(version != CSONFlag.CSON_VERSION) {
			throw new CSONParseException("Invalid CSON Version. Current version is " + CSONFlag.CSON_VERSION + " but read version is " + version);
		}
		short options = dataInputStream.readShort();
		if((options & CSONFlag.ENABLE_COMMENT) == CSONFlag.ENABLE_COMMENT) {
			hasComment = true;
			commentStack = new ArrayStack<>();
		}
		if((options & CSONFlag.ENABLE_STRING_TABLE) == CSONFlag.ENABLE_STRING_TABLE) {
			hasStringTable = true;
		}
		int state = dataInputStream.read();
		if(hasStringTable) {
			readStringTable(state);
			state = dataInputStream.read();
		}
		if(hasComment) {
			state = readHeaderComment(state);
		}
		return state;
	}

	private void readStringTable(int state) throws IOException {
		int tableSize;
		int type = state >> 4;
		if(type == CSONFlag.TYPE_ARRAY_LESS_THAN_16) {
			tableSize = state & 0x0F;
		} else {

			switch (state) {
				case CSONFlag.ARRAY_UINT8:
					tableSize = dataInputStream.read() & 0xFF;
					break;
				case CSONFlag.ARRAY_UINT16:
					tableSize = dataInputStream.readShort() & 0xFFFF;
					break;
				case CSONFlag.ARRAY_UINT32:
					tableSize = dataInputStream.readInt();
					break;
				default:
					throw new CSONParseException("Invalid String Table Type");
			}
		}
		stringTable = new String[tableSize];
		for (int i = 0; i < tableSize; i++) {
			stringTable[i] = readString();
		}
	}


	private int readHeaderComment(int state) throws IOException {
		String headerComment;
		if (state == CSONFlag.HEADER_COMMENT) {
			headerComment = readString();
		} else if(state == CSONFlag.COMMENT_ZERO) {
			headerComment = null;
		} else {
			throw new CSONParseException("Invalid Header Comment Type");
		}
		rootDataContainer.setComment(headerComment, CommentPosition.HEADER);
		return dataInputStream.read();
	}

	private void readFooterComment() throws IOException {
		int state = dataInputStream.readByte();
		if(state == (byte)CSONFlag.FOOTER_COMMENT) {
			String footerComment = readString();
			rootDataContainer.setComment(footerComment, CommentPosition.FOOTER);
		} else if(state != (byte)CSONFlag.COMMENT_ZERO) {
			throw new CSONParseException("Invalid Footer Comment Type");
		}
	}




	private void newContainer() throws IOException {
		int type = currentReadState >> 4;
		switch (type) {
			case CSONFlag.TYPE_OBJECT_LESS_THAN_16:
				newKeyValueContainer(new ValueCounter(currentReadState & 0x0F, true));
				break;
			case CSONFlag.TYPE_ARRAY_LESS_THAN_16:
				newArrayContainer(new ValueCounter(currentReadState & 0x0F, false));
				break;
			case CSONFlag.TYPE_OBJECT:
				int containerValueCount = readObjectCount(currentReadState);
				if (currentReadState > CSONFlag.OBJECT_UINT32) {
					newArrayContainer(new ValueCounter(containerValueCount, true));
				} else {
					newKeyValueContainer(new ValueCounter(containerValueCount, false));
				}
				break;
		}
	}



	@SuppressWarnings("DuplicatedCode")
	private void newArrayContainer(ValueCounter valueCount) {
		BaseDataContainer nextContainer;
		if(currentContainer == null) {
			nextContainer = initRootContainer();
		}
		else if(lastKey != null) {
			nextContainer =  arrayDataContainerFactory.create();
			((KeyValueDataContainer) currentContainer).put(lastKey, nextContainer);
			lastKey = null;
		} else {
			nextContainer =  arrayDataContainerFactory.create();
			((ArrayDataContainer) currentContainer).add(nextContainer);
		}
		pushContainer(nextContainer, valueCount);
	}

	@SuppressWarnings("DuplicatedCode")
    private void newKeyValueContainer(ValueCounter valueCount) {
		BaseDataContainer nextContainer;
		if(currentContainer == null) {
			nextContainer = initRootContainer();
		}
		else if(lastKey != null) {
			nextContainer =  keyValueDataContainerFactory.create();
			((KeyValueDataContainer) currentContainer).put(lastKey, nextContainer);
			lastKey = null;
		} else {
			nextContainer =  keyValueDataContainerFactory.create();
			((ArrayDataContainer) currentContainer).add(nextContainer);
		}
		pushContainer(nextContainer, valueCount);
	}

	private void pushContainer(BaseDataContainer container, ValueCounter valueCount) {
		containerStack.push(container);
		containerValueCountStack.push(valueCount);
		currentContainer = container;
		currentContainerValueCount = valueCount;
	}

	private BaseDataContainer initRootContainer() {
		BaseDataContainer nextContainer;
		if(rootDataContainer == null) {
			nextContainer =  keyValueDataContainerFactory.create();
		} else {
			nextContainer = rootDataContainer;
		}
		return nextContainer;
	}





	/**
	 * 현재 Container 에서 Key, Value 쌍을 읽어서 KeyValueDataContainer 에 저장한다.
	 * @throws IOException IO Exception
	 */
	private int readKeyValueDataContainer() throws IOException {
		while (currentContainerValueCount.isContains()) {
			currentContainerValueCount.decrease();
			String key = readKey();
			int state = dataInputStream.read();
			if(CSONFlag.OBJECT_LESS_THAN_16 <= state &&  state < CSONFlag.HEADER_COMMENT) {
				lastKey = key;
				currentReadState = state;
				return currentReadState;
			}
			Object value = readValue(state);
			((KeyValueDataContainer)currentContainer).put(key, value);
		}
		upParentContainer();
		int state = currentReadState;
		currentReadState = -1;
		return state;
	}

	private String readKey() throws IOException {
		int keyType = dataInputStream.read();
		return readKey(keyType);
	}

	private String readKey(int keyType) throws IOException {
		if(hasStringTable) {
			int index;
			switch (keyType) {
				case CSONFlag.INT8:
					index = dataInputStream.readByte() & 0xFF;
					break;
				case CSONFlag.INT16:
					index = dataInputStream.readShort() & 0xFFFF;
					break;
				case CSONFlag.INT32:
					index = dataInputStream.readInt();
					break;
				default:
					throw new CSONParseException("Invalid String Table Index Type");
			}
			return stringTable[index];
		} else {
			return readString(keyType);
		}
	}

	private void readArray() throws IOException {
		while (currentContainerValueCount.isContains()) {
			currentContainerValueCount.decrease();
			int state = dataInputStream.read();
			if(CSONFlag.OBJECT_LESS_THAN_16 <= state &&  state < CSONFlag.HEADER_COMMENT) {
				currentReadState = state;
				return;
			}
			Object value = readValue(state);
			((ArrayDataContainer)currentContainer).add(value);
		}
		upParentContainer();
		currentReadState = -1;
	}

	private int readObjectCount(int state) throws IOException {
		int currentContainerValueCount;
		switch (state) {
			case CSONFlag.OBJECT_UINT8:
            case CSONFlag.ARRAY_UINT8:
                currentContainerValueCount = dataInputStream.read() & 0xFF;
				break;
			case CSONFlag.OBJECT_UINT16:
            case CSONFlag.ARRAY_UINT16:
                currentContainerValueCount = dataInputStream.readShort() & 0xFFFF;
				break;
			case CSONFlag.OBJECT_UINT32:
            case CSONFlag.ARRAY_UINT32:
                currentContainerValueCount = dataInputStream.readInt();
				break;
            default:
				throw new CSONParseException("Invalid Container length type");
		}
		return currentContainerValueCount;
	}

	private Object readNumerable(int state) throws IOException {
		switch (state) {
			case CSONFlag.BIG_INT:
				String bigIntValue = readString();
				return new BigInteger(bigIntValue);
			case CSONFlag.INT8:
				return dataInputStream.readByte();
			case CSONFlag.INT16:
				return dataInputStream.readShort();
			case CSONFlag.INT32:
				return dataInputStream.readInt();
			case CSONFlag.INT64:
				return dataInputStream.readLong();
			case CSONFlag.INT_CHAR:
				return (char)dataInputStream.readShort();
			default:
				throw new CSONParseException("Invalid Integer Type");
		}
	}

	private int readInteger(int state) throws IOException {
		Object value = readNumerable(state);
		if(value instanceof Number) {
			return ((Number)value).intValue();
		} else if(value instanceof Character) {
			return (Character)value;
		} else {
			throw new CSONParseException("Invalid Integer Type");
		}
	}


	private Number readFloat(int state) throws IOException {
		switch (state) {
			case CSONFlag.BIG_DEC:
				String bigDecValue = readString();
				return new BigDecimal(bigDecValue);
			case CSONFlag.DEC32:
				return dataInputStream.readFloat();
			case CSONFlag.DEC64:
				return dataInputStream.readDouble();
			default:
				throw new CSONParseException("Invalid Float Type");
		}
	}

	private String readString() throws IOException {
		int stringLengthType = dataInputStream.read();
		return readString(stringLengthType);
	}
	private String readString(int stringLengthType) throws IOException {
		int stringType = stringLengthType >> 4;
		int lengthInt32 = 0;
		if (stringType == CSONFlag.TYPE_STRING_LESS_THAN_16) {
			lengthInt32 = stringLengthType & 0x0F;
		} else {
			lengthInt32 = readStringLength(stringLengthType);
		}
		byte[] buffer;
		if(lengthInt32 < BUFFER_SIZE) {
			buffer = defaultBuffer;
		} else {
			buffer = new byte[lengthInt32];
		}
		dataInputStream.readFully(buffer, 0, lengthInt32);
		return new String(buffer,0, lengthInt32, StandardCharsets.UTF_8);
	}

	private String readStringMoreThan15(int state) throws IOException {
		int lengthInt32 = readStringLength(state);
		byte[] buffer;
		if(lengthInt32 < BUFFER_SIZE) {
			buffer = defaultBuffer;
		} else {
			buffer = new byte[lengthInt32];
		}
		dataInputStream.readFully(buffer, 0, lengthInt32);
		return new String(buffer,0, lengthInt32, StandardCharsets.UTF_8);
	}


	private String readStringLessThan16(int state) throws IOException {
		int length = state & 0x0F;
		dataInputStream.readFully(defaultBuffer, 0, length);
		return new String(defaultBuffer,0, length, StandardCharsets.UTF_8);
	}

	private int readStringLength(int stringType) throws IOException {
		int lengthInt32;
		switch (stringType) {
			case CSONFlag.STRING_UINT8:
				lengthInt32 = dataInputStream.read() & 0xFF;
				break;
			case CSONFlag.STRING_UINT16:
				lengthInt32 = dataInputStream.readShort() & 0xFFFF;
				break;
			case CSONFlag.STRING_UINT32:
				lengthInt32 = dataInputStream.readInt();
				break;
			default:
				throw new CSONParseException("Invalid String length type");
		}
		return lengthInt32;
	}


	private byte[] readByteBuffer(int state) throws IOException {
		int length;
		switch (state) {
			case CSONFlag.BYTE_BUFFER_UINT8:
				length = dataInputStream.read() & 0xFF;
				break;
			case CSONFlag.BYTE_BUFFER_UINT16:
				length = dataInputStream.readShort() & 0xFFFF;
				break;
			case CSONFlag.BYTE_BUFFER_UINT32:
				length = dataInputStream.readInt();
				break;
			default:
				throw new CSONParseException("Invalid ByteBuffer length type");
		}
		byte[] buffer = new byte[length];
		dataInputStream.readFully(buffer);
		return buffer;
	}


	private Object readStringOrByteBuffer(int state) throws IOException {
		switch (state) {
			case CSONFlag.STRING_UINT8:
			case CSONFlag.STRING_UINT16:
			case CSONFlag.STRING_UINT32:
				return readStringMoreThan15(state);
			case CSONFlag.BYTE_BUFFER_UINT8:
			case CSONFlag.BYTE_BUFFER_UINT16:
			case CSONFlag.BYTE_BUFFER_UINT32:
				return readByteBuffer(state);
			default:
				throw new CSONParseException("Invalid String or ByteBuffer length type");
		}
	}

	private Object readValue(int state) throws IOException {
		int type = state >> 4;

		switch (type) {
			case CSONFlag.TYPE_FIXED_VALUE:
				return readFixedValue(state);
			case CSONFlag.TYPE_INTEGER:
				return readNumerable(state);
			case CSONFlag.TYPE_FLOAT:
				return readFloat(state);
			case CSONFlag.TYPE_STRING_LESS_THAN_16:
				return readStringLessThan16(state);
			case CSONFlag.TYPE_STRING_OR_BYTE_BUFFER:
				return readStringOrByteBuffer(state);
			default:
				throw new CSONParseException("Invalid Value Type");
		}

	}



	private Object readFixedValue(int state) {
		switch (state) {
			case CSONFlag.NULL:
				return NullValue.Instance;
			case CSONFlag.EMPTY:
				return "";
			case CSONFlag.TRUE:
				return Boolean.TRUE;
			case CSONFlag.FALSE:
				return Boolean.FALSE;
			case CSONFlag.NAN:
				return Double.NaN;
			case CSONFlag.INFINITY:
				return Double.POSITIVE_INFINITY;
			case CSONFlag.NEGATIVE_INFINITY:
				return Double.NEGATIVE_INFINITY;
			default:
				throw new CSONParseException("Invalid Value Type");
		}
	}


	private static class ValueCounter {
		int value;
		int size;
		boolean isKeyValueContainer;

		ValueCounter(int value, boolean isKeyValueContainer) {
			this.value = value;
			this.size = value;
			this.isKeyValueContainer = isKeyValueContainer;
		}

		boolean isBegin() {
			return size == value && size > 0;
		}

		private boolean isContains() {
			return value > 0;
		}


		private void decrease() {
			value--;
		}

		private boolean isEmpty() {
			return value < 1;
		}

	}







}
