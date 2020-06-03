package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

public class ArrayRecordIterator<RecordType extends Record> implements Iterable<RecordType> {

	private final int recList[];
	private final RecordList<RecordType> parentList;

	public ArrayRecordIterator(RecordList<RecordType> masterList, int[] recList) {
		this.parentList = masterList;
		this.recList = recList;
	}

	@Override
	public Iterator<RecordType> iterator() {
		return new Iterator<RecordType>() {
			int index = 0;
			@Override
			public boolean hasNext() {
				return index < recList.length;
			}

			@Override
			public RecordType next() {
				RecordType result = null;
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
