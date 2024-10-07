package com.hancomins.cson.format.cson;

import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.ArrayDataContainer;
import com.hancomins.cson.format.BaseDataContainer;
import com.hancomins.cson.format.DataIterator;
import com.hancomins.cson.format.KeyValueDataContainer;
import com.hancomins.cson.util.ArrayStack;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;


@SuppressWarnings("UnusedReturnValue")
public class BinaryCSONWriter {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	

	private final ArrayStack<DataIterator<?>> dataContainerIteratorStack = new ArrayStack<>();
	private final ArrayStack<BaseDataContainer> dataContainerStack = new ArrayStack<>();
	private final OutputStream outputStream;
	private final DataOutputStream dataOutputStream;
	private DataIterator<?> currentIterator;
	private BaseDataContainer currentDataContainer;



	@SuppressWarnings("unchecked")
	public void write(BaseDataContainer dataContainer) throws IOException {
		BaseDataContainer rootContainer = dataContainer;
		writePrefix();
		String header = dataContainer.getComment(CommentPosition.HEADER);
		writeHeaderComment(header);
		currentDataContainer = dataContainer;
		this.currentIterator = dataContainer.iterator();
		dataContainerIteratorStack.push(currentIterator);
		dataContainerStack.push(currentDataContainer);



		LOOP:
		while(!dataContainerIteratorStack.isEmpty()) {
			if(this.currentIterator.isBegin()) {
				writeContainerPrefix(this.currentIterator);
			}

			while (this.currentIterator.hasNext()) {
				if (this.currentIterator.isKeyValue()) {
					String key = writeObject((DataIterator<Map.Entry<String, Object>>)this.currentIterator);
					if(key != null) {
						continue LOOP;
					}
				} else {
					if(writeArray(this.currentIterator)) {
						break;
					}
				}
			}
			BaseDataContainer oldDataContainer = currentDataContainer;
			dataContainerIteratorStack.pop();
			dataContainerStack.pop();
			currentIterator =  dataContainerIteratorStack.top();
			currentDataContainer =  dataContainerStack.top();
			if(oldDataContainer instanceof KeyValueDataContainer) {
				writeObjectSuffix();
			} else if(oldDataContainer instanceof ArrayDataContainer) {
				writeArraySuffix();
			}

		}

		String footerComment = rootContainer.getComment(CommentPosition.FOOTER);
		writeFooterComment(footerComment);

		writeSuffix();
	}

	@SuppressWarnings("unchecked")
    private void writeContainerPrefix(DataIterator<?> iterator) throws IOException {
		if(iterator.isKeyValue()) {
			writeObjectPrefix((DataIterator<Map.Entry<String, Object>>) iterator);
		} else {
			writeArrayPrefix(iterator);
		}
	}


	public BinaryCSONWriter() {
		this.outputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
		this.dataOutputStream = new DataOutputStream(outputStream);
	}


	public BinaryCSONWriter(OutputStream outputStream) {
		this.outputStream = new BufferedOutputStream(outputStream, DEFAULT_BUFFER_SIZE);
		this.dataOutputStream = new DataOutputStream(outputStream);
	}

	public void write(int value) throws IOException {
		dataOutputStream.writeInt(value);
	}

	private void writeHeaderComment(String comment) throws IOException {
		if(comment == null) {
			return;
		}
		dataOutputStream.writeByte(CSONFlags.HEADER_COMMENT);
		writeString(comment);
	}

	private void writeFooterComment(String comment) throws IOException {
		if(comment == null) {
			return;
		}
		dataOutputStream.writeByte(CSONFlags.FOOTER_COMMENT);
		writeString(comment);

	}

	private void writeString(String value) throws IOException {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		int length = bytes.length;
		if(length < 16) {
			int flag = CSONFlags.STRING_LESS_THAN_16 | length;
			dataOutputStream.writeByte(flag);
		} else if(length < 256) {
			dataOutputStream.writeByte(CSONFlags.STRING_UINT8);
			dataOutputStream.writeByte(length);
		} else if(length < 65536) {
			dataOutputStream.writeByte(CSONFlags.STRING_UINT16);
			dataOutputStream.writeShort(length);
		} else {
			dataOutputStream.writeByte(CSONFlags.STRING_UINT32);
			dataOutputStream.writeInt(length);
		}
		dataOutputStream.write(bytes);
	}

