package com.hancomins.cson.format.json;

import com.hancomins.cson.*;
import com.hancomins.cson.format.FormatWriter;
import com.hancomins.cson.options.JsonWritingOptions;
import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.EscapeUtil;
import com.hancomins.cson.util.NullValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


@SuppressWarnings("UnusedReturnValue")
public class JSONWriter implements FormatWriter {


	private static final int DEFAULT_BUFFER_SIZE = 512;

	private final JsonWritingOptions JsonWritingOptions;

	private static final int COMMENT_BEFORE_KEY = 1;

	private static final int COMMENT_SLASH_STAR = 4;


	private static final int COMMENT_BEFORE_ARRAY_VALUE = 5;
	private static final int COMMENT_COMMA_AND_SLASH_STAR = 6;
	private static final int COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE = 7;
	private static final int COMMENT_SIMPLE = 8;

	private final boolean isComment;
	private boolean isAllowLineBreak;

	private boolean isPretty = false;
	private boolean isUnprettyArray = false;
	private String depthSpace = "  ";

	private boolean hasValue = false;
	private int lastCharacterBufferLength = 0;

	private String keyQuote = "\"";
	private char keyQuoteChar = '"';
	private String valueQuote = "\"";
	private char valueQuoteChar = '"';
	private boolean isSpacialValueQuote = false;
	private boolean isSpacialKeyQuote = false;



	//private final ArrayDeque<CommentObject> keyValueCommentObjects = new ArrayDeque<>();
	private ArrayDeque<CommentObject> currentKeyValueCommentObjects;

	private final ArrayDeque<ObjectType> typeStack_ = new ArrayDeque<>();
	private final CharacterBuffer stringBuilder = new CharacterBuffer(DEFAULT_BUFFER_SIZE);



	private ObjectType removeStack() {
		return typeStack_.removeLast();
	}

	private void changeStack(ObjectType type) {
		typeStack_.removeLast();
		typeStack_.addLast(type);
	}

	private void pushStack(ObjectType type) {
		typeStack_.addLast(type);
	}

	private void writeDepthTab(CharacterBuffer stringBuilder) {
		if(!isPretty) return;
		for (int i = 0, n = typeStack_.size(); i < n; i++) {
			stringBuilder.append(depthSpace);
		}
	}


	boolean isComment() {
		return isComment;
	}


	private String getAfterComment() {
		if(!isComment) return null;
		if(currentKeyValueCommentObjects != null &&  !currentKeyValueCommentObjects.isEmpty()) {
			CommentObject commentObject = currentKeyValueCommentObjects.removeFirst();
			String afterComment = commentObject.getTrailingComment();
			if (afterComment == null) {
				return null;
			}
			return commentObject.getTrailingComment();
		}
		return null;
	}

	private void writeAfterComment(int type) {
		String afterComment = getAfterComment();
		writeComment(afterComment, type);
	}



	private void writeBeforeComment(int type) {
		if(!isComment) return;
		if(currentKeyValueCommentObjects != null && !currentKeyValueCommentObjects.isEmpty()) {
			CommentObject commentObject = currentKeyValueCommentObjects.getFirst();
			String beforeComment  = commentObject.getLeadingComment();
			if(beforeComment == null) {
				return;
			}
			writeComment(beforeComment, type);
			//commentObject.setHeadComment(null);
		}
	}



	public JSONWriter(JsonWritingOptions JsonWritingOptions) {
		this.JsonWritingOptions = JsonWritingOptions;
		if(JsonWritingOptions.isPretty()) {
			isPretty = true;
		}
		if(JsonWritingOptions.isUnprettyArray()) {
			isUnprettyArray = true;
		}
		if(JsonWritingOptions.getDepthString() != null) {
			depthSpace = JsonWritingOptions.getDepthString();
		}


		keyQuote = JsonWritingOptions.getKeyQuote();
		valueQuote = JsonWritingOptions.getValueQuote();
		if(!JsonWritingOptions.isAllowUnquoted() && keyQuote.isEmpty()) {
			keyQuote = "\"";
		}
		if(!JsonWritingOptions.isAllowSingleQuotes()) {
			if(keyQuote.equals("'")) {
				keyQuote = JsonWritingOptions.isAllowUnquoted() ? "" : "\"";

			}
			if(valueQuote.equals("'")) {
				valueQuote = "\"";
			}
		}
		if(!"\"".equals(keyQuote)) {
			isSpacialKeyQuote = true;
		}
		if(!"\"".equals(valueQuote)) {
			isSpacialValueQuote = true;
		}
		keyQuoteChar = keyQuote.isEmpty() ? 0 : keyQuote.charAt(0);
		valueQuoteChar = valueQuote.charAt(0);
		isAllowLineBreak = JsonWritingOptions.isAllowLineBreak();

		isComment =  !JsonWritingOptions.isSkipComments();
	}


