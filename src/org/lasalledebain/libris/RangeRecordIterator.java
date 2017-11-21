package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Vector;

import org.lasalledebain.libris.exception.InputException;

class RangeRecordIterator implements Iterator<Record> {
	Vector <Record> recordList;
	private LibrisDatabase database;
	private int limit;
	private int cursor;
	private Record nextRecord;
	public RangeRecordIterator(LibrisDatabase db, int startId, int endId) {
		database = db;
		cursor = startId;
		limit = endId;
		nextRecord = null;
	}
	@Override
	public boolean hasNext() {
		while (Objects.isNull(nextRecord) && (cursor <= limit)) {
			try {
				nextRecord = database.getRecord(cursor);
			} catch (InputException e) {
				throw new NoSuchElementException();
			}
			cursor += 1;
		}
		return !Objects.isNull(nextRecord);
	}
	@Override
	public Record next() {
		Record result;
		if (!Objects.isNull(nextRecord)) {
			result = nextRecord;
			nextRecord = null;
		} else {
			if (!hasNext()) {
				throw new NoSuchElementException();
			} else {
				result = nextRecord;
				nextRecord = null;
			}
		}
		return result;
	}

}
