package org.lasalledebain.libris;

import java.util.Collections;
import java.util.Iterator;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.UserErrorException;

class EmptyRecordList extends ModifiedRecordList {

	@Override
	public Iterator<Record> iterator() {
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
	public Record getRecord(int id) throws InputException {
		return null;
	}
	
}