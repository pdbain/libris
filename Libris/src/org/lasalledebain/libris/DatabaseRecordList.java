package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.Vector;

import org.lasalledebain.libris.exception.LibrisException;

public class DatabaseRecordList extends RecordList {
	Vector <Record> recordList;
	public DatabaseRecordList(LibrisDatabase database) {
		super(database);
		
	}

	@Override
	public Iterator<Record> iterator() {
		Iterator<Record> iter;
		try {
			iter = database.getDatabaseRecords().iterator();
		} catch (LibrisException e) {
			return new ErrorIterator(e);
		}
		return iter;
	}

	@Override
	public Record getRecord(RecordId id) throws LibrisException {
		return database.getRecord(id);
	}

}
