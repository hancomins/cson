package com.hancomins.cson.format.cson;

import com.hancomins.cson.CSONException;
import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.format.*;
import com.hancomins.cson.util.ArrayStack;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("UnusedReturnValue")
public class BinaryCSONWriter extends WriterBorn {


	private static final int DEFAULT_BUFFER_SIZE = 4096;


	private List<CommentObject> currentCommentList = null;
	private final OutputStream outputStream;
	private final DataOutputStream dataOutputStream;
	private final ArrayStack<List<CommentObject>> commentStack = new ArrayStack<>();



	public BinaryCSONWriter() {
		super(false);
		this.outputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
		this.dataOutputStream = new DataOutputStream(outputStream);
	}


	public BinaryCSONWriter(OutputStream outputStream) {
		super(false);
		this.outputStream = new BufferedOutputStream(outputStream, DEFAULT_BUFFER_SIZE);
		this.dataOutputStream = new DataOutputStream(outputStream);
	}


	@Override
	protected void writeHeaderComment(String comment) {

        try {
			if(comment == null || comment.isEmpty()) {
				dataOutputStream.write(CSONFlag.COMMENT_ZERO);
			} else {
				dataOutputStream.writeByte(CSONFlag.HEADER_COMMENT);
				writeString(comment);
			}
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}

	@Override
	protected void writeFooterComment(String comment) {
		try {
			if(comment == null || comment.isEmpty()) {
				dataOutputStream.write(CSONFlag.COMMENT_ZERO);
			} else {
				dataOutputStream.writeByte(CSONFlag.FOOTER_COMMENT);
				writeString(comment);
			}
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}

	@Override
	protected void writePrefix() {
		try {
			dataOutputStream.writeInt(CSONFlag.CSON_HEADER);
			dataOutputStream.writeShort(CSONFlag.CSON_VERSION);
			dataOutputStream.writeShort(getOptionsFlag());
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}

	private short getOptionsFlag() {
		short optionsFlag = 0;
		if(!isSkipComments()) {
			optionsFlag |= CSONFlag.ENABLE_COMMENT;
		}
		return (byte) optionsFlag;
	}

	@Override
	protected void writeSuffix() {
		try {
			dataOutputStream.write(CSONFlag.CSON_FOOTER);
			dataOutputStream.flush();
			dataOutputStream.close();
		}catch (IOException e) {
			throw new CSONException(e);
		}
	}

	@Override
	protected void writeArrayPrefix(BaseDataContainer parents, DataIterator<?> iterator) {
		if(!isSkipComments()) {
			currentCommentList = new ArrayList<>();
			commentStack.push(currentCommentList);
		}
		writeSizeBuffer(iterator.size(), CSONFlag.ARRAY_LESS_THAN_16, CSONFlag.ARRAY_UINT8, CSONFlag.ARRAY_UINT16, CSONFlag.ARRAY_UINT32);
	}

	@Override
	protected void writeObjectPrefix(BaseDataContainer parents, DataIterator<Map.Entry<String, Object>> iterator) {
		if(!isSkipComments()) {
			currentCommentList = new ArrayList<>();
			commentStack.push(currentCommentList);
		}
		writeSizeBuffer(iterator.size(), CSONFlag.OBJECT_LESS_THAN_16, CSONFlag.OBJECT_UINT8, CSONFlag.OBJECT_UINT16, CSONFlag.OBJECT_UINT32);

	}


	private void writeSizeBuffer(int size,int twoBitFlag, int uint8Flag, int uint16Flag, int uint32Flag) {
		try {
			if (size < 16 && twoBitFlag > 0) {
				dataOutputStream.writeByte(twoBitFlag | size);
			} else if (size < 256) {
				dataOutputStream.writeByte(uint8Flag);
				dataOutputStream.writeByte(size);
			} else if (size < 65536) {
				dataOutputStream.writeByte(uint16Flag);
				dataOutputStream.writeShort(size);
			} else {
				dataOutputStream.writeByte(uint32Flag);
				dataOutputStream.writeInt(size);
			}
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}


	@Override
	protected void writeObjectSuffix(DataIterator<Map.Entry<String, Object>> iterator) {
		writeComments(true);
	}

	private void writeComments(boolean isKeyValueComment) {
		if(isSkipComments()) {
			return;
		}
		try {
			currentCommentList = commentStack.poll();
			if (currentCommentList != null && !currentCommentList.isEmpty()) {
				writeSizeBuffer(currentCommentList.size(), -1, CSONFlag.COMMENT_UINT8, CSONFlag.COMMENT_UINT16, CSONFlag.COMMENT_UINT32);
				for (CommentObject<?> commentObject : currentCommentList) {
					writeComment(commentObject, isKeyValueComment);
				}
			} else {
				dataOutputStream.write(CSONFlag.COMMENT_ZERO);
			}
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}




	private void writeComment(CommentObject<?> commentObject, boolean isKeyValueComment) throws IOException {
		if(isKeyValueComment) {
			String index = (String)commentObject.getIndex();
			writeString(index);
		} else {
			Integer index = (Integer)commentObject.getIndex();
			writeValue(index);
		}


		String beforeKey = commentObject.getComment(CommentPosition.BEFORE_KEY);
		String afterKey = commentObject.getComment(CommentPosition.AFTER_KEY);
		String beforeValue = commentObject.getComment(CommentPosition.BEFORE_VALUE);
		String afterValue = commentObject.getComment(CommentPosition.AFTER_VALUE);
		byte flag = makeCommentFlag(beforeKey, afterKey, beforeValue, afterValue);
		dataOutputStream.writeByte(flag);
		if(beforeKey != null && !beforeKey.isEmpty()) {
			writeString(beforeKey);
		}
		if(afterKey != null && !afterKey.isEmpty()) {
			writeString(afterKey);
		}
		if(beforeValue != null && !beforeValue.isEmpty()) {
			writeString(beforeValue);
		}
		if(afterValue != null && !afterValue.isEmpty()) {
			writeString(afterValue);
		}
	}

	private byte makeCommentFlag(String beforeKey, String afterKey, String beforeValue, String afterValue) {
		byte flag = 0;
		if(beforeKey != null && !beforeKey.isEmpty()) {
			flag |= CSONFlag.COMMENT_TYPE_BEFORE_KEY;
		}
		if(afterKey != null && !afterKey.isEmpty()) {
			flag |= CSONFlag.COMMENT_TYPE_AFTER_KEY;
		}
		if(beforeValue != null && !beforeValue.isEmpty()) {
			flag |= CSONFlag.COMMENT_TYPE_BEFORE_VALUE;
		}
		if(afterValue != null && !afterValue.isEmpty()) {
			flag |= CSONFlag.COMMENT_TYPE_AFTER_VALUE;
		}
		return flag;
	}







	@Override
	protected void writeArraySuffix(DataIterator<Object> iterator) {
		writeComments(false);
	}

	@Override
	protected void writeKey(String key) {
		if(!isSkipComments()) {
			@SuppressWarnings("unchecked") CommentObject<String> commentObject = (CommentObject<String>)getCurrentCommentObject();
			if(commentObject != null) {
				currentCommentList.add(commentObject);
			}
		}
		writeString(key);
	}

	@Override
	protected void writeObjectValue(Object value) {
		writeValue(value);
	}

	@Override
	protected void writeArrayValue(Object value) {
		if(!isSkipComments()) {
			@SuppressWarnings("unchecked") CommentObject<Integer> commentObject = (CommentObject<Integer>)getCurrentCommentObject();
			if(commentObject != null) {
				currentCommentList.add(commentObject);
			}
		}
		writeValue(value);
	}

	private void writeStringNullToEmpty(String value) {
		if(value == null) {
			writeString("");
		} else {
			writeString(value);
		}
	}


	private void writeString(String value)  {
		try {
			byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
			int length = bytes.length;
			if (length < 16) {
				int flag = CSONFlag.STRING_LESS_THAN_16 | length;
				dataOutputStream.writeByte(flag);
			} else if (length < 256) {
				dataOutputStream.writeByte(CSONFlag.STRING_UINT8);
				dataOutputStream.writeByte(length);
			} else if (length < 65536) {
				dataOutputStream.writeByte(CSONFlag.STRING_UINT16);
				dataOutputStream.writeShort(length);
			} else {
				dataOutputStream.writeByte(CSONFlag.STRING_UINT32);
				dataOutputStream.writeInt(length);
			}
			dataOutputStream.write(bytes);
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}



	private void writeValue(Object value)  {
		try {
			if (value instanceof String) {
				writeString((String) value);
			} else if (value instanceof Boolean) {
				if (value == Boolean.TRUE) {
					dataOutputStream.writeByte(CSONFlag.TRUE);
				} else {
					dataOutputStream.writeByte(CSONFlag.FALSE);
				}
			} else if (value instanceof Integer) {
				dataOutputStream.writeByte(CSONFlag.INT32);
				dataOutputStream.writeInt((Integer) value);
			} else if (value instanceof Long) {
				dataOutputStream.writeByte(CSONFlag.INT64);
				dataOutputStream.writeLong((Long) value);
			} else if (value instanceof Float) {
				dataOutputStream.writeByte(CSONFlag.DEC32);
				dataOutputStream.writeFloat((Float) value);
			} else if (value instanceof Double) {
				dataOutputStream.writeByte(CSONFlag.DEC64);
				dataOutputStream.writeDouble((Double) value);
			} else if (value instanceof Short) {
				dataOutputStream.writeByte(CSONFlag.INT16);
				dataOutputStream.writeShort((Short) value);
			} else if (value instanceof Byte) {
				dataOutputStream.writeByte(CSONFlag.INT8);
				dataOutputStream.writeByte((Byte) value);
			} else if (value instanceof Character) {
				dataOutputStream.writeByte(CSONFlag.INT_CHAR);
				dataOutputStream.writeChar((Character) value);
			} else if (value instanceof BigInteger) {
				dataOutputStream.writeByte(CSONFlag.BIG_INT);
				BigInteger bigInteger = (BigInteger) value;
				String integerString = bigInteger.toString();
				writeString(integerString);
			} else if (value instanceof BigDecimal) {
				dataOutputStream.writeByte(CSONFlag.BIG_DEC);
				BigDecimal bigDecimal = (BigDecimal) value;
				String decimalString = bigDecimal.toString();
				writeString(decimalString);
			} else if (value instanceof Number) {
				dataOutputStream.writeByte(CSONFlag.BIG_DEC);
				BigDecimal bigDecimal = new BigDecimal(value.toString());
				String decimalString = bigDecimal.toString();
				writeString(decimalString);
			} else if (value instanceof byte[]) {
				writeBuffer((byte[]) value);
			} else {
				dataOutputStream.write(CSONFlag.NULL);
			}
		} catch (IOException e) {
			throw new CSONException(e);
		}
	}


	private void writeBuffer(byte[] buffer) throws IOException {
		int length = buffer.length;
		if(length < 256) {
			dataOutputStream.writeByte(CSONFlag.BYTE_BUFFER_UINT8);
			dataOutputStream.writeByte(length);
		} else if(length < 65536) {
			dataOutputStream.writeByte(CSONFlag.BYTE_BUFFER_UINT16);
			dataOutputStream.writeShort(length);
		} else {
			dataOutputStream.writeByte(CSONFlag.BYTE_BUFFER_UINT32);
			dataOutputStream.writeInt(length);
		}
		dataOutputStream.write(buffer);

	}
}
