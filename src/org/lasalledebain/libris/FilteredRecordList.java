package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.search.RecordFilter;

public class FilteredRecordList<RecordType extends Record> extends RecordList<RecordType> {

	RecordList<RecordType> recordSource;
	RecordFilter filter;

	public FilteredRecordList(RecordList<RecordType> src, RecordFilter f) {
		recordSource = src;
		filter = f;
	}

	@Override
	public Iterator<RecordType> iterator() {
		Iterator<RecordType> iter = recordSource.iterator();
		return new RecordIterator(iter);
	}

	@Override
	public RecordType getRecord(int id) throws InputException {
		return recordSource.getRecord(id);
	}

	class RecordIterator implements Iterator<RecordType> {

		private RecordType savedRec;
		protected Iterator<RecordType> recordSource;

		protected RecordIterator(Iterator<RecordType> src) {
			savedRec = null;
			recordSource = src;
		}

		@Override
		public boolean hasNext() {
			try {
				while ((null == savedRec) && recordSource.hasNext()) {
					RecordType temp = recordSource.next();
					if (filter.matches(temp)) {
						savedRec = temp;
					}
				}
			} catch (InputException e) {
				throw new DatabaseError("Error reading record", e);
			}
			return (null != savedRec);
		}

		@Override
		public RecordType next() {
			if (hasNext()) {
				RecordType temp = savedRec;
				savedRec = null;
				return temp;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			return;
		}		
	}
}
