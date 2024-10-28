package com.hancomins.cson.format;

import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.util.ArrayStack;

import java.util.Map;


@SuppressWarnings("UnusedReturnValue")
public abstract class WriterBorn implements FormatWriter {


	private final ArrayStack<DataIterator<?>> dataContainerIteratorStack = new ArrayStack<>();
	private final ArrayStack<BaseDataContainer> dataContainerStack = new ArrayStack<>();
	private final ArrayStack<CommentObject<?>> commentStack;

	private DataIterator<?> currentIterator;

	private BaseDataContainer currentDataContainer;
	private boolean rootContainerIsArray = false;

	private final boolean skipComments;

	protected boolean isArrayRootContainer() {
		return rootContainerIsArray;
	}



	protected WriterBorn(boolean skipComments) {
		this.skipComments = skipComments;
		if(!skipComments) {
			commentStack = new ArrayStack<>();
		} else {
			commentStack = null;
		}
	}




	@SuppressWarnings("unchecked")
	public void write(BaseDataContainer dataContainer) {
        rootContainerIsArray = dataContainer instanceof ArrayDataContainer;
		writePrefix();
		if(!isSkipComments()) {
			String header = dataContainer.getComment(CommentPosition.HEADER);
			writeHeaderComment(header);
		}
		currentDataContainer = dataContainer;
		this.currentIterator = dataContainer.iterator();
		dataContainerIteratorStack.push(currentIterator);
		dataContainerStack.push(currentDataContainer);

		LOOP:
		while(!dataContainerIteratorStack.isEmpty()) {
			if(this.currentIterator.isBegin()) {
				writeContainerPrefix(this.dataContainerStack.top(), this.currentIterator);
			}

			while (this.currentIterator.hasNext()) {
				if (this.currentIterator.isKeyValue()) {
					if(!writeObject((DataIterator<Map.Entry<String, Object>>)this.currentIterator)) {
						continue LOOP;
					}
				} else {
					if(!writeArray(this.currentIterator)) {
						continue LOOP;
					}
				}
			}
			BaseDataContainer oldDataContainer = currentDataContainer;
			DataIterator<?> lastIterator = currentIterator;


			dataContainerIteratorStack.pop();
			dataContainerStack.pop();
			currentIterator =  dataContainerIteratorStack.top();
			currentDataContainer =  dataContainerStack.top();

			if(oldDataContainer instanceof KeyValueDataContainer) {
				writeObjectSuffix((DataIterator<Map.Entry<String, Object>>) lastIterator);
			} else if(oldDataContainer instanceof ArrayDataContainer) {
				writeArraySuffix((DataIterator<Object>)lastIterator);
			}
			if(!skipComments) {
				//noinspection DataFlowIssue
				commentStack.poll();
			}
		}

		writeSuffix();
		if(!isSkipComments()) {
			String footerComment = dataContainer.getComment(CommentPosition.FOOTER);
			writeFooterComment(footerComment);
		}
		endWrite();


	}

	@SuppressWarnings("unchecked")
    private void writeContainerPrefix(BaseDataContainer parents, DataIterator<?> iterator) {
		if(iterator.isKeyValue()) {
			writeObjectPrefix(parents, (DataIterator<Map.Entry<String, Object>>) iterator);
		} else {
			writeArrayPrefix(parents,iterator);
		}
	}

	abstract protected void writeHeaderComment(String comment);

	abstract protected void writeFooterComment(String comment);


	abstract protected void writePrefix();
	abstract protected void writeSuffix();
	abstract protected void endWrite();

	private void intoChildBaseDataContainer(BaseDataContainer value) {
		currentDataContainer = value;
		dataContainerStack.push(currentDataContainer);
		currentIterator = currentDataContainer.iterator();
		dataContainerIteratorStack.push(currentIterator);

	}

	protected boolean isSkipComments() {
		return skipComments;
	}

	protected final CommentObject<?> getCurrentCommentObject() {
		if(skipComments) {
			return null;
		}
        //noinspection DataFlowIssue
        return commentStack.top();
	}


	abstract protected void writeArrayPrefix(BaseDataContainer parents, DataIterator<?> iterator);
	abstract protected void writeObjectPrefix(BaseDataContainer parents, DataIterator<Map.Entry<String, Object>> iterator);
	abstract protected void writeObjectSuffix(DataIterator<Map.Entry<String, Object>> iterator);
	abstract protected void writeArraySuffix(DataIterator<Object> iterator);
	abstract protected void writeKey(String key);
	abstract protected void writeObjectValue(Object value);
	abstract protected void writeArrayValue(Object value);


	private boolean writeArray(DataIterator<?> iterator) {
		if(!skipComments) {
			//noinspection DataFlowIssue
			commentStack.push(((ArrayDataContainer)currentDataContainer).getCommentObject(iterator.getPosition()));
		}
		Object value = iterator.next();
		if (value instanceof BaseDataContainer) {
			intoChildBaseDataContainer((BaseDataContainer)value);
			return false;
		}
		writeArrayValue(value);
		if(!skipComments) {
			commentStack.poll();
		}
		return true;
	}

	private boolean writeObject(DataIterator<Map.Entry<String, Object>> iterator) {
		Map.Entry<String, Object> entry = iterator.next();
		String key = entry.getKey();
		Object value = entry.getValue();
		if(!skipComments) {
            //noinspection DataFlowIssue
            commentStack.push(((KeyValueDataContainer)currentDataContainer).getCommentObject(key));
		}
		writeKey(key);
		if (value instanceof BaseDataContainer) {
			intoChildBaseDataContainer((BaseDataContainer)value);
			return false;
		}
		writeObjectValue(value);
		if(!skipComments) {
			commentStack.poll();
		}
		return true;
	}

 
}
