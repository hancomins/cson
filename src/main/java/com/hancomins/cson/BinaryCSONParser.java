package com.hancomins.cson;

import java.io.InputStream;
import java.nio.ByteBuffer;

class BinaryCSONParser {
	
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

	public static CSONElement parse(InputStream inputStream) {
		BinaryCSONParseIterator binaryCsonParseIterator = new BinaryCSONParseIterator();
		BinaryCSONBufferReader.parse(inputStream, binaryCsonParseIterator);
		return binaryCsonParseIterator.release();
	}



}