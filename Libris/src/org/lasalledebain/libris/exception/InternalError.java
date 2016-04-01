package org.lasalledebain.libris.exception;

import org.lasalledebain.libris.LibrisDatabase;

public class InternalError extends Error {

	public InternalError(String msg) {
		super(msg);
		LibrisDatabase.librisLogger.severe(msg);
		System.err.println(msg);
	}

	public InternalError(String msg, Exception e) {
		super(msg, e);
		LibrisDatabase.librisLogger.severe(msg);
		System.err.println(msg);
	}
}