	private void writeComment(String comment, int commentType) {

		if(comment != null && isComment) {
			String[] commentLines = comment.split("\n");
			boolean isSlashStar = !isPretty || commentType == COMMENT_SLASH_STAR || commentType == COMMENT_COMMA_AND_SLASH_STAR || commentType == COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE;
			boolean isMultiLine = commentLines.length > 1;
			for (int i = 0, n = commentLines.length; i < n; i++) {
				String commentLine = commentLines[i];
				if(commentLine.isEmpty()) {
					stringBuilder.append("\n");
					continue;
				}
				if(isSlashStar) {
					if(i == 0) {
						if(commentType == COMMENT_COMMA_AND_SLASH_STAR) {
							stringBuilder.append(",");
						}
						else if(commentType == COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE) {
							stringBuilder.append("\n");
							writeDepthTab(stringBuilder);
						}
						if(isMultiLine) {
							stringBuilder.append("\n");
							writeDepthTab(stringBuilder);
						}
						stringBuilder.append("/*");
					} else {
						stringBuilder.append("\n");
					}

					if(i != 0) {
						//writeDepthTab(stringBuilder);
					}

					stringBuilder.append(commentLine);



				}
				else if(commentType == COMMENT_BEFORE_ARRAY_VALUE) {
					stringBuilder.append("//");
					stringBuilder.append(commentLine);
					stringBuilder.append("\n");
					writeDepthTab(stringBuilder);
				} else if(commentType == COMMENT_SIMPLE) {
					stringBuilder.append("\n");
					writeDepthTab(stringBuilder);
					stringBuilder.append("//");
					stringBuilder.append(commentLine);

				}
				else if(commentType == COMMENT_BEFORE_KEY) {
					writeDepthTab(stringBuilder);
					stringBuilder.append("//");
					stringBuilder.append(commentLine);
					stringBuilder.append("\n");
				}
			}

			if(isSlashStar) {
				stringBuilder.append("*/");
				if(isMultiLine) {
					stringBuilder.append("\n");
					writeDepthTab(stringBuilder);
				}
			}

		}
	}


	void nextCommentObject(CommentObject commentObject) {
		if(!isComment) return;
		if(this.currentKeyValueCommentObjects == null) {
			this.currentKeyValueCommentObjects = new ArrayDeque<>();
		}
		if(commentObject == null) {
			this.currentKeyValueCommentObjects.addLast(new CommentObject());
			return;
		}



		this.currentKeyValueCommentObjects.addLast(commentObject.copy());
	}


	protected void writeComment(String comment, @SuppressWarnings("SameParameterValue") boolean alwaysSlash, String prefix, String suffix) {
		if(!isComment || comment == null) return;
		if(!isPretty || alwaysSlash || comment.contains("\n")) {
			stringBuilder.append(" /* " ).append(comment).append(" */ ");
		} else {
			this.stringBuilder.append(prefix).append("//").append(comment).append(suffix);
		}
	}






	private void writeString(String quoteArg, String str) {
		String quote = quoteArg;

		if(isAllowLineBreak && str.contains("\\\n")) {
			quote = "\"";
		}
		char quoteChar = quote.isEmpty() ? '\0'  : quote.charAt(0);
		str = EscapeUtil.escapeJSONString(str, isAllowLineBreak, quoteChar);
		stringBuilder.append(quote);
		stringBuilder.append(str);
		stringBuilder.append(quote);
	}

