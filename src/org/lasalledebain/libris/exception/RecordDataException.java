package org.lasalledebain.libris.exception;


public class RecordDataException extends DatabaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7429505775675080171L;

	public RecordDataException(String msg) {
		super(msg);
	}

	public RecordDataException(String msg, Exception cause) {
		super(msg, cause);
	}

}
