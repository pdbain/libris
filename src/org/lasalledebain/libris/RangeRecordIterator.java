package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.lasalledebain.libris.exception.InputException;

class RangeRecordIterable<RecordType extends Record> implements Iterable<RecordType> {
	private GenericDatabase<RecordType> database;
	int limit;
	int base;
	public RangeRecordIterable(GenericDatabase<RecordType> db, int startId, int endId) {
		database = db;
		base = startId + 1;
		limit = endId;
	}
	class RangeRecordIterator implements Iterator<RecordType> {
		private int cursor;
		private RecordType nextRecord;
		public RangeRecordIterator() {
			cursor = base;
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
		public RecordType next() {
			RecordType result;
			if (!hasNext()) {
				throw new NoSuchElementException();
			}		
			result = nextRecord;
			nextRecord = null;
			return result;
		}
	}

	@Override
	public Iterator<RecordType> iterator() {
		return new RangeRecordIterator();
	}
}
