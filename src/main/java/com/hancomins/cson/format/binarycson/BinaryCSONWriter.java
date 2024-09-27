package com.hancomins.cson.format.binarycson;

import com.hancomins.cson.CSONWriteException;
import com.hancomins.cson.ObjectType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;


@SuppressWarnings("UnusedReturnValue")
public class BinaryCSONWriter {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
	
	private final ArrayDeque<ObjectType> typeStack = new ArrayDeque<>();
	private final OutputStream outputStream;



	public BinaryCSONWriter() {
		outputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
		typeStack.addLast(ObjectType.None);
		try {
			outputStream.write(BinaryCSONDataType.PREFIX);
			outputStream.write(BinaryCSONDataType.VER_RAW);
		} catch (IOException ignored) {}
	}


	public BinaryCSONWriter(OutputStream outputStream) {
		this.outputStream = outputStream;
		typeStack.addLast(ObjectType.None);
		try {
			outputStream.write(BinaryCSONDataType.PREFIX);
			outputStream.write(BinaryCSONDataType.VER_RAW);
		} catch (IOException ignored) {}
	}


	
	
	private void writeString(byte[] buffer) throws IOException {
		// 총 16 개 버퍼 저장 가능. 1바이트 사용.
		if(buffer.length < 16) { 
			
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_STRING_SHORT | buffer.length);
			outputStream.write(typeAndLen);
			if(buffer.length == 0) {
				return;
			}
		}
		else if(buffer.length < 4095) {
			// 총 4094 개 버퍼 저장 가능. 2바이트 사용.
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_STRING_MIDDLE | ((buffer.length & 0x00000F00) >> 8));
			byte lenNext = (byte)(buffer.length & 0x000000FF);
			
			outputStream.write(typeAndLen);
			outputStream.write(lenNext);
			
