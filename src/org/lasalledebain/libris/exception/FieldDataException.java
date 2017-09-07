package org.lasalledebain.libris.exception;

public class FieldDataException extends InputException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5756779516220869915L;

	public FieldDataException(String msg) {
		super(msg);
	}

	public FieldDataException(String msg, Exception e) {
		super(msg, e);
	}

}