	public JSONWriter key(String key) {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.OpenObject) {
			stringBuilder.append(',');
			/*if(commentStringAfterObjectValue != null) {
				writeComment(commentStringAfterObjectValue, COMMENT_SLASH_START);
				commentStringAfterObjectValue = null;
			}*/
		}
		else {
			changeStack(ObjectType.Object);
		}
		pushStack(ObjectType.ObjectKey);
		if(isPretty) {
			stringBuilder.append("\n");
		}
		writeBeforeComment(COMMENT_BEFORE_KEY);
		writeDepthTab(stringBuilder);
		writeString(keyQuote, key);
		writeAfterComment(COMMENT_SLASH_STAR);
		stringBuilder.append(":");
		return this;
	}

	/*public JSONWriter key(char key) {
		writeBeforeComment();
		key(String.valueOf(key));
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}*/

	public JSONWriter nullValue() {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		writeBeforeComment(COMMENT_SLASH_STAR);
		stringBuilder.append("null");
		writeAfterComment(COMMENT_SLASH_STAR);;
		removeStack();
		return this;
	}


	public JSONWriter value(String value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		writeString(valueQuote, value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(byte[] value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		stringBuilder.append("\"base64,");
		stringBuilder.append(Base64.getEncoder().encodeToString(value));
		stringBuilder.append('"');
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	/*public JSONWriter value(CSONElement value) {
		writeBeforeComment(COMMENT_SLASH_STAR);
		String afterValueComment = getAfterComment();
		value.write(this, false);
		writeComment(afterValueComment, COMMENT_SLASH_STAR);
		return this;
	}*/

	public JSONWriter value(java.lang.Object value) {
		if(value== null) {
			nullValue();
			return this;
		}

		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		if(value instanceof CharSequence || value instanceof Character) {
			writeString(valueQuote, value.toString());
		} else if(value instanceof Number) {
			stringBuilder.append(value.toString());
		} else if(value instanceof Boolean) {
			stringBuilder.append(value.toString());
		} else if(value instanceof byte[]) {
			stringBuilder.append("\"base64,");
			stringBuilder.append(Base64.getEncoder().encodeToString((byte[])value));
			stringBuilder.append('"');
		} else  {
			stringBuilder.append(valueQuote);
			stringBuilder.append(String.valueOf(value));
			stringBuilder.append(valueQuote);
		}
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(byte value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(int value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(long value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(short value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(boolean value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		hasValue = true;
		stringBuilder.append(value ? "true" : "false");
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter value(char value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		String quote =  valueQuote;
		hasValue = true;
		stringBuilder.append(quote);
		stringBuilder.append(value);
		stringBuilder.append(quote);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}



	public JSONWriter value(float value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		hasValue = true;
		writeFloat(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	private void writeFloat(float value) {
		hasValue = true;
		if(JsonWritingOptions.isAllowInfinity() && Float.isInfinite(value)) {
			if(value > 0) {
				stringBuilder.append("Infinity");
			} else {
				stringBuilder.append("-Infinity");
			}
		} else if(Float.isNaN(value)) {
			if(JsonWritingOptions.isAllowNaN()) {
				stringBuilder.append("NaN");
			} else {
				stringBuilder.append(valueQuote).append("NaN").append(valueQuote);
			}
		} else {
			stringBuilder.append(value);
		}
	}

	public JSONWriter value(double value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_STAR);
		hasValue = true;
		writeDouble(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	private void writeDouble(double value) {
		hasValue = true;
		if(JsonWritingOptions.isAllowInfinity() && Double.isInfinite(value)) {
			if(value > 0) {
				stringBuilder.append("Infinity");
			} else {
				stringBuilder.append("-Infinity");
			}
		} else if(Double.isNaN(value)) {
			if(JsonWritingOptions.isAllowNaN()) {
				stringBuilder.append("NaN");
			} else {
				stringBuilder.append(valueQuote).append("NaN").append(valueQuote);
			}
		} else {
			stringBuilder.append(value);
		}
	}

	private void checkAndAppendInArray() {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.OpenArray) {

			stringBuilder.append(',');
		} else if(type == ObjectType.OpenArray) {
			changeStack(ObjectType.Array);
		}
		else if(type != ObjectType.Array) {
			throw new CSONWriteException();
		}


		if(isPretty && !isUnprettyArray) {
			//stringBuilder.append('\n');
			writeBeforeComment(COMMENT_SIMPLE);
			stringBuilder.append('\n');
			writeDepthTab(stringBuilder);
		} else {
			writeBeforeComment(COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE);
		}
	}

	///
	public JSONWriter addNull() {
		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append("null");
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(String value) {
		if(value== null) {
			addNull();
			return this;
		}
		checkAndAppendInArray();
		hasValue = true;
		writeString(valueQuote, value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}


	public JSONWriter add(byte[] value) {
		if(value== null) {
			addNull();
			return this;
		}

		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append("\"base64,");
		stringBuilder.append(Base64.getEncoder().encodeToString(value));
		stringBuilder.append('"');
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter add(BigDecimal value) {
		if(value== null) {
			addNull();
			return this;
		}

		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter add(BigInteger value) {
		if(value== null) {
			addNull();
			return this;
		}
		hasValue = true;
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	/*
	int commentType = COMMENT_COMMA_AND_SLASH_STAR;
	boolean isEmptyObject = stringBuilder.charAt(stringBuilder.length() - 1) == '{';
		if(isPretty && !isEmptyObject) {
		stringBuilder.append('\n');
		writeDepthTab(stringBuilder);
	} else {
		stringBuilder.append("");
	}
		if(isEmptyObject) {
		commentType = COMMENT_SLASH_STAR;
	}
	writeBeforeComment(commentType);*/


	/*public JSONWriter add(CSONElement value) {
		checkAndAppendInArray();
		hasValue = true;
		String afterComment = getAfterComment();
		value.write(this, false);
		if(afterComment != null) {
			writeComment(afterComment, COMMENT_SLASH_STAR);
		}
		//writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}*/

	public JSONWriter addCSONElement() {


		checkAndAppendInArray();



		/*String afterComment = getAfterComment();
		if(afterComment != null) {
			writeComment(afterComment, COMMENT_SLASH_STAR);
		}*/


		//writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}


	public JSONWriter add(byte value) {
		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter add(int value) {
		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(long value) {

		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(short value) {

		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(boolean value) {

		checkAndAppendInArray();
		hasValue = true;
		stringBuilder.append(value ? "true" : "false");
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}



	public JSONWriter add(char value) {
		checkAndAppendInArray();
		String quote =   valueQuote;
		stringBuilder.append(quote);
		stringBuilder.append(value);
		stringBuilder.append(quote);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(float value) {
		checkAndAppendInArray();
		writeFloat(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(double value) {
		checkAndAppendInArray();
		writeDouble(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	@SuppressWarnings("StatementWithEmptyBody")
	public JSONWriter openArray() {
		if(!typeStack_.isEmpty()) {
			ObjectType type = typeStack_.getLast();
			if (type == ObjectType.OpenArray) {
				changeStack(ObjectType.Array);
				if(isPretty && !isUnprettyArray) {
					stringBuilder.append('\n');
					writeDepthTab(stringBuilder);
				}
			} else if (type == ObjectType.Array) {

			} else if (type != ObjectType.ObjectKey && type != ObjectType.None) {
				throw new CSONWriteException();
			} else {
				writeBeforeComment(COMMENT_SLASH_STAR);
			}
		}
		pushStack(ObjectType.OpenArray);
		stringBuilder.append('[');
		return this;
	}

	public JSONWriter closeArray() {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.Array && type != ObjectType.OpenArray) {
			throw new CSONWriteException();
		}
		removeStack();
		int commentType = COMMENT_COMMA_AND_SLASH_STAR;
		boolean isEmptyArray = stringBuilder.charAt(stringBuilder.length() - 1) == '[';
		if(isPretty && !isUnprettyArray && !isEmptyArray) {
			stringBuilder.append('\n');
			writeDepthTab(stringBuilder);
		} else if(isEmptyArray){
			commentType = COMMENT_SLASH_STAR;
		}

		//writeBeforeComment(commentType);

		if(typeStack_.isEmpty()) {
			stringBuilder.append(']');
			writeBeforeComment(COMMENT_BEFORE_KEY);
			return this;
		}

		if(typeStack_.getLast() == ObjectType.ObjectKey) {
			removeStack();
		}
		stringBuilder.append(']');
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	@SuppressWarnings("StatementWithEmptyBody")
	public JSONWriter openObject() {
		ObjectType type = typeStack_.isEmpty() ? null : typeStack_.getLast();
		//int commentType = COMMENT_BEFORE_ARRAY_VALUE;
		if(type == ObjectType.Object) {
			throw new CSONWriteException();
		} else if(type == ObjectType.Array) {
			//stringBuilder.append(',');
			//if(isPretty) {
			//	writeDepthTab(stringBuilder);
			//}
		} else if(type == ObjectType.OpenArray) {
			if(isPretty) {
				stringBuilder.append('\n');
				writeDepthTab(stringBuilder);
			}
			changeStack(ObjectType.Array);
		} else if(type == ObjectType.ObjectKey) {
			//commentType = COMMENT_SLASH_STAR;
		} else if(type == ObjectType.OpenObject) {
			//commentType = COMMENT_BEFORE_KEY;
		}
		//writeBeforeAndRemoveComment(commentType);
		stringBuilder.append('{');
		pushStack(ObjectType.OpenObject);
		lastCharacterBufferLength = stringBuilder.length();
		return this;
	}

	public boolean isEmpty() {
		return typeStack_.isEmpty();
	}

	public JSONWriter closeObject(boolean inArray) {
		if(typeStack_.getLast() != ObjectType.Object && typeStack_.getLast() != ObjectType.OpenObject) {
			throw new CSONWriteException();
		}
		removeStack();


		if(isPretty) {
			if(stringBuilder.length() != lastCharacterBufferLength && (!inArray || !isUnprettyArray)) {
				stringBuilder.append('\n');
				writeDepthTab(stringBuilder);
			}
		}

		stringBuilder.append('}');

		if(!typeStack_.isEmpty() && typeStack_.getLast() == ObjectType.ObjectKey) {
			removeStack();
		}
		if(typeStack_.isEmpty()) {
			writeAfterComment(COMMENT_BEFORE_KEY);
		} else {
			writeAfterComment(COMMENT_SLASH_STAR);
		}
		return this;
	}

	@Override
	public String toString() {

		return stringBuilder.toString();
	}

	private static class Count {
		int count = 0;

		int getAndIncrement() {
			return count++;
		}

	}


	public static void writeJSONElement(CSONElement root,JSONWriter writer) {
		//JSONWriter writer  = new JSONWriter((JsonWritingOptions) stringFormatOption);

		boolean allowComment = writer.isComment();

		CSONElement currentElement = root;
		ArrayDeque<Iterator<?>> iteratorStack = new ArrayDeque<>();
		ArrayDeque<CSONElement> elementStack = new ArrayDeque<>();
		ArrayDeque<Count> iteratorCountStack = new ArrayDeque<>();
		ArrayDeque<ArrayDeque<CommentObject>> keyValueCommentObjectStack = new ArrayDeque<>();

		Iterator<?> currentIter = null;
		Count iteratorCount = new Count();

		int step = 0;

		boolean lastClosed = false;
		boolean skipClose = false;
		boolean closeRoot = false;



		writer.writeComment(currentElement.getHeadComment(), false, "", "\n");
		if(writer.isComment) {
			writer.currentKeyValueCommentObjects = new ArrayDeque<>();
		}


		do {

			if (currentElement instanceof CSONObject) {
				Iterator<Map.Entry<String, java.lang.Object>> iter;
				Map<String, KeyValueCommentObject> keyValueCommentMap;
				CSONObject currentObject = (CSONObject) currentElement;
				keyValueCommentMap = currentObject.getKeyValueCommentMap();
				if(!lastClosed) {
					iter = currentObject.entrySet().iterator();
					currentIter = iter;
					writer.openObject();
				} else {
					iter = (Iterator<Map.Entry<String, java.lang.Object>>)currentIter;
					lastClosed = false;
				}
				boolean isComment = allowComment && keyValueCommentMap != null;
				while (iter.hasNext()) {
					Map.Entry<String, java.lang.Object> entry = iter.next();
					String key = entry.getKey();
					java.lang.Object value = entry.getValue();

					KeyValueCommentObject keyValueCommentObject = isComment ? keyValueCommentMap.get(key) : null;
					if(allowComment) {
						writer.nextCommentObject(keyValueCommentObject == null ? null : keyValueCommentObject.getKeyCommentObject());
						writer.nextCommentObject(keyValueCommentObject == null ? null : keyValueCommentObject.getValueCommentObject());
					}
					if (value == null || value instanceof NullValue) writer.key(key).nullValue();
					else if (value instanceof CSONElement) {
						iteratorStack.add(iter);
						elementStack.add(currentElement);
						writer.key(key);

						if(allowComment) {

							/*String beforeComment = null;
							if(writer.currentKeyValueCommentObjects != null) {
								CommentObject commentObject = writer.currentKeyValueCommentObjects.peekLast();
								if(commentObject != null) {
									beforeComment = commentObject.getLeadingComment();
								}
							}*/
							//String beforeComment = writer.currentKeyValueCommentObjects == null ? null : writer.currentKeyValueCommentObjects.getLast().get
							/*if(writer.currentKeyValueCommentObjects == null) {
								writer.currentKeyValueCommentObjects = new ArrayDeque<>();
							}*/
							keyValueCommentObjectStack.add(writer.currentKeyValueCommentObjects);
							writer.currentKeyValueCommentObjects = new ArrayDeque<>();
							/*if(beforeComment != null) {
								writer.currentKeyValueCommentObjects.addLast(new CommentObject(beforeComment, null));
							}*/


						}

						if(isComment &&
								keyValueCommentObject != null &&
								!keyValueCommentObject.isNullOrEmptyValueCommentObject()) {
							String beforeComment = keyValueCommentObject.getValueCommentObject().getLeadingComment();
							if(beforeComment != null && !beforeComment.isEmpty()) {
								writer.writeComment(beforeComment, COMMENT_SLASH_STAR);
							}
						}
						iteratorCountStack.add(iteratorCount);
						iteratorCount = new Count();
						currentElement = (CSONElement) value;
						skipClose = true;
						if(isComment) {
							writer.currentKeyValueCommentObjects.clear();
						}


						break;
					} else if (value instanceof Byte) writer.key(key).value((byte) value);
					else if (value instanceof Boolean) writer.key(key).value((boolean) value);
					else if (value instanceof Short) writer.key(key).value((short) value);
					else if (value instanceof Character) writer.key(key).value((char) value);
					else if (value instanceof Integer) writer.key(key).value((int) value);
					else if (value instanceof Float) writer.key(key).value((float) value);
					else if (value instanceof Long) writer.key(key).value((long) value);
					else if (value instanceof Double) writer.key(key).value((double) value);
					else if (value instanceof String) writer.key(key).value((String) value);
					else if (value instanceof BigDecimal) writer.key(key).value(value);
					else if (value instanceof BigInteger) writer.key(key).value(value);
					else if (value instanceof Enum) writer.key(key).value(value);
					else if (value instanceof byte[]) writer.key(key).value((byte[]) value);
					else if (currentObject.isAllowRawValue()) {
						writer.key(key).value(value.toString());
					}
				}
				if(!skipClose) {


					currentElement = elementStack.pollLast();
					writer.closeObject(currentElement instanceof CSONArray);
					currentIter = iteratorStack.pollLast();
					iteratorCount = iteratorCountStack.pollLast();


					if(writer.isComment) {

						// todo 코드 중복 풀어야함.
						if(!keyValueCommentObjectStack.isEmpty()) {
							ArrayDeque<CommentObject> commentObjects = keyValueCommentObjectStack.getLast();
							if(!commentObjects.isEmpty()) {
								CommentObject commentObject = commentObjects.getLast();
								String afterComment = commentObject.getTrailingComment();
								if(afterComment != null && !afterComment.isEmpty()) {
									writer.writeComment(afterComment, COMMENT_SLASH_STAR);
								}
							}
						}

						keyValueCommentObjectStack.pollLast();
						writer.currentKeyValueCommentObjects = keyValueCommentObjectStack.pollLast();
						if (writer.currentKeyValueCommentObjects == null) {
							writer.currentKeyValueCommentObjects = new ArrayDeque<>();
						}
					}
					lastClosed = true;
				}
				skipClose = false;

			} else if(currentElement instanceof CSONArray) {
				CSONArray currentArray = (CSONArray) currentElement;
				Iterator<java.lang.Object> iter = null;


				if(!lastClosed) {
					iter = currentArray.iterator();
					currentIter = iter;
					writer.openArray();
				} else {
					iter = (Iterator<java.lang.Object>) currentIter;
					lastClosed = false;
				}
				while(iter.hasNext()) {
					int index = iteratorCount.getAndIncrement();
					if(allowComment) {
						CommentObject commentObject = currentArray.getCommentObject(index);
						if(commentObject != null && commentObject.isCommented()) {
							writer.nextCommentObject(commentObject);
						}
					}

					java.lang.Object value = iter.next();

					if(value == null || value instanceof NullValue) writer.addNull();
					else if(value instanceof CSONElement)  {
						iteratorStack.add(iter);
						elementStack.add(currentElement);
						iteratorCountStack.add(iteratorCount);
						if(allowComment) {
							String beforeComment = null;
							if(writer.currentKeyValueCommentObjects != null) {
								CommentObject commentObject = writer.currentKeyValueCommentObjects.peekLast();
								if(commentObject != null) {
									beforeComment = commentObject.getLeadingComment();
								}
							}
							//String beforeComment = writer.currentKeyValueCommentObjects == null ? null : writer.currentKeyValueCommentObjects.getLast().get
							if(writer.currentKeyValueCommentObjects == null) {
								writer.currentKeyValueCommentObjects = new ArrayDeque<>();
							}
							keyValueCommentObjectStack.add(writer.currentKeyValueCommentObjects);

							writer.currentKeyValueCommentObjects = new ArrayDeque<>();
							if(beforeComment != null) {
								writer.currentKeyValueCommentObjects.addLast(new CommentObject(beforeComment, null));
							}

						}
						iteratorCount = new Count();
						writer.addCSONElement();
						currentElement = (CSONElement) value;
						skipClose = true;
						if(allowComment) {
							writer.currentKeyValueCommentObjects.clear();
						}
						break;
					}
					else if(value instanceof Byte)	writer.add((Byte)value);
					else if(value instanceof Short)	writer.add((Short)value);
					else if(value instanceof Character) writer.add((Character)value);
					else if(value instanceof Integer) writer.add((Integer)value);
					else if(value instanceof Float) writer.add((Float)value);
					else if(value instanceof Long) writer.add((Long)value);
					else if(value instanceof Double) writer.add((Double)value);
					else if(value instanceof String) writer.add((String)value);
					else if (value instanceof BigDecimal) writer.add((BigDecimal)value);
					else if (value instanceof BigInteger) writer.add((BigInteger)value);
					else if(value instanceof byte[]) writer.add((byte[])value);
					else if(value instanceof Boolean) writer.add((Boolean)value);
					else if (currentArray.isAllowRawValue()) {
						writer.add(value.toString());
					}
				}
				if(!skipClose) {
					writer.closeArray();
					currentIter = iteratorStack.pollLast();
					currentElement = elementStack.pollLast();
					iteratorCount = iteratorCountStack.pollLast();

					// todo 코드 중복 풀어야함.
					if(allowComment && !keyValueCommentObjectStack.isEmpty()) {
						ArrayDeque<CommentObject> commentObjects = keyValueCommentObjectStack.getLast();
						if(!commentObjects.isEmpty()) {
							CommentObject commentObject = commentObjects.getLast();
							String afterComment = commentObject.getTrailingComment();
							if(afterComment != null && !afterComment.isEmpty()) {
								writer.writeComment(afterComment, COMMENT_SLASH_STAR);
							}
						}
					}

					keyValueCommentObjectStack.pollLast();
					writer.currentKeyValueCommentObjects = keyValueCommentObjectStack.pollLast();
					lastClosed = true;
				}
				skipClose = false;
			}

		} while (currentElement != null);
		writer.writeComment(root.getTailComment(), false, "\n", "");
	}

}
