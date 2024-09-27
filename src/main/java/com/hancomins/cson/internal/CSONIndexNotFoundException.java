package com.hancomins.cson.internal;

public class CSONIndexNotFoundException extends CSONException {


	CSONIndexNotFoundException(String object, int key) {
		super( object +  "['" + key + "'] is not found.");
	}

	CSONIndexNotFoundException(String object, String key) {
		super( object +  "['" + key + "'] is not found.");
	}

	CSONIndexNotFoundException(String object) {
		super(object);
	}
	
	CSONIndexNotFoundException(Exception cause) {
		super(cause);
	}
}
