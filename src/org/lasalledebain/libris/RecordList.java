package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public abstract class RecordList<RecordType extends Record> implements Iterable<RecordType> { // TODO RecordList implement ListModel

	@Override
	public abstract Iterator<RecordType> iterator();

	public abstract RecordType getRecord(int id) throws InputException;

	public RecordType getFirstRecord() throws InputException {
		return iterator().next();
	}

	public abstract int size();

	public Stream<RecordType> asStream() {
		return StreamSupport.stream(spliterator(), false);
	}
	
	@Deprecated
	/* use generics */
	public RecordList<Record> ofRecords() throws LibrisException {
		return new RecordList<Record> () {
			Iterator<RecordType> recs = RecordList.this.iterator();

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
				return RecordList.this.getRecord(id);
			}

			@Override
			public int size() {
				return RecordList.this.size();
			}

		};
	}

}
