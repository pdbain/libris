package org.lasalledebain.libris.exception;

public class DatabaseError extends Error {

	public DatabaseError(String msg, InputException exc) {
		super(msg, exc);
	}

	public DatabaseError(String msg) {
		super(msg);
	}
}
