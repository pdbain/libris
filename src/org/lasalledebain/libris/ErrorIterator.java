package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.LibrisException;

public class ErrorIterator implements Iterator<Record> {

	private LibrisException exc;

	public ErrorIterator(LibrisException e) {
		this.exc = e;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Record next() {
		return null;
	}

	public LibrisException getException() {
		return exc;
	}

	@Override
	public void remove() {
		return;
	}

}
