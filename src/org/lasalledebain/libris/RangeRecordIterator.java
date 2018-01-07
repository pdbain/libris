package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.lasalledebain.libris.exception.InputException;

class RangeRecordIterable implements Iterable<Record> {
	private LibrisDatabase database;
	int limit;
	int base;
	public RangeRecordIterable(LibrisDatabase db, int startId, int endId) {
		database = db;
		base = startId + 1;
		limit = endId;
	}
	class RangeRecordIterator implements Iterator<Record> {
		private int cursor;
		private Record nextRecord;
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
		public Record next() {
			Record result;
			if (!hasNext()) {
				throw new NoSuchElementException();
			}		
			result = nextRecord;
			nextRecord = null;
			return result;
		}
	}

	@Override
	public Iterator<Record> iterator() {
		return new RangeRecordIterator();
	}
}
