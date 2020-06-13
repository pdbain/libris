package org.lasalledebain.libris;

import java.util.Iterator;

public class SingleRecordList<RecordType extends Record> extends RecordList<RecordType> implements Iterator<RecordType> {
	RecordType rec;
	int position;
	public SingleRecordList(RecordType rec) {
		this.rec = rec;
	}

	public SingleRecordList() {
		position = 0;
	}

	public Iterator<RecordType> iterator() {

		return new SingleRecordList<RecordType>(rec);
	}

	@Override
	public boolean hasNext() {
		return position == 0;
	}

	@Override
	public RecordType next() {
		position = 1;
		return rec;
	}

	@Override
	public void remove() {
		return;
	}

	@Override
	public RecordType getRecord(int id) {
		if ((null != rec) && (rec.getRecordId() == id)) {
			return rec;
		} else {
			return null;
		}
	}

	@Override
	public int size() {
		return 1;
	}

}
