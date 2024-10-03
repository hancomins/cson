package com.hancomins.cson.format.binarycson;

import com.hancomins.cson.CSONElement;
import com.hancomins.cson.format.ArrayDataContainerFactory;
import com.hancomins.cson.format.BaseDataContainer;
import com.hancomins.cson.format.KeyValueDataContainerFactory;
import com.sun.org.apache.xml.internal.security.signature.ObjectContainer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BinaryCSONParser {
	
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

	public BaseDataContainer parse(InputStream inputStream, KeyValueDataContainerFactory keyValueDataContainerFactory, ArrayDataContainerFactory arrayDataContainerFactory) throws IOException {
		this.arrayDataContainerFactory = arrayDataContainerFactory;
		this.keyValueDataContainerFactory = keyValueDataContainerFactory;
		int v = -1;
		while((v = inputStream.read()) != -1) {
			byte flag = (byte)v;
			byte type = (byte)(flag & 0xF0);

		}



	}







}
