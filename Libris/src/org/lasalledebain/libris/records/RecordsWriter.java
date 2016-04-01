package org.lasalledebain.libris.records;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public interface RecordsWriter {
	
	public abstract void put(Record rec) throws LibrisException;
	public abstract void addAll(Iterable<Record> recList) throws LibrisException;
	public abstract void closeFile() throws DatabaseException;
}
