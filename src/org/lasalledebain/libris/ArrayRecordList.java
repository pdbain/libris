package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

public class ArrayRecordList extends RecordList {

	private final int recList[];
	private final RecordList parentList;

	public ArrayRecordList(RecordList parentList, int[] recList) {
		this.parentList = parentList;
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

	@Override
	public Record getRecord(int id) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

}
