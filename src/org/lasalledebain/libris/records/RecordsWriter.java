package org.lasalledebain.libris.records;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public interface RecordsWriter<RecordType extends Record> {
	
	public abstract void put(RecordType rec) throws LibrisException;
	public abstract void addAll(Iterable<RecordType> recList) throws LibrisException;
	public abstract void closeFile() throws DatabaseException;
}
