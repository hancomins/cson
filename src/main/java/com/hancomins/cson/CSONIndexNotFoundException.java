package com.hancomins.cson;

public class CSONIndexNotFoundException extends RuntimeException {

	CSONIndexNotFoundException() {
		super();
	}


	CSONIndexNotFoundException(String key) {
		super("'" + key + "' is not found.");
	}
	
	CSONIndexNotFoundException(Exception cause) {
		super(cause);
	}
}
