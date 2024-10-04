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
import java.util.PrimitiveIterator;

public class BinaryCSONParser {


	private ArrayStack<Integer> containerValueCountStack = new ArrayStack<>();
	private ArrayStack<BaseDataContainer> containerStack = new ArrayStack<>();
	private int currentContainerValueCount = 0;
	private BaseDataContainer currentContainer;

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

	BinaryCSONParser(KeyValueDataContainerFactory keyValueDataContainerFactory, ArrayDataContainerFactory arrayDataContainerFactory) {
		this.keyValueDataContainerFactory = keyValueDataContainerFactory;
		this.arrayDataContainerFactory = arrayDataContainerFactory;
	}





	public BaseDataContainer parse(InputStream inputStream, BaseDataContainer rootDataContainer) throws IOException {
		dataInputStream = new DataInputStream(inputStream);
		readHeader();


		int v = -1;
		String key = null;
		while((v = inputStream.read()) != -1) {
			readContainerState(v, key);


		}

		return null;

	}

	private void readHeader() throws IOException {
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
		}
	}

	private void readContainerState(int state, String key) throws IOException {
		int type = state >> 4;
		int containerValueCount = 0;
		switch (type) {
			case CSONFlags.TYPE_OBJECT_LESS_THAN_16:
				newObject(state & 0x0F, null, true);
				readObject();
			case CSONFlags.TYPE_ARRAY_LESS_THAN_16:
				newObject(state & 0x0F, null, false);
				readArray();
				break;
			case CSONFlags.TYPE_OBJECT:
				containerValueCount = readObjectCount(state);
				if(containerValueCount < 0) {
					containerValueCount = -containerValueCount;
					newObject(containerValueCount, null, false);
					readArray();
				} else {
					newObject(containerValueCount, null, true);
					readObject();
				}
				break;
			case CSONFlags.TYPE_COMMENT:
				boolean isObjectComment = false;
				int commentLengthCount = 0;
				switch (state) {
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
	}


	private void newObject(int valueCount,String key, boolean isObject) {
		BaseDataContainer newContainer = isObject ? keyValueDataContainerFactory.create() : arrayDataContainerFactory.create();
		if(currentContainer instanceof ArrayDataContainer) {
			((ArrayDataContainer) currentContainer).add(newContainer);
		} else if(key != null) {
			((KeyValueDataContainer) currentContainer).put(key, newContainer);
		}
		containerStack.push(newContainer);
		containerValueCountStack.push(valueCount);
		currentContainer = newContainer;
		currentContainerValueCount = valueCount;
	}



	private void readObject() throws IOException {
		for(int i = 0; i < currentContainerValueCount; i++) {
			String key = readString();
			int state = dataInputStream.read();
			Object value = null;
		}

	}

	private void readArray() throws IOException {

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
		int stringType = dataInputStream.read();
		long lengthInt32 = 0;
		if (stringType == CSONFlags.TYPE_STRING_LESS_THAN_16) {
			lengthInt32 = stringType & 0x0F;
		} else {
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
		}
		byte[] buffer = new byte[(int) lengthInt32];
		dataInputStream.readFully(buffer);
		return new String(buffer, StandardCharsets.UTF_8);
	}

	private Object readValue() throws IOException {
		int state = dataInputStream.read();
		int type = state >> 4;

		switch (type) {
			case CSONFlags.TYPE_FIXED_VALUE:
				return readFixedValue(state);
			case CSONFlags.TYPE_INTEGER:
				return readInteger(state);
			case CSONFlags.TYPE_FLOAT:
				return readFloat(state);
			case CSONFlags.TYPE_STRING_LESS_THAN_16:
				return readBinary(state);
			case CSONFlags.TYPE_BYTE_BUFFER:
				return readObject(state);

		}

		return null;
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






}
