package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.Vector;

import org.lasalledebain.libris.exception.InputException;

class DatabaseRecordList extends RecordList<DatabaseRecord> {
	Vector <DatabaseRecord> recordList;
	private LibrisDatabase database;
	public DatabaseRecordList(LibrisDatabase db) {
		database = db;
		
	}

	@Override
	public Iterator<DatabaseRecord> iterator() {
		Iterator<DatabaseRecord> iter;
		iter = database.getDatabaseRecords().iterator();
		return iter;
	}

	@Override
	public DatabaseRecord getRecord(int id) throws InputException {
		return database.getRecord(id);
	}

}
