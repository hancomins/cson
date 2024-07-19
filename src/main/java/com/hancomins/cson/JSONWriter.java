package com.hancomins.cson;

import com.hancomins.cson.util.EscapeUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;


@SuppressWarnings("UnusedReturnValue")
public class JSONWriter {

	private final static int DEFAULT_BUFFER_SIZE = 512;

	private final JSONOptions jsonOptions;

	private final static int COMMENT_BEFORE_KEY = 1;

	private final static int COMMENT_SLASH_STAR = 4;


	private final static int COMMENT_BEFORE_ARRAY_VALUE = 5;
	private final static int COMMENT_COMMA_AND_SLASH_STAR = 6;
	private final static int COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE = 7;

	private boolean isComment;
	private boolean isAllowLineBreak;

	private boolean isPretty = false;
	private boolean isUnprettyArray = false;
	private String depthSpace = "  ";

	private String keyQuote = "\"";
	private char keyQuoteChar = '"';
	private String valueQuote = "\"";
	private char valueQuoteChar = '"';
	private boolean isSpacialValueQuote = false;
	private boolean isSpacialKeyQuote = false;



	private final ArrayDeque<CommentObject> keyValueCommentObjects = new ArrayDeque<CommentObject>();


	private final ArrayDeque<ObjectType> typeStack_ = new ArrayDeque<>();
	private final StringBuilder stringBuilder = new StringBuilder(DEFAULT_BUFFER_SIZE);



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

	private void writeDepthTab(StringBuilder stringBuilder) {
		if(!isPretty) return;
		for (int i = 0, n = typeStack_.size(); i < n; i++) {
			stringBuilder.append(depthSpace);
		}
	}


	boolean isComment() {
		return isComment;
	}


	private String getAfterComment() {
		if(!keyValueCommentObjects.isEmpty()) {
			CommentObject commentObject = keyValueCommentObjects.removeFirst();
			String afterComment = commentObject.getAfterComment();
			if (afterComment == null) {
				return null;
			}
			return commentObject.getAfterComment();
		}
		return null;
	}

	private void writeAfterComment(int type) {
		String afterComment = getAfterComment();
		writeComment(afterComment, type);
	}



	private void writeBeforeComment(int type) {
		if(!keyValueCommentObjects.isEmpty()) {
			CommentObject commentObject = keyValueCommentObjects.getFirst();
			String beforeComment  = commentObject.getBeforeComment();
			if(beforeComment == null) {
				return;
			}
			writeComment(beforeComment, type);
			commentObject.setBeforeComment(null);
		}
	}



	public JSONWriter(JSONOptions jsonOptions) {
		this.jsonOptions = jsonOptions;
		if(jsonOptions.isPretty()) {
			isPretty = true;
		}
		if(jsonOptions.isUnprettyArray()) {
			isUnprettyArray = true;
		}
		if(jsonOptions.getDepthSpace() != null) {
			depthSpace = jsonOptions.getDepthSpace();
		}


		keyQuote = jsonOptions.getKeyQuote();
		valueQuote = jsonOptions.getValueQuote();
		if(!jsonOptions.isAllowUnquoted() && keyQuote.isEmpty()) {
			keyQuote = "\"";
		}
		if(!jsonOptions.isAllowSingleQuotes()) {
			if(keyQuote.equals("'")) {
				keyQuote = jsonOptions.isAllowUnquoted() ? "" : "\"";

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
		isAllowLineBreak = jsonOptions.isAllowLineBreak();

		isComment = jsonOptions.isAllowComments() && !jsonOptions.isSkipComments();
	}


	private void writeComment(String comment, int commentType) {

		if(comment != null && isComment) {
			String[] commentLines = comment.split("\n");
			boolean isSlashStar = !isPretty || commentType == COMMENT_SLASH_STAR || commentType == COMMENT_COMMA_AND_SLASH_STAR || commentType == COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE;
			boolean isMultiLine = commentLines.length > 1;
			for (int i = 0, n = commentLines.length; i < n; i++) {
				String commentLine = commentLines[i].trim();
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
						stringBuilder.append("/* ");
					} else {
						stringBuilder.append("\n");
					}

					if(i != 0) {
						writeDepthTab(stringBuilder);
					}

					stringBuilder.append(commentLine);



				}
				else if(commentType == COMMENT_BEFORE_ARRAY_VALUE) {

					stringBuilder.append("//");
					stringBuilder.append(commentLine);



					stringBuilder.append("\n");
					writeDepthTab(stringBuilder);


				}
				else if(commentType == COMMENT_BEFORE_KEY) {
					writeDepthTab(stringBuilder);
					stringBuilder.append("//");
					stringBuilder.append(commentLine);
					stringBuilder.append("\n");
				}
			}

			if(isSlashStar) {
				stringBuilder.append(" */");
				if(isMultiLine) {
					stringBuilder.append("\n");
					writeDepthTab(stringBuilder);
				}
			}

		}
	}


