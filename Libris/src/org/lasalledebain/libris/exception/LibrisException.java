package org.lasalledebain.libris.exception;

import java.util.HashMap;

public abstract class LibrisException extends Exception {

	// TODO refactor exceptions
	// system exceptions, I/O etc
	// user error
	// coding error
	// make factories for assertions
	/**
	 * 
	 */
	private static final long serialVersionUID = -2706811107093235124L;
	private static HashMap <Thread, LibrisException> lastException;

	public LibrisException(String msg) {
		super(msg);
		setStackTrace(Thread.currentThread().getStackTrace());
	}

	public LibrisException(String msg, Exception cause) {
		super(msg, cause);
	}

	public LibrisException() {
		super();
	}

	public LibrisException(Exception e) {
		super(e);
	}

	/**
	 * save the latest exception thrown on the current thread.
	 * @param exc exception to be thrown
	 */
	public static void saveException(LibrisException exc) {
		if (null == lastException) {
			lastException = new HashMap<Thread, LibrisException>();
		}
		lastException.put(Thread.currentThread(), exc);
	}

	/**
	 * Destructively read the latest exception thrown on the current thread.
	 * @return last exception thrown.
	 */
	public static LibrisException getLastException() {
		if (null == lastException) {
			return null;
		} else {
			 LibrisException exc = lastException.remove(Thread.currentThread());
			return exc;
		}
	}
}
