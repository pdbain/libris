package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.Vector;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

class DatabaseRecordList extends RecordList<DatabaseRecord> {
	Vector <DatabaseRecord> recordList;
	private LibrisDatabase database;
	public DatabaseRecordList(LibrisDatabase db) {
		database = db;
		
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
	public Record getRecord(int id) throws InputException {
		return database.getRecord(id);
	}

}