	public void writeBuffer(byte[] buffer) throws IOException {
		int length = buffer.length;
		if(length < 256) {
			dataOutputStream.writeByte(CSONFlags.BYTE_BUFFER_UINT8);
			dataOutputStream.writeByte(length);
		} else if(length < 65536) {
			dataOutputStream.writeByte(CSONFlags.BYTE_BUFFER_UINT16);
			dataOutputStream.writeShort(length);
		} else {
			dataOutputStream.writeByte(CSONFlags.BYTE_BUFFER_UINT32);
			dataOutputStream.writeInt(length);
		}
		dataOutputStream.write(buffer);

	}

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		dataOutputStream.writeShort(65535);
		dataOutputStream.flush();
		byte[] bytes = byteArrayOutputStream.toByteArray();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
		System.out.println(dataInputStream.readShort() & 0xFFFF);
	}

	private void writePrefix() throws IOException {
		dataOutputStream.writeInt(CSONFlags.CSON_HEADER);
		dataOutputStream.writeShort(CSONFlags.CSON_VERSION);
	}

	protected void writeSuffix() throws IOException {
		dataOutputStream.flush();
		dataOutputStream.close();
	}


	private void intoChildBaseDataContainer(BaseDataContainer value) {
		currentDataContainer = value;
		dataContainerStack.push(currentDataContainer);
		currentIterator = currentDataContainer.iterator();
		dataContainerIteratorStack.push(currentIterator);
	}

	public void writeArrayPrefix(DataIterator<?> iterator) throws IOException {
		int size = iterator.size();
		if(size < 16) {
			dataOutputStream.writeByte(CSONFlags.ARRAY_LESS_THAN_16 | size);
		} else if(size < 256) {
			dataOutputStream.writeByte(CSONFlags.ARRAY_UINT8);
			dataOutputStream.writeByte(size);
		} else if(size < 65536) {
			dataOutputStream.writeByte(CSONFlags.ARRAY_UINT16);
			dataOutputStream.writeShort(size);
		} else {
			dataOutputStream.writeByte(CSONFlags.ARRAY_UINT32);
			dataOutputStream.writeInt(size);
		}
	}

	private void writeObjectPrefix(DataIterator<Map.Entry<String, Object>> iterator) throws IOException {
		int size = iterator.size();
		if(size < 16) {
			dataOutputStream.writeByte(CSONFlags.OBJECT_LESS_THAN_16 | size);
		} else if(size < 256) {
			dataOutputStream.writeByte(CSONFlags.OBJECT_UINT8);
			dataOutputStream.writeByte(size);
		} else if(size < 65536) {
			dataOutputStream.writeByte(CSONFlags.OBJECT_UINT16);
			dataOutputStream.writeShort(size);
		} else {
			dataOutputStream.writeByte(CSONFlags.OBJECT_UINT32);
			dataOutputStream.writeInt(size);
		}
	}

	protected void writeObjectSuffix() throws IOException {

	}

	protected void writeArraySuffix() throws IOException {

	}

	private void writeKey(String key) throws IOException {
		writeString(key);
	}

	private void writeValue(Object value) throws IOException {
		if(value instanceof String) {
			writeString((String)value);
		} else if(value instanceof Boolean) {
			if(value == Boolean.TRUE) {
				dataOutputStream.writeByte(CSONFlags.TRUE);
			} else {
				dataOutputStream.writeByte(CSONFlags.FALSE);
			}
		} else if(value instanceof Integer) {
			dataOutputStream.writeByte(CSONFlags.INT32);
			dataOutputStream.writeInt((Integer)value);
		} else if(value instanceof Long) {
			dataOutputStream.writeByte(CSONFlags.INT64);
			dataOutputStream.writeLong((Long)value);
		} else if(value instanceof Float) {
			dataOutputStream.writeByte(CSONFlags.DEC32);
			dataOutputStream.writeFloat((Float)value);
		} else if(value instanceof Double) {
			dataOutputStream.writeByte(CSONFlags.DEC64);
			dataOutputStream.writeDouble((Double)value);
		} else if(value instanceof Short) {
			dataOutputStream.writeByte(CSONFlags.INT16);
			dataOutputStream.writeShort((Short)value);
		} else if(value instanceof Byte) {
			dataOutputStream.writeByte(CSONFlags.INT8);
			dataOutputStream.writeByte((Byte)value);
		} else if(value instanceof Character) {
			dataOutputStream.writeByte(CSONFlags.INT16);
			dataOutputStream.writeChar((Character)value);
		} else if(value instanceof BigInteger) {
			dataOutputStream.writeByte(CSONFlags.BIG_INT);
			BigInteger bigInteger = (BigInteger)value;
			String integerString = bigInteger.toString();
			writeString(integerString);
		}
		else if(value instanceof BigDecimal) {
			dataOutputStream.writeByte(CSONFlags.BIG_DEC);
			BigDecimal bigDecimal = (BigDecimal)value;
			String decimalString = bigDecimal.toString();
			writeString(decimalString);
		} else if(value instanceof Number) {
			dataOutputStream.writeByte(CSONFlags.BIG_DEC);
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			String decimalString = bigDecimal.toString();
			writeString(decimalString);
		}
		else if(value instanceof byte[]) {
			writeBuffer((byte[])value);
		}
	}


	private boolean writeArray(DataIterator<?> iterator) throws IOException {
		if (this.currentIterator.isBegin()) {
			writeArrayPrefix(this.currentIterator);
		}
		Object value = this.currentIterator.next();
		if (value instanceof BaseDataContainer) {
			intoChildBaseDataContainer((BaseDataContainer)value);
			return false;
		}
		writeValue(value);
		return true;
	}

	private String writeObject(DataIterator<Map.Entry<String, Object>> iterator) throws IOException {
		Map.Entry<String, Object> entry = iterator.next();
		String key = entry.getKey();
		Object value = entry.getValue();
		writeString(key);
		if (value instanceof BaseDataContainer) {
			intoChildBaseDataContainer((BaseDataContainer)value);
			iterator.setKey(key);
			return key;
		}
		writeValue(value);
		return null;
	}



	public void writeKeyValue(String key, Object value) throws IOException {
		
		writeKey(key);

	}







 
}
