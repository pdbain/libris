package org.lasalledebain.libris;

import java.util.Collections;
import java.util.Iterator;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.UserErrorException;

class EmptyRecordList<RecordType extends Record> extends ModifiedRecordList<RecordType> {

	@Override
	public Iterator<RecordType> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public boolean addRecord(Record rec) throws UserErrorException {
		throw new UserErrorException("Cannot add records to read-only database");
	}

	@Override
	public void clear() {
		return;
	}

	@Override
	public RecordType getRecord(int id) throws InputException {
		return null;
	}
	
}