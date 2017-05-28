package org.lasalledebain.libris.xmlUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.lasalledebain.libris.exception.DatabaseException;

public class LibrisEmptyAttributes extends LibrisAttributes {
	private static LibrisEmptyAttributes attrs = null;
	public static LibrisEmptyAttributes getLibrisEmptyAttributes() {
		if (null == attrs) {
			attrs = new LibrisEmptyAttributes();
		}
		return (attrs);
	}
	private LibrisEmptyAttributes() {
		return;
	}
	@Override
	public void setAttribute(String key, String value) {
		throw new InternalError("Attempt to add "+key+"="+value+" to empty Attributes object");
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public LibrisAttributes getNext() {
		return null;
	}

	@Override
	public Iterator<String[]> iterator() {;
		return (new ArrayList<String[]>(0)).iterator();
	}

	@Override
	public void putAll(Map<String, String> attribs) throws DatabaseException {
		throw new InternalError("Attempt to add to empty Attributes object");
		}

	@Override
	public void setNext(LibrisAttributes next) {
		super.setNext(next);
	}

}
