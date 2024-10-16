package com.hancomins.cson.format;

import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.util.ArrayStack;

import java.util.Map;


@SuppressWarnings("UnusedReturnValue")
public abstract class WriterBorn implements FormatWriter {


	private final ArrayStack<DataIterator<?>> dataContainerIteratorStack = new ArrayStack<>();
	private final ArrayStack<BaseDataContainer> dataContainerStack = new ArrayStack<>();
	private DataIterator<?> currentIterator;
	private BaseDataContainer currentDataContainer;
	private boolean rootContainerIsArray = false;

	protected boolean isArrayRootContainer() {
		return rootContainerIsArray;
	}




	@SuppressWarnings("unchecked")
	public void write(BaseDataContainer dataContainer) {
		BaseDataContainer rootContainer = dataContainer;
		rootContainerIsArray = rootContainer instanceof ArrayDataContainer;
		writePrefix();
		String header = dataContainer.getComment(CommentPosition.HEADER);
		writeHeaderComment(header);
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
			dataContainerIteratorStack.pop();
			dataContainerStack.pop();
			currentIterator =  dataContainerIteratorStack.top();
			currentDataContainer =  dataContainerStack.top();
			if(oldDataContainer instanceof KeyValueDataContainer) {
				writeObjectSuffix();
			} else if(oldDataContainer instanceof ArrayDataContainer) {
				writeArraySuffix();
			}

		}

		String footerComment = rootContainer.getComment(CommentPosition.FOOTER);
		writeFooterComment(footerComment);

		writeSuffix();
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


	private void intoChildBaseDataContainer(BaseDataContainer value) {
		currentDataContainer = value;
		dataContainerStack.push(currentDataContainer);
		currentIterator = currentDataContainer.iterator();
		dataContainerIteratorStack.push(currentIterator);
	}

	abstract protected void writeArrayPrefix(BaseDataContainer parents, DataIterator<?> iterator);
	abstract protected void writeObjectPrefix(BaseDataContainer parents, DataIterator<Map.Entry<String, Object>> iterator);
	abstract protected void writeObjectSuffix();
	abstract protected void writeArraySuffix();
	abstract protected void writeKey(String key);
	abstract protected void writeValue(Object value);


	private boolean writeArray(DataIterator<?> iterator) {
		Object value = iterator.next();
		if (value instanceof BaseDataContainer) {
			intoChildBaseDataContainer((BaseDataContainer)value);
			return false;
		}
		writeValue(value);
		return true;
	}

	private boolean writeObject(DataIterator<Map.Entry<String, Object>> iterator) {
		Map.Entry<String, Object> entry = iterator.next();
		String key = entry.getKey();
		Object value = entry.getValue();
		writeKey(key);
		if (value instanceof BaseDataContainer) {
			intoChildBaseDataContainer((BaseDataContainer)value);
			return false;
		}
		writeValue(value);
		return true;
	}








 
}
