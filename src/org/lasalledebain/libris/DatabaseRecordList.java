package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

class DatabaseRecordList extends RecordList<DatabaseRecord> {

	private GenericDatabase<DatabaseRecord> database;
	public DatabaseRecordList(GenericDatabase<DatabaseRecord> db) {
		database = db;
		
	}

	@Override
	public Iterator<DatabaseRecord> iterator() {
		Iterator<DatabaseRecord> iter;
		try {
			iter = database.getDatabaseRecords().iterator();
		} catch (LibrisException e) {
			throw new DatabaseError(e);
		}
		return iter;
	}

	@Override
	public DatabaseRecord getRecord(int id) throws InputException {
		return database.getRecord(id);
	}

	@Override
	public int size() {
		return database.getLastRecordId();
	}

}
