package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class DatabaseRecordList extends RecordList<DatabaseRecord> {

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

	public RecordList<Record> ofRecords() throws LibrisException {
		return new RecordList<Record> () {
			Iterator<DatabaseRecord> recs = database.getDatabaseRecords().iterator();

			@Override
			public Iterator<Record> iterator() {
				Iterator<Record> iter;
				iter = new Iterator<Record>() {

					@Override
					public boolean hasNext() {
						return recs.hasNext();
					}

					@Override
					public Record next() {
						return recs.next();
					}

				};
				return iter;
			}

			@Override
			public Record getRecord(int id) throws InputException {
				return database.getRecord(id);
			}

			@Override
			public int size() {
				return database.getLastRecordId();
			}

		};
	}

}
