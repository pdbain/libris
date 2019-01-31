package org.lasalledebain.libris.exception;

import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;

@SuppressWarnings("serial")
public class InternalError extends Error {

	public InternalError(String msg) {
		super(msg);
		LibrisDatabase.log(Level.SEVERE, msg);
	}

	public InternalError(String msg, Exception e) {
		super(msg, e);
		LibrisDatabase.log(Level.SEVERE, msg, e);
	}
}
