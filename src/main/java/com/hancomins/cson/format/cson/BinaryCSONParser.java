package com.hancomins.cson.format.cson;

import com.hancomins.cson.CSONElement;
import com.hancomins.cson.format.*;
import com.hancomins.cson.format.json.ParsingState;
import com.hancomins.cson.util.ArrayStack;
import com.hancomins.cson.util.NullValue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryCSONParser {

	private static int BUFFER_SIZE = 4096;

	private ArrayStack<ValueCounter> containerValueCountStack = new ArrayStack<>();
	private ArrayStack<BaseDataContainer> containerStack = new ArrayStack<>();
	private ArrayStack<DataIterator<?>> iteratorStack = new ArrayStack<>();
	private ValueCounter currentContainerValueCount;
	private BaseDataContainer currentContainer;

	private byte[] defaultBuffer = new byte[BUFFER_SIZE];

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
		String key = null;
		do {
			int childContainerState;
			if(currentContainerValueCount != null && !currentContainerValueCount.isBegin()) {
				childContainerState = readKeyValueDataContainer();
			} else {
				childContainerState = readContainerState(key);
			}
            if (childContainerState == -1) {
                upParentContainer();
				if (currentContainer == null) {
					break;
				}
            } else {
                currentReadState = childContainerState;
            }
		} while (!containerStack.isEmpty());

		return null;
	}

	private void upParentContainer() {
		containerStack.pop();
		containerValueCountStack.pop();
		currentContainer = containerStack.top();
		if(currentContainer != null) {
			currentContainerValueCount = containerValueCountStack.top();
		}
	}

	private int readHeader() throws IOException {
		int value = dataInputStream.readInt();
		if(value != CSONFlags.CSON_HEADER) {
			throw new CSONParseException("Invalid CSON Header");
		}
		int version = dataInputStream.readShort();
		if(version != CSONFlags.CSON_VERSION) {
			throw new CSONParseException("Invalid CSON Version. Current version is " + CSONFlags.CSON_VERSION + " but read version is " + version);
		}

		int state = dataInputStream.read();
		if (state == CSONFlags.HEADER_COMMENT) {
			this.headerComment = readString();
			state = dataInputStream.read();
		}
		return state;
	}

	private int readContainerState(String key) throws IOException {

		int type = currentReadState >> 4;
		int containerValueCount = 0;
		switch (type) {
			case CSONFlags.TYPE_OBJECT_LESS_THAN_16:
				newObject( new ValueCounter(currentReadState & 0x0F),  true);
				return readKeyValueDataContainer();
			case CSONFlags.TYPE_ARRAY_LESS_THAN_16:
				newObject(new ValueCounter(currentReadState & 0x0F),  false);
				return readArray();
			case CSONFlags.TYPE_OBJECT:
				containerValueCount = readObjectCount(currentReadState);
				if(containerValueCount < 0) {
					containerValueCount = -containerValueCount;
					newObject(new ValueCounter(containerValueCount), false);
					return readArray();
				} else {
					newObject(new ValueCounter(containerValueCount), true);
					return readKeyValueDataContainer();
				}
			case CSONFlags.TYPE_COMMENT:
				boolean isObjectComment = false;
				int commentLengthCount = 0;
				switch (currentReadState) {
					case CSONFlags.OBJECT_COMMENT_UINT8:
						isObjectComment = true;
						commentLengthCount = dataInputStream.read() & 0xFF;
						break;
					case CSONFlags.OBJECT_COMMENT_UINT16:
						isObjectComment = true;
						commentLengthCount = dataInputStream.readShort() & 0xFFFF;
						break;
					case CSONFlags.OBJECT_COMMENT_UINT32:
						isObjectComment = true;
						commentLengthCount = dataInputStream.readInt();
						break;
					case CSONFlags.ARRAY_COMMENT_UINT8:
						commentLengthCount = dataInputStream.read() & 0xFF;
						break;
					case CSONFlags.ARRAY_COMMENT_UINT16:
						commentLengthCount = dataInputStream.readShort() & 0xFFFF;
						break;
					case CSONFlags.ARRAY_COMMENT_UINT32:
						commentLengthCount = dataInputStream.readInt();
						break;
					default:
						throw new CSONParseException("Invalid Comment length type");
				}
				break;
		}
		return -1;
	}


	private void newObject(ValueCounter valueCount,boolean isObject) {
		BaseDataContainer nextContainer;
		if(currentContainer == null) {
			if(rootDataContainer == null) {
				nextContainer =  isObject ? keyValueDataContainerFactory.create() : arrayDataContainerFactory.create();
			} else {
				nextContainer = rootDataContainer;
			}
		}
		else if(lastKey != null) {
			nextContainer =  isObject ? keyValueDataContainerFactory.create() : arrayDataContainerFactory.create();
			((KeyValueDataContainer) currentContainer).put(lastKey, nextContainer);
			lastKey = null;
		} else {
			nextContainer =  isObject ? keyValueDataContainerFactory.create() : arrayDataContainerFactory.create();
			((ArrayDataContainer) currentContainer).add(nextContainer);
		}
		containerStack.push(nextContainer);
		containerValueCountStack.push(valueCount);
		currentContainer = nextContainer;
		currentContainerValueCount = valueCount;
	}


	/**
	 * 현재 Container 에서 Key, Value 쌍을 읽어서 KeyValueDataContainer 에 저장한다.
	 * @return 자식 Container 가 더 이상 없다면 -1을 반환하고, 아니라면 중간에 끊고 자식 Container 의 상태를 반환한다.
	 * @throws IOException
	 */
	private int readKeyValueDataContainer() throws IOException {
		while (currentContainerValueCount.isContains()) {
			currentContainerValueCount.decrease();
			String key = readString();
			int state = dataInputStream.read();
			if(CSONFlags.OBJECT_LESS_THAN_16 <= state &&  state < CSONFlags.HEADER_COMMENT) {
				lastKey = key;
				currentContainerValueCount = null;
				return state;
			}
			Object value = readValue(state);
			((KeyValueDataContainer)currentContainer).put(key, value);
		}
		return -1;
	}

	private int readArray() throws IOException {
		while (currentContainerValueCount.isContains()) {
			String key = readString();
			int state = dataInputStream.read();
			if(CSONFlags.OBJECT_LESS_THAN_16 <= state &&  state < CSONFlags.HEADER_COMMENT) {
				return state;
			}
			Object value = readValue(state);
			((KeyValueDataContainer)currentContainer).put(key, value);
		}
		return -1;
	}

	private int readObjectCount(int state) throws IOException {
		boolean isObject = false;
		int currentContainerValueCount;
		switch (state) {
			case CSONFlags.OBJECT_UINT8:
				currentContainerValueCount = dataInputStream.read() & 0xFF;
				isObject = true;
				break;
			case CSONFlags.OBJECT_UINT16:
				currentContainerValueCount = dataInputStream.readShort() & 0xFFFF;
				isObject = true;
				break;
			case CSONFlags.OBJECT_UINT32:
				currentContainerValueCount = dataInputStream.readInt();
				isObject = true;
				break;
			case CSONFlags.ARRAY_UINT8:
				currentContainerValueCount = dataInputStream.read() & 0xFF;
				break;
			case CSONFlags.ARRAY_UINT16:
				currentContainerValueCount = dataInputStream.readShort() & 0xFFFF;
				break;
			case CSONFlags.ARRAY_UINT32:
				currentContainerValueCount = dataInputStream.readInt();
				break;
			default:
				throw new CSONParseException("Invalid Container length type");
		}
		// 만약 Object type 이라면 양수로 반환하지만, Array type 이라면 음수로 반환한다.
		return isObject ? currentContainerValueCount : -currentContainerValueCount;
	}

	private Number readInteger(int state) throws IOException {
		switch (state) {
			case CSONFlags.BIG_INT:
				String bigIntValue = readString();
				return new BigInteger(bigIntValue);
			case CSONFlags.INT8:
				return (byte)dataInputStream.read();
			case CSONFlags.INT16:
				return dataInputStream.readShort();
			case CSONFlags.INT32:
				return dataInputStream.readInt();
			case CSONFlags.INT64:
				return dataInputStream.readLong();
			default:
				throw new CSONParseException("Invalid Integer Type");
		}
	}

	private Number readFloat(int state) throws IOException {
		switch (state) {
			case CSONFlags.BIG_DEC:
				String bigDecValue = readString();
				return new BigInteger(bigDecValue);
			case CSONFlags.DEC32:
				return dataInputStream.readFloat();
			case CSONFlags.DEC64:
				return dataInputStream.readDouble();
			default:
				throw new CSONParseException("Invalid Float Type");
		}
	}


	private String readString() throws IOException {
		int stringLengthType = dataInputStream.read();
		int stringType = stringLengthType >> 4;
		int lengthInt32 = 0;
		if (stringType == CSONFlags.TYPE_STRING_LESS_THAN_16) {
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
			case CSONFlags.STRING_UINT8:
				lengthInt32 = dataInputStream.read() & 0xFF;
				break;
			case CSONFlags.STRING_UINT16:
				lengthInt32 = dataInputStream.readShort() & 0xFFFF;
				break;
			case CSONFlags.STRING_UINT32:
				lengthInt32 = dataInputStream.readInt();
				break;
			default:
				throw new CSONParseException("Invalid String length type");
		}
		return lengthInt32;
	}


	private byte[] readByteBuffer(int state) throws IOException {
		int length = 0;
		switch (state) {
			case CSONFlags.BYTE_BUFFER_UINT8:
				length = dataInputStream.read() & 0xFF;
				break;
			case CSONFlags.BYTE_BUFFER_UINT16:
				length = dataInputStream.readShort() & 0xFFFF;
				break;
			case CSONFlags.BYTE_BUFFER_UINT32:
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
			case CSONFlags.STRING_UINT8:
			case CSONFlags.STRING_UINT16:
			case CSONFlags.STRING_UINT32:
				return readStringMoreThan15(state);
			case CSONFlags.BYTE_BUFFER_UINT8:
			case CSONFlags.BYTE_BUFFER_UINT16:
			case CSONFlags.BYTE_BUFFER_UINT32:
				return readByteBuffer(state);
			default:
				throw new CSONParseException("Invalid String or ByteBuffer length type");
		}
	}

	private Object readValue(int state) throws IOException {
		int type = state >> 4;

		switch (type) {
			case CSONFlags.TYPE_FIXED_VALUE:
				return readFixedValue(state);
			case CSONFlags.TYPE_INTEGER:
				return readInteger(state);
			case CSONFlags.TYPE_FLOAT:
				return readFloat(state);
			case CSONFlags.TYPE_STRING_LESS_THAN_16:
				return readStringLessThan16(state);
			case CSONFlags.TYPE_STRING_OR_BYTE_BUFFER:
				return readStringOrByteBuffer(state);
			default:
				throw new CSONParseException("Invalid Value Type");
		}

	}



	private Object readFixedValue(int state) {
		switch (state) {
			case CSONFlags.NULL:
				return NullValue.Instance;
			case CSONFlags.EMPTY:
				return "";
			case CSONFlags.TRUE:
				return Boolean.TRUE;
			case CSONFlags.FALSE:
				return Boolean.FALSE;
			case CSONFlags.NAN:
				return Double.NaN;
			case CSONFlags.INFINITY:
				return Double.POSITIVE_INFINITY;
			case CSONFlags.NEGATIVE_INFINITY:
				return Double.NEGATIVE_INFINITY;
			default:
				throw new CSONParseException("Invalid Value Type");
		}
	}


	private static class ValueCounter {
		int value;
		int size;

		ValueCounter(int value) {
			this.value = value;
			this.size = value;
		}

		boolean isBegin() {
			return size == value;
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
