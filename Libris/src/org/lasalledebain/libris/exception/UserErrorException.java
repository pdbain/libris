package org.lasalledebain.libris.exception;

import java.io.IOException;


public class UserErrorException extends LibrisException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4641187002727883785L;

	public UserErrorException(String msg) {
		super(msg);
	}

	public UserErrorException(String msg, IOException cause) {
		super(msg, cause);
	}

}