			// 되돌리기
			//int lenFirst = ((int)(typeAndLen & 0x0F) << 8); 
			//int size = (lenFirst | (int)(lenNext & 0xFF));
		} else {
			// 총 1048574 개의 버퍼 저장 가능. 총 3바이트 사용.
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_STRING_LONG | ((buffer.length & 0x000F0000) >> 16));
			byte lenNextA = (byte)((buffer.length & 0x0000FF00) >> 8);
			byte lenNextB = (byte)(buffer.length & 0x000000FF);
			
			// 되돌리기 
			//int lenFirst = ((int)(typeAndLen & 0x0F) << 16); 
			//int size =  lenFirst | ((int)(lenNextA & 0xff) << 8) | (int)(lenNextB & 0xff);
			outputStream.write(typeAndLen);
			outputStream.write(lenNextA);
			outputStream.write(lenNextB);
		}
		outputStream.write(buffer, 0 ,buffer.length );
	}
	
	private void writeBuffer(byte[] buffer) throws IOException {
		// 총 16 개 버퍼 저장 가능. 1바이트 사용.
		if(buffer.length < 4095) {
			// 총 4094 개 버퍼 저장 가능. 2바이트 사용.
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_RAW_MIDDLE | ((buffer.length & 0x00000F00) >> 8));
			byte lenNext = (byte)(buffer.length & 0x000000FF);
			
			outputStream.write(typeAndLen);
			outputStream.write(lenNext);
			
			if(buffer.length == 0) {
				return;
			}
			
			// 되돌리기
			//int lenFirst = ((int)(typeAndLen & 0x0F) << 8); 
			//int size = (lenFirst | (int)(lenNext & 0xFF));
		} else if(buffer.length < 1048574) {
			// 총 1048574 개의 버퍼 저장 가능. 총 3바이트 사용.
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_RAW_LONG | ((buffer.length & 0x000F0000) >> 16));
			byte lenNextA = (byte)((buffer.length & 0x0000FF00) >> 8);
			byte lenNextB = (byte)(buffer.length & 0x000000FF);
			
			// 되돌리기 
			//int lenFirst = ((int)(typeAndLen & 0x0F) << 16); 
			//int size =  lenFirst | ((int)(lenNextA & 0xff) << 8) | (int)(lenNextB & 0xff);
			outputStream.write(typeAndLen);
			outputStream.write(lenNextA);
			outputStream.write(lenNextB);
		}
		else  {
				// 총 4G 저장 가능 총. 5바이트 사용.
				byte type = BinaryCSONDataType.TYPE_RAW_WILD;
				writeInt(buffer.length);				
				outputStream.write(type);
		}
		outputStream.write(buffer, 0 ,buffer.length );
	}
	
	
	
	private void writeFloat(float data) throws IOException {
		int floatValue = Float.floatToIntBits(data);
		writeInt(floatValue);
	}

	private void writeDouble(double data) throws IOException {
		long doubleValue = Double.doubleToLongBits(data);
		writeLong(doubleValue);
	}
			

	private void writeShort(short value) throws IOException {
		outputStream.write((byte) (value >> 8));
		outputStream.write((byte) (value));
	}

	private void writeInt(int value) throws IOException {
		outputStream.write((byte) (value >> 24));
		outputStream.write((byte) (value >> 16));
		outputStream.write((byte) (value >> 8));
		outputStream.write((byte) (value));
	}

	private void writeLong(long value) throws IOException {
		outputStream.write((byte) (value >> 56));
		outputStream.write((byte) (value >> 48));
		outputStream.write((byte) (value >> 40));
		outputStream.write((byte) (value >> 32));
		outputStream.write((byte) (value >> 24));
		outputStream.write((byte) (value >> 16));
		outputStream.write((byte) (value >> 8));
		outputStream.write((byte) (value));
		
	}
	

	@SuppressWarnings("unused")
	BinaryCSONWriter key(char key) throws IOException {
		if(typeStack.getLast() != ObjectType.Object) {
			throw new CSONWriteException();
		}
		typeStack.addLast(ObjectType.ObjectKey);
		writeString((key + "").getBytes(StandardCharsets.UTF_8));
		return this;
	 }
	
	 public BinaryCSONWriter key(String key) throws IOException {
		 if(typeStack.getLast() != ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 typeStack.addLast(ObjectType.ObjectKey);
		 writeString(key.getBytes(StandardCharsets.UTF_8));
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
     public BinaryCSONWriter nullValue() throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_NULL);
		 return this;
	 }

	public BinaryCSONWriter value(String value) throws IOException {
		 if(value== null) {
			 nullValue();
			 return this;
		 }
		 
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 writeString(value.getBytes(StandardCharsets.UTF_8));
		 return this;
	 }
	 
	 public BinaryCSONWriter value(byte[] value) throws IOException {
		 if(value== null) {
			 nullValue();
			 return this;
		 }
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 writeBuffer(value);
		 return this;
	 }

	public BinaryCSONWriter value(BigDecimal value) throws IOException {
        //noinspection DuplicatedCode
        if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		typeStack.removeLast();
		outputStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}

	public BinaryCSONWriter value(BigInteger value) throws IOException {
		//noinspection DuplicatedCode
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		typeStack.removeLast();
		outputStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}
	 
	 public BinaryCSONWriter value(byte value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_BYTE);
		 outputStream.write(value);
		 return this;
	 }

	public BinaryCSONWriter value(int value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_INT);
		 writeInt(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter value(long value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_LONG);
		 writeLong(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter value(short value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_SHORT);
		 writeShort(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter value(boolean value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_BOOLEAN);
		 outputStream.write(value ? 1 : 0);
		 return this;
	 }
	 
	 public BinaryCSONWriter value(char value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_CHAR);
		 writeShort((short)value);
		 return this;
	 }
	 
	 public BinaryCSONWriter value(float value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_FLOAT);
		 writeFloat(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter value(double value) throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 outputStream.write(BinaryCSONDataType.TYPE_DOUBLE);
		 writeDouble(value);
		 return this;
	 }


	 @SuppressWarnings("UnusedReturnValue")
     public BinaryCSONWriter addNull() throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_NULL);
		 return this;
	 }
	 
	 public BinaryCSONWriter add(String value) throws IOException {
		 if(value== null) {
			 addNull();
			 return this;
		 }
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 writeString(value.getBytes(StandardCharsets.UTF_8));
		 return this;
	 }
	 
	 public BinaryCSONWriter add(byte[] value) throws IOException {
		 if(value== null) {
			 addNull();
			 return this;
		 }
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 writeBuffer(value);
		 return this;
	 }

	@SuppressWarnings("unused")
	public BinaryCSONWriter add(BigDecimal value) throws IOException {
		//noinspection DuplicatedCode
		if(value== null) {
			addNull();
			return this;
		}
		if(typeStack.getLast() != ObjectType.Array) {
			throw new CSONWriteException();
		}
		outputStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}

	@SuppressWarnings("unused")
	public BinaryCSONWriter add(BigInteger value) throws IOException {
		//noinspection DuplicatedCode
		if(value== null) {
			addNull();
			return this;
		}
		if(typeStack.getLast() != ObjectType.Array) {
			throw new CSONWriteException();
		}
		outputStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}
	 
	 
	 public BinaryCSONWriter add(byte value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_BYTE);
		 outputStream.write(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter add(int value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_INT);
		 writeInt(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter add(long value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_LONG);
		 writeLong(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter add(short value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_SHORT);
		 writeShort(value);
		 return this;
	 }
	 
	 public BinaryCSONWriter add(boolean value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_BOOLEAN);
		 outputStream.write(value ? 1 : 0);
		 return this;
	 }
	 
	 @SuppressWarnings("unused")
	 public BinaryCSONWriter add(BinaryCSONWriter writer) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array && writer.typeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 byte[] buffer = writer.toByteArray(); 
		 int headerSize = 1/*prefix size*/ + BinaryCSONDataType.VER_RAW.length;
		 outputStream.write(buffer, headerSize, buffer.length - headerSize);
		 return this;
	 }
	 
	 public BinaryCSONWriter add(char value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_CHAR);
		 writeShort((short)value);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
	 public BinaryCSONWriter add(float value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_FLOAT);
		 writeFloat(value);
		 return this;
	 }

	@SuppressWarnings("UnusedReturnValue")
	public BinaryCSONWriter add(double value) throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_DOUBLE);
		 writeDouble(value);
		 return this;
	 }
	 
	 
	 
	 @SuppressWarnings("UnusedReturnValue")
     public BinaryCSONWriter openArray() throws IOException {
		 if(typeStack.getLast() != ObjectType.ObjectKey && typeStack.getLast() != ObjectType.Array && typeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 typeStack.addLast(ObjectType.Array);
		 outputStream.write(BinaryCSONDataType.TYPE_OPEN_ARRAY);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
     public BinaryCSONWriter closeArray() throws IOException {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 
		 typeStack.removeLast();
		 if(typeStack.getLast() == ObjectType.ObjectKey) {
			 typeStack.removeLast();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_CLOSE_ARRAY);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
     public BinaryCSONWriter openObject() throws IOException {
		 if(typeStack.getLast() == ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 typeStack.addLast(ObjectType.Object);
		 outputStream.write(BinaryCSONDataType.TYPE_OPEN_OBJECT);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
     public BinaryCSONWriter closeObject() throws IOException {
		 if(typeStack.getLast() != ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 if(typeStack.getLast() == ObjectType.ObjectKey) {
			 typeStack.removeLast();
		 }
		 outputStream.write(BinaryCSONDataType.TYPE_CLOSE_OBJECT);
		 return this;
	 }
	 
	 public byte[] toByteArray() {
		if(outputStream instanceof  ByteArrayOutputStream) {
			if (typeStack.getLast() != ObjectType.None) {
				throw new CSONWriteException();
			}
			return ((ByteArrayOutputStream)outputStream).toByteArray();
		}
		return null;
	 }
 
}
