package org.lasalledebain.libris.exception;

import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;

@SuppressWarnings("serial")
public class DatabaseError extends Error {

	public DatabaseError(String msg, Exception exc) {
		super(msg, exc);
		LibrisDatabase.log(Level.SEVERE, msg);
	}

	public DatabaseError(Exception exc) {
		super(exc);
		LibrisDatabase.log(Level.SEVERE, "Database error caused by "+exc.getMessage()+" at "+exc.getStackTrace()[0].toString());
	}

	public DatabaseError(String msg) {
		super(msg);
		LibrisDatabase.log(Level.SEVERE, msg);
	}
}
