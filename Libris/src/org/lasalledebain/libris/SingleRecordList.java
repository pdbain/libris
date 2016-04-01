package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.LibrisException;

public class SingleRecordList extends RecordList implements Iterator<Record> {
	Record rec;
	int position;
	public SingleRecordList(LibrisDatabase database, Record rec) {
		this(database);
		this.rec = rec;
	}

	public SingleRecordList(LibrisDatabase database) {
		super(database);
		position = 0;
	}

	@Override
	public Record getRecord(RecordId id) throws LibrisException {
		if ((null != rec) && (rec.getRecordId().equals(id))) {
			return rec;
		} else {
			return null;
		}
	}

	@Override
	public Iterator<Record> iterator() {
		
		return new SingleRecordList(database, rec);
	}

	@Override
	public boolean hasNext() {
		return 1 == position;
	}

	@Override
	public Record next() {
		position = 1;
		return rec;
	}

	@Override
	public void remove() {
		return;
	}

}
