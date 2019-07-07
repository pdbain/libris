package org.lasalledebain.libris.exception;

@SuppressWarnings("serial")
public class DatabaseError extends Error {

	public DatabaseError(String msg, Exception exc) {
		super(msg, exc);
	}

	public DatabaseError(String msg) {
		super(msg);
	}
	
	public static void assertTrue(String message, boolean condition) throws DatabaseError {
		if (!condition) {
			throw new DatabaseError(message);
		}
	}
}
