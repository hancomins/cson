package com.clipsoft.cson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;


@SuppressWarnings("UnusedReturnValue")
class BinaryCSONWriter {
	
	private final static int DEFAULT_BUFFER_SIZE = 4096;
	
	
	private final ArrayDeque<ObjectType> mTypeStack = new ArrayDeque<>();
	private final ByteArrayOutputStream mBufferStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
	
	

	BinaryCSONWriter() {
		mTypeStack.addLast(ObjectType.None);
		try {
			mBufferStream.write(BinaryCSONDataType.PREFIX);
			mBufferStream.write(BinaryCSONDataType.VER_RAW);
		} catch (IOException ignored) {}
	}


	
	
	private void writeString(byte[] buffer) {
		// 총 16 개 버퍼 저장 가능. 1바이트 사용.
		if(buffer.length < 16) { 
			
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_STRING_SHORT | buffer.length);
			mBufferStream.write(typeAndLen);
			if(buffer.length == 0) {
				return;
			}
		}
		else if(buffer.length < 4095) {
			// 총 4094 개 버퍼 저장 가능. 2바이트 사용.
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_STRING_MIDDLE | ((buffer.length & 0x00000F00) >> 8));
			byte lenNext = (byte)(buffer.length & 0x000000FF);
			
			mBufferStream.write(typeAndLen);
			mBufferStream.write(lenNext);
			
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
			mBufferStream.write(typeAndLen);
			mBufferStream.write(lenNextA);
			mBufferStream.write(lenNextB);
		}
		mBufferStream.write(buffer, 0 ,buffer.length );
	}
	
	private void writeBuffer(byte[] buffer) {
		// 총 16 개 버퍼 저장 가능. 1바이트 사용.
		if(buffer.length < 4095) {
			// 총 4094 개 버퍼 저장 가능. 2바이트 사용.
			byte typeAndLen = (byte)(BinaryCSONDataType.TYPE_RAW_MIDDLE | ((buffer.length & 0x00000F00) >> 8));
			byte lenNext = (byte)(buffer.length & 0x000000FF);
			
			mBufferStream.write(typeAndLen);
			mBufferStream.write(lenNext);
			
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
			mBufferStream.write(typeAndLen);
			mBufferStream.write(lenNextA);
			mBufferStream.write(lenNextB);
		}
		else  {
				// 총 4G 저장 가능 총. 5바이트 사용.
				byte type = BinaryCSONDataType.TYPE_RAW_WILD;
				writeInt(buffer.length);				
				mBufferStream.write(type);
		}
		mBufferStream.write(buffer, 0 ,buffer.length );
	}
	
	
	
	private void writeFloat(float data) {
		int floatValue = Float.floatToIntBits(data);
		writeInt(floatValue);
	}

	private void writeDouble(double data) {
		long doubleValue = Double.doubleToLongBits(data);
		writeLong(doubleValue);
	}
			

	private void writeShort(short value) {
		mBufferStream.write((byte) (value >> 8));
		mBufferStream.write((byte) (value));
	}

	private void writeInt(int value) {
		mBufferStream.write((byte) (value >> 24));
		mBufferStream.write((byte) (value >> 16));
		mBufferStream.write((byte) (value >> 8));
		mBufferStream.write((byte) (value));
	}

	private void writeLong(long value) {
		mBufferStream.write((byte) (value >> 56));
		mBufferStream.write((byte) (value >> 48));
		mBufferStream.write((byte) (value >> 40));
		mBufferStream.write((byte) (value >> 32));
		mBufferStream.write((byte) (value >> 24));
		mBufferStream.write((byte) (value >> 16));
		mBufferStream.write((byte) (value >> 8));
		mBufferStream.write((byte) (value));
		
	}
	

	@SuppressWarnings("unused")
	BinaryCSONWriter key(char key) {
		if(mTypeStack.getLast() != ObjectType.Object) {
			throw new CSONWriteException();
		}
		mTypeStack.addLast(ObjectType.ObjectKey);
		writeString((key + "").getBytes(StandardCharsets.UTF_8));
		return this;
	 }
	
	 BinaryCSONWriter key(String key) {
		 if(mTypeStack.getLast() != ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.addLast(ObjectType.ObjectKey);
		 writeString(key.getBytes(StandardCharsets.UTF_8));
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter nullValue() {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_NULL);
		 return this;
	 }
	 
	 BinaryCSONWriter value(String value) {
		 if(value== null) {
			 nullValue();
			 return this;
		 }
		 
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 writeString(value.getBytes(StandardCharsets.UTF_8));
		 return this;
	 }
	 
	 BinaryCSONWriter value(byte[] value) {
		 if(value== null) {
			 nullValue();
			 return this;
		 }
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 writeBuffer(value);
		 return this;
	 }

	BinaryCSONWriter value(BigDecimal value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		mTypeStack.removeLast();
		mBufferStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}

	BinaryCSONWriter value(BigInteger value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		mTypeStack.removeLast();
		mBufferStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}
	 
	 BinaryCSONWriter value(byte value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_BYTE);
		 mBufferStream.write(value);
		 return this;
	 }
	 
	 BinaryCSONWriter value(int value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_INT);
		 writeInt(value);
		 return this;
	 }
	 
	 BinaryCSONWriter value(long value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_LONG);
		 writeLong(value);
		 return this;
	 }
	 
	 BinaryCSONWriter value(short value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_SHORT);
		 writeShort(value);
		 return this;
	 }
	 
	 BinaryCSONWriter value(boolean value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_BOOLEAN);
		 mBufferStream.write(value ? 1 : 0);
		 return this;
	 }
	 
	 BinaryCSONWriter value(char value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_CHAR);
		 writeShort((short)value);
		 return this;
	 }
	 
	 BinaryCSONWriter value(float value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_FLOAT);
		 writeFloat(value);
		 return this;
	 }
	 
	 BinaryCSONWriter value(double value) {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 mBufferStream.write(BinaryCSONDataType.TYPE_DOUBLE);
		 writeDouble(value);
		 return this;
	 }


	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter addNull() {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_NULL);
		 return this;
	 }
	 
	 BinaryCSONWriter add(String value) {
		 if(value== null) {
			 addNull();
			 return this;
		 }
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 writeString(value.getBytes(StandardCharsets.UTF_8));
		 return this;
	 }
	 
	 BinaryCSONWriter add(byte[] value) {
		 if(value== null) {
			 addNull();
			 return this;
		 }
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 writeBuffer(value);
		 return this;
	 }

	@SuppressWarnings("unused")
	BinaryCSONWriter add(BigDecimal value) {
		if(value== null) {
			addNull();
			return this;
		}
		if(mTypeStack.getLast() != ObjectType.Array) {
			throw new CSONWriteException();
		}
		mBufferStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}

	@SuppressWarnings("unused")
	BinaryCSONWriter add(BigInteger value) {
		if(value== null) {
			addNull();
			return this;
		}
		if(mTypeStack.getLast() != ObjectType.Array) {
			throw new CSONWriteException();
		}
		mBufferStream.write(BinaryCSONDataType.TYPE_BIGDECIMAL);
		writeString(value.toString().getBytes(StandardCharsets.UTF_8));
		return this;
	}
	 
	 
	 BinaryCSONWriter add(byte value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_BYTE);
		 mBufferStream.write(value);
		 return this;
	 }
	 
	 BinaryCSONWriter add(int value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_INT);
		 writeInt(value);
		 return this;
	 }
	 
	 BinaryCSONWriter add(long value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_LONG);
		 writeLong(value);
		 return this;
	 }
	 
	 BinaryCSONWriter add(short value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_SHORT);
		 writeShort(value);
		 return this;
	 }
	 
	 BinaryCSONWriter add(boolean value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_BOOLEAN);
		 mBufferStream.write(value ? 1 : 0);
		 return this;
	 }
	 
	 @SuppressWarnings("unused")
	 BinaryCSONWriter add(BinaryCSONWriter writer) {
		 if(mTypeStack.getLast() != ObjectType.Array && writer.mTypeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 byte[] buffer = writer.toByteArray(); 
		 int headerSize = 1/*prefix size*/ + BinaryCSONDataType.VER_RAW.length;
		 mBufferStream.write(buffer, headerSize, buffer.length - headerSize);
		 return this;
	 }
	 
	 BinaryCSONWriter add(char value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_CHAR);
		 writeShort((short)value);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter add(float value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_FLOAT);
		 writeFloat(value);
		 return this;
	 }

	@SuppressWarnings("UnusedReturnValue")
	BinaryCSONWriter add(double value) {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_DOUBLE);
		 writeDouble(value);
		 return this;
	 }
	 
	 
	 
	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter openArray() {
		 if(mTypeStack.getLast() != ObjectType.ObjectKey && mTypeStack.getLast() != ObjectType.Array && mTypeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.addLast(ObjectType.Array);
		 mBufferStream.write(BinaryCSONDataType.TYPE_OPEN_ARRAY);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter closeArray() {
		 if(mTypeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 
		 mTypeStack.removeLast();
		 if(mTypeStack.getLast() == ObjectType.ObjectKey) {
			 mTypeStack.removeLast();	 
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_CLOSE_ARRAY);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter openObject() {
		 if(mTypeStack.getLast() == ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.addLast(ObjectType.Object);
		 mBufferStream.write(BinaryCSONDataType.TYPE_OPEN_OBJECT);
		 return this;
	 }
	 
	 @SuppressWarnings("UnusedReturnValue")
	 BinaryCSONWriter closeObject() {
		 if(mTypeStack.getLast() != ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 mTypeStack.removeLast();
		 if(mTypeStack.getLast() == ObjectType.ObjectKey) {
			 mTypeStack.removeLast();	 
		 }
		 mBufferStream.write(BinaryCSONDataType.TYPE_CLOSE_OBJECT);
		 return this;
	 }
	 
	 byte[] toByteArray() {
		 if(mTypeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 return mBufferStream.toByteArray();
	 }
 
}
