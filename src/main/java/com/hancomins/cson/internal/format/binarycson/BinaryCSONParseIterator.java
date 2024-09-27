package com.hancomins.cson.internal.format.binarycson;


import com.hancomins.cson.internal.CSONArray;
import com.hancomins.cson.internal.CSONElement;
import com.hancomins.cson.internal.CSONObject;

class BinaryCSONParseIterator implements BinaryCSONBufferReader.ParseCallback {
	String selectKey = null;
	CSONElement currentElement;
	CSONElement root;
	byte[] versionRaw;
	
	public CSONElement release() {
		CSONElement result = root;
		selectKey = null;
		currentElement = null;
		root = null;
		return result;
	}
	
	
	
	@Override
	public void onVersion(byte[] versionRaw) {
		this.versionRaw = versionRaw;
	}
	
	@Override
	public void onValue(Object value) {	
		if(currentElement.getType() == CSONElement.ElementType.Object) {
			((CSONObject) currentElement).put(selectKey, value);
			selectKey = null;
		} else {
			((CSONArray) currentElement).put(value);
		}
	}
	
	@Override
	public void onOpenObject() {
		CSONObject obj = new CSONObject();
		obj.setVersion(this.versionRaw);
		if(currentElement == null)
		{
			currentElement = obj;
			return;
		}				
		else if(currentElement.getType() == CSONElement.ElementType.Object) {
			((CSONObject) currentElement).put(selectKey, obj);
			selectKey = null;
		} else {
			((CSONArray) currentElement).add(obj);
		}
		// todo 개서필요.
		//obj.setParents(currentElement);
		currentElement = obj;
	}
	
	@Override
	public void onOpenArray() {
		CSONArray obj = new CSONArray();
		obj.setVersion(this.versionRaw);
		if(currentElement == null)
		{
			currentElement = obj;
			return;
		}		
		else if(currentElement.getType() == CSONElement.ElementType.Object) {
			((CSONObject) currentElement).put(selectKey, obj);
			selectKey = null;
		} else {
			((CSONArray) currentElement).add(obj);
		}
		// 개선 필요.
		//obj.setParents(currentElement);
		currentElement = obj;
		
		
	}
	
	@Override
	public void onKey(String key) {
		selectKey = key;
	}
	
	@Override
	public void onCloseObject() {
		onCloseCSONElement();
	}
	
	@Override
	public void onCloseArray() {
		onCloseCSONElement();
	}
	
	private void onCloseCSONElement() {
		// todo 개선 필욘
		//CSONElement parents =  currentElement.getParents();
		/*if(parents ==null) {
			root = currentElement;
			return;
		}
		currentElement = parents;*/
	}

	
}
