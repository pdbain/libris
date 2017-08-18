package org.lasalledebain.libris.exception;

public class DatabaseError extends Error {

	public DatabaseError(String msg, Exception exc) {
		super(msg, exc);
	}

	public DatabaseError(String msg) {
		super(msg);
	}
}
