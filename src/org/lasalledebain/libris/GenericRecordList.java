package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class GenericRecordList<RecordType extends Record> extends RecordList<RecordType> {

	private GenericDatabase<RecordType> database;
	public GenericRecordList(GenericDatabase<RecordType> db) {
		database = db;	
	}

	@Override
	public Iterator<RecordType> iterator() {
		Iterator<RecordType> iter;
		iter = database.getRecords().iterator();
		return iter;
	}

	@Override
	public RecordType getRecord(int id) throws InputException {
		return database.getRecord(id);
	}

	@Override
	public int size() {
		return database.getLastRecordId();
	}

}
