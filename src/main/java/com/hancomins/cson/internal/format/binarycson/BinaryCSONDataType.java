package com.hancomins.cson.internal.format.binarycson;

import java.nio.ByteBuffer;

public class BinaryCSONDataType {

	protected static final byte PREFIX = 'C';

	public static final byte[] VER_RAW;
	protected static final short VER = 901;

	static {
        VER_RAW = ByteBuffer.wrap(new byte[2]).putShort(VER).array();
	}



	
	protected static final byte TYPE_OPEN_OBJECT = 0x01;
	protected static final byte TYPE_CLOSE_OBJECT = 0x02;
	protected static final byte TYPE_OPEN_ARRAY = 0x03;
	protected static final byte TYPE_CLOSE_ARRAY = 0x04;
	
	protected static final byte TYPE_BYTE = 0x05;
	protected static final byte TYPE_BOOLEAN = 0x06;
	protected static final byte TYPE_SHORT = 0x07;
	protected static final byte TYPE_CHAR = 0x08;
	protected static final byte TYPE_INT = 0x09;
	protected static final byte TYPE_FLOAT = 0x0A;
	protected static final byte TYPE_LONG = 0x0B;
	protected static final byte TYPE_DOUBLE = 0x0C;
	protected static final byte TYPE_BIGDECIMAL = 0x0D;
	protected static final byte TYPE_NULL = 0x0E;
	
	protected static final byte TYPE_STRING_SHORT = (byte)0xA0;
	protected static final byte TYPE_STRING_MIDDLE = (byte)0xB0;
	protected static final byte TYPE_STRING_LONG = (byte)0xC0;
	
	protected static final byte TYPE_RAW_MIDDLE = (byte)0xD0;
	protected static final byte TYPE_RAW_LONG = (byte)0xE0;
	protected static final byte TYPE_RAW_WILD = (byte)0xF0;
}
