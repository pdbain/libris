package org.lasalledebain.libris.search;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;

public interface RecordFilter {
	public abstract boolean matches(Record rec) throws InputException;
	
}
