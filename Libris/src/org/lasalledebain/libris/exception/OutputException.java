package org.lasalledebain.libris.exception;


public class OutputException extends LibrisException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1812541886292279961L;

	public OutputException(String msg) {
		super(msg);
	}

	public OutputException(String msg, Exception cause) {
		super(msg, cause);
	}

}
