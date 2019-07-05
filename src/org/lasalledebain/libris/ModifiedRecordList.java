package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.UserErrorException;

public class ModifiedRecordList<RecordType extends Record> extends RecordList<RecordType> {

	TreeMap<Integer, Record> modifiedRecords;
	private static EmptyRecordList emptyList = new EmptyRecordList();
	public static EmptyRecordList getEmptyList() {
		return emptyList;
	}

	public ModifiedRecordList() {
		modifiedRecords = new TreeMap<Integer, Record>();
	}

	@Override
	public Iterator<Record> iterator() {
		Iterator<Record> records = modifiedRecords.values().iterator();
		return records;
	}

	/**
	 * Add a record to the list.
	 * @param rec
	 * @return true if successful
	 * @throws UserErrorException 
	 */
	public boolean addRecord(Record rec) throws UserErrorException {
		modifiedRecords.put(rec.getRecordId(), rec);
		return true;
	}

	public void clear() {
		modifiedRecords.clear();
	}

	@Override
	public Record getRecord(int id) throws InputException {
		return modifiedRecords.get(id);
	}
}