	void nextCommentObject(CommentObject commentObject) {
		if(commentObject == null) {
			keyValueCommentObjects.addLast(new CommentObject());
			return;
		}


		this.keyValueCommentObjects.addLast(commentObject.clone());
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
		stringBuilder.append(Base64.encode(value));
		stringBuilder.append('"');
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter value(CSONElement value) {
		writeBeforeComment(COMMENT_SLASH_STAR);
		String afterValueComment = getAfterComment();
		value.write(this, false);
		writeComment(afterValueComment, COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter value(Object value) {
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
			stringBuilder.append(value);
		} else if(value instanceof Boolean) {
			stringBuilder.append(value);
		} else if(value instanceof byte[]) {
			stringBuilder.append("\"base64,");
			stringBuilder.append(Base64.encode((byte[])value));
			stringBuilder.append('"');
		} else  {
			stringBuilder.append('"');
			stringBuilder.append(value);
			stringBuilder.append('"');
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
		String quote =  jsonOptions.isAllowCharacter() ? "'" : valueQuote;
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
		writeFloat(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

 	private void writeFloat(float value) {
		if(jsonOptions.isAllowInfinity() && Float.isInfinite(value)) {
			if(value > 0) {
				stringBuilder.append("Infinity");
			} else {
				stringBuilder.append("-Infinity");
			}
		} else if(Float.isNaN(value)) {
			if(jsonOptions.isAllowNaN()) {
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
		writeDouble(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	private void writeDouble(double value) {
		if(jsonOptions.isAllowInfinity() && Double.isInfinite(value)) {
			if(value > 0) {
				stringBuilder.append("Infinity");
			} else {
				stringBuilder.append("-Infinity");
			}
		} else if(Double.isNaN(value)) {
			if(jsonOptions.isAllowNaN()) {
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

		writeBeforeComment(COMMENT_COMMA_AND_SLASH_STAR_IN_ARRAY_VALUE);
		if(isPretty && !isUnprettyArray) {
			stringBuilder.append('\n');
			writeDepthTab(stringBuilder);
		}
	}

	///
	public JSONWriter addNull() {
		checkAndAppendInArray();
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
		stringBuilder.append("\"base64,");
		stringBuilder.append(Base64.encode(value));
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
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter add(BigInteger value) {
		if(value== null) {
			addNull();
			return this;
		}

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


	public JSONWriter add(CSONElement value) {
		checkAndAppendInArray();
		String afterComment = getAfterComment();
		value.write(this, false);
		if(afterComment != null) {
			writeComment(afterComment, COMMENT_SLASH_STAR);
		}
		//writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}


	public JSONWriter add(byte value) {
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);;
		return this;
	}

	public JSONWriter add(int value) {
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(long value) {

		checkAndAppendInArray();
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(short value) {

		checkAndAppendInArray();
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}

	public JSONWriter add(boolean value) {

		checkAndAppendInArray();
		stringBuilder.append(value ? "true" : "false");
		writeAfterComment(COMMENT_SLASH_STAR);
		return this;
	}



	public JSONWriter add(char value) {
		checkAndAppendInArray();
		String quote =  jsonOptions.isAllowCharacter() ? "'" : valueQuote;
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

		writeBeforeComment(commentType);

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
		return this;
	}

	public JSONWriter closeObject() {
		if(typeStack_.getLast() != ObjectType.Object && typeStack_.getLast() != ObjectType.OpenObject) {
			throw new CSONWriteException();
		}
		removeStack();
		/*if(commentStringAfterObjectValue != null) {
			writeComment(commentStringAfterObjectValue, COMMENT_SLASH_START);
			commentStringAfterObjectValue = null;
		}*/

		if(isPretty) {
			stringBuilder.append('\n');
			writeDepthTab(stringBuilder);
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

}
