package org.lasalledebain.libris.exception;

import java.io.File;

public class InputException extends LibrisException {
	/**
	 * For errors in the schema or user-provided data.
	 */
	private static final long serialVersionUID = -3283523417194275968L;

	public InputException(String msg, File source, Exception e) {
		super(source.getPath()+": "+msg, e);
	}

	public InputException(File source, Exception e) {
		super(source.getPath()+": "+e.getMessage(), e);
	}

	public InputException(String msg, String sourceFilePath, Exception e) {
		super(sourceFilePath+": "+msg, e);
		}
	
	public InputException(String msg, Exception e) {
		super(msg, e);
	}

	public InputException(String msg) {
		super(msg);
}

	public InputException() {
		super();
	}

	public InputException(Exception e) {
		super(e);
	}

}
