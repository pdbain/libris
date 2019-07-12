package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.Vector;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

class DatabaseRecordList extends RecordList<DatabaseRecord> {
	Vector <DatabaseRecord> recordList;
	private LibrisDatabase database;
	public DatabaseRecordList(LibrisDatabase db) {
		database = db;
		
	}

	@Override
	public Iterator<DatabaseRecord> iterator() {
		Iterator<DatabaseRecord> iter;
		try {
			iter = database.getDatabaseRecords().iterator();
		} catch (LibrisException e) {
			throw new DatabaseError("Error getting database records", e);
		}
		return iter;
	}

	@Override
	public DatabaseRecord getRecord(int id) throws InputException {
		return database.getRecord(id);
	}

}
