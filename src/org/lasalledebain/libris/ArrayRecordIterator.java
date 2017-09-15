package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

public class ArrayRecordIterator implements Iterable<Record> {

	private final int recList[];
	private final RecordList parentList;

	public ArrayRecordIterator(RecordList masterList, int[] recList) {
		this.parentList = masterList;
		this.recList = recList;
	}

	@Override
	public Iterator<Record> iterator() {
		return new Iterator<Record>() {
			int index = 0;
			@Override
			public boolean hasNext() {
				return index < recList.length;
			}

			@Override
			public Record next() {
				Record result = null;
				try {
					result = parentList.getRecord(recList[index]);
					++index;
				} catch (InputException e) {
					throw new DatabaseError("Error getting record "+recList[index], e);
				}
				return result;
			}
		};
	}
}
