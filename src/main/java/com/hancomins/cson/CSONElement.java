package com.hancomins.cson;


import com.hancomins.cson.options.StringFormatOption;
import com.hancomins.cson.util.NullValue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract  class CSONElement implements Iterable<Object>  {


	private static StringFormatOption<?> DefaultJSONOptions = StringFormatOption.json5();

	private static final Pattern BASE64_PREFIX_REPLACE_PATTERN = Pattern.compile("(?i)^base64,");
	private static final Pattern BASE64_PREFIX_PATTERN = Pattern.compile("^((?i)base64,)([a-zA-Z0-9+/]*={0,2})$");
	private CommentObject headTailCommentObject = null;
	private CSONPath csonPath = null;


	private CSONElement parents = null;
	private byte[] versionRaw = BinaryCSONDataType.VER_RAW;
	private final ElementType type;

	private StringFormatOption<?> jsonOptions = DefaultJSONOptions;


	protected boolean allowJsonPathKey = true;
	private boolean allowRawValue = false;
	private boolean unknownObjectToString = false;


	protected CSONElement(ElementType type, StringFormatOption<?> stringFormatOption) {
		this.type = type;
		this.jsonOptions = stringFormatOption;
	}

	protected CSONElement setAllowRawValue(boolean allowRawValue) {
		this.allowRawValue = allowRawValue;
		return this;
	}

	@SuppressWarnings({"unchecked", "unused"})
	public <T extends CSONElement> T setAllowJsonPathKey(boolean allowJsonPathKey) {
		this.allowJsonPathKey = allowJsonPathKey;
		return (T)this;
	}

	public void setUnknownObjectToString(boolean unknownObjectToString) {
		this.unknownObjectToString = unknownObjectToString;
	}


	protected boolean isUnknownObjectToString() {
		return unknownObjectToString;
	}

	protected boolean isAllowRawValue() {
		return allowRawValue;
	}


	@SuppressWarnings("unused")
	public static StringFormatOption<?> getDefaultStringFormatOption() {
		return DefaultJSONOptions;
	}


	@SuppressWarnings("unused")
	public static void setDefaultStringFormatOption(StringFormatOption<?> defaultJSONOptions) {
		DefaultJSONOptions = defaultJSONOptions;
	}


	public void setStringFormatOption(StringFormatOption<?> defaultJSONOptions) {
		this.jsonOptions = defaultJSONOptions;
	}

	public StringFormatOption<?> getStringFormatOption() {
		return this.jsonOptions;
	}



	@SuppressWarnings({"unchecked", "UnusedReturnValue"})
	public <T extends CSONElement> T setHeadComment(String comment) {
		if(headTailCommentObject == null) {
			headTailCommentObject = new CommentObject();
		}
		headTailCommentObject.setLeadingComment(comment);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public  <T extends CSONElement> T setTailComment(String comment) {
		if(headTailCommentObject == null) {
			headTailCommentObject = new CommentObject();
		}
		headTailCommentObject.setTrailingComment(comment);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
    public <T extends CSONElement> T addHeadComment(String comment) {
		if(headTailCommentObject == null) {
			headTailCommentObject = new CommentObject();
		}
		headTailCommentObject.appendLeadingComment(comment);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public  <T extends CSONElement> T addTailComment(String comment) {
		if(headTailCommentObject == null) {
			headTailCommentObject = new CommentObject();
		}
		headTailCommentObject.appendTrailingComment(comment);
		return (T) this;
	}




	/**
	 * @deprecated use {@link #getHeadComment()} instead
	 **/
	@Deprecated
	public String getCommentAfterThis() {
		return getTailComment();
	}

	public String getTailComment() {
		return headTailCommentObject == null ? null : headTailCommentObject.getTrailingComment();
	}

	public String getHeadComment() {
		return  headTailCommentObject == null ? null : headTailCommentObject.getLeadingComment();
	}


	/**
	 * @deprecated use {@link #setHeadComment(String)} instead
	 */
	@Deprecated
	public String getCommentThis() {
		return getHeadComment();
	}


	public final CSONPath getCsonPath() {
		if(csonPath == null) {
			csonPath = new CSONPath(this);
		}
		return csonPath;
	}



	protected abstract void write(JSONWriter writer, boolean root);


	public abstract String toString(StringFormatOption<?> option);


	public enum ElementType { Object, Array}



	protected void setParents(CSONElement parents) {
		this.parents = parents;
	}



	public CSONElement getParents() {
		return parents;
	}

	public ElementType getType() {
		return type;
	}


	public String getVersion() {
		return Short.toString(ByteBuffer.wrap(versionRaw).getShort());
	}

	protected void setVersion(byte[] versionRaw) {
		this.versionRaw = versionRaw;
	}



	@SuppressWarnings("unchecked")
	public static <T extends CSONElement> T clone(T element) {
		if(element == null) return null;
		if(element instanceof CSONObject) {
			return (T) ((CSONObject)element).clone();
		}
		return (T) ((CSONArray)element).clone();
	}


	public static boolean isBase64String(String value) {
		return BASE64_PREFIX_PATTERN.matcher(value).matches();
	}

	public static byte[] base64StringToByteArray(String value) {
		value = BASE64_PREFIX_REPLACE_PATTERN.matcher(value).replaceAll("");
		return Base64.getDecoder().decode(value);
	}

	public abstract void clear();
	public abstract byte[] toCSONBinary();
	public abstract void writeCSONBinary(OutputStream outputStream) throws IOException;


	protected static boolean containsNoStrict(Collection<Object> valueList, Object value) {
		for(Object obj : valueList) {
			boolean result = Objects.equals(obj, value);
			if(result) return true;
			else if(obj == NullValue.Instance || obj == null) {
				if(value == NullValue.Instance || value == null) return true;
				else continue;
			} else if(value == null) {
				return false;
			}
			String strObj = obj instanceof byte[] ? Base64.getEncoder().encodeToString((byte[]) obj) : obj.toString();
			String strValue = value instanceof byte[] ? Base64.getEncoder().encodeToString((byte[])value) : value.toString();
			if(strObj.equals(strValue)) {
				return true;
			}
		}
		return false;
	}





}
