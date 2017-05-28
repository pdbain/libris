package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.InputException;

public class ModifiedRecordList extends RecordList {

	TreeMap<Integer, Record> modifiedRecords;
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
	 */
	public boolean addRecord(Record rec) {
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
