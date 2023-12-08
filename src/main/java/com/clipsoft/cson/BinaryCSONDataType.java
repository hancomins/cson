package com.clipsoft.cson;

import java.nio.ByteBuffer;

class BinaryCSONDataType {

	protected final static byte PREFIX = 'C';

	public final static byte[] VER_RAW;
	protected final static short VER = 901;

	static {
        VER_RAW = ByteBuffer.wrap(new byte[2]).putShort(VER).array();
	}



	
	protected final static byte TYPE_OPEN_OBJECT = 0x01;
	protected final static byte TYPE_CLOSE_OBJECT = 0x02;
	protected final static byte TYPE_OPEN_ARRAY = 0x03;
	protected final static byte TYPE_CLOSE_ARRAY = 0x04;
	
	protected final static byte TYPE_BYTE = 0x05;
	protected final static byte TYPE_BOOLEAN = 0x06;
	protected final static byte TYPE_SHORT = 0x07;
	protected final static byte TYPE_CHAR = 0x08;
	protected final static byte TYPE_INT = 0x09;
	protected final static byte TYPE_FLOAT = 0x0A;
	protected final static byte TYPE_LONG = 0x0B;
	protected final static byte TYPE_DOUBLE = 0x0C;
	protected final static byte TYPE_BIGDECIMAL = 0x0D;
	protected final static byte TYPE_NULL = 0x0E;
	
	protected final static byte TYPE_STRING_SHORT = (byte)0xA0;
	protected final static byte TYPE_STRING_MIDDLE = (byte)0xB0;
	protected final static byte TYPE_STRING_LONG = (byte)0xC0;
	
	protected final static byte TYPE_RAW_MIDDLE = (byte)0xD0;
	protected final static byte TYPE_RAW_LONG = (byte)0xE0;
	protected final static byte TYPE_RAW_WILD = (byte)0xF0;
}
