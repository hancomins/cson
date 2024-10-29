package com.clipsoft.cson;


import com.clipsoft.cson.util.NullValue;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract  class CSONElement implements Iterable<Object>  {


	private static StringFormatOption<?> DefaultJSONOptions = StringFormatOption.jsonPure();

	private final static Pattern BASE64_PREFIX_REPLACE_PATTERN = Pattern.compile("(?i)^base64,");
	private final static Pattern BASE64_PREFIX_PATTERN = Pattern.compile("^((?i)base64,)([a-zA-Z0-9+/]*={0,2})$");
	private CommentObject commentAfterElement = null;
	private CommentObject commentBeforeElement = null;
	private CSONPath csonPath = null;


	StringFormatOption<?> defaultJSONOptions = DefaultJSONOptions;


	protected boolean allowJsonPathKey = true;
	private boolean allowRawValue = false;
	private boolean unknownObjectToString = false;
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
		this.defaultJSONOptions = defaultJSONOptions;
	}

	public StringFormatOption<?> getStringFormatOption() {
		return defaultJSONOptions;
	}


	CommentObject getCommentAfterElement() {
		return commentAfterElement;
	}


	@SuppressWarnings({"unchecked", "UnusedReturnValue"})
	public <T extends CSONElement> T setCommentThis(String comment) {
		if(commentBeforeElement == null) {
			commentBeforeElement = new CommentObject();
		}
		commentBeforeElement.setBeforeComment(comment);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public  <T extends CSONElement> T setCommentAfterThis(String comment) {
		if(commentAfterElement == null) {
			commentAfterElement = new CommentObject();
		}
		commentAfterElement.setAfterComment(comment);
		return (T) this;
	}

	protected CommentObject getOrCreateTailCommentObject() {
		return commentAfterElement == null ? commentAfterElement = new CommentObject() : commentAfterElement;
	}


	public String getCommentAfterThis() {
		return commentAfterElement == null ? null : commentAfterElement.getComment();
	}

	public String getCommentThis() {
		return commentBeforeElement == null ? null : commentBeforeElement.getComment();
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

	private CSONElement mParents = null;
	private byte[] versionRaw = BinaryCSONDataType.VER_RAW;
	private final ElementType mType;


	protected void setParents(CSONElement parents) {
		mParents = parents;
	}

	protected CSONElement(ElementType type) {
		this.mType = type;
	}

	public CSONElement getParents() {
		return mParents;
	}

	public ElementType getType() {
		return mType;
	}

	@SuppressWarnings("unused")
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
		return Base64.decode(value);

	}

	public abstract void clear();

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
			String strObj = obj instanceof byte[] ? Base64.encode((byte[]) obj) : obj.toString();
			String strValue = value instanceof byte[] ? Base64.encode((byte[])value) : value.toString();
			if(strObj.equals(strValue)) {
				return true;
			}
		}
		return false;
	}


	// 0.9.x //
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof CSONElement)) {
			return false;
		}
		return CSONElements.equals(this, (CSONElement) obj);
	}

	// 0.9.x
	public boolean equalsIgnoreTypes(Object obj) {
		if(!(obj instanceof CSONElement)) {
			return false;
		}
		return CSONElements.equalsIgnoreTypes(this, (CSONElement) obj);
	}





}
