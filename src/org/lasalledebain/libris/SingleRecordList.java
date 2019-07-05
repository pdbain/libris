package org.lasalledebain.libris;

import java.util.Iterator;

public class SingleRecordList<RecordType extends Record> extends RecordList<RecordType> implements Iterator<Record> {
	Record rec;
	int position;
	public SingleRecordList(Record rec) {
		this.rec = rec;
	}

	public SingleRecordList() {
		position = 0;
	}

	public Iterator<Record> iterator() {

		return new SingleRecordList(rec);
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

	@Override
	public Record getRecord(int id) {
		if ((null != rec) && (rec.getRecordId() == id)) {
			return rec;
		} else {
			return null;
		}
	}

}
