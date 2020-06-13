package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.UserErrorException;

public class ModifiedRecordList<RecordType extends Record> extends RecordList<RecordType> {

	TreeMap<Integer, RecordType> modifiedRecords;
	public ModifiedRecordList() {
		modifiedRecords = new TreeMap<Integer, RecordType>();
	}

	@Override
	public Iterator<RecordType> iterator() {
		Iterator<RecordType> records = modifiedRecords.values().iterator();
		return records;
	}

	/**
	 * Add a record to the list.
	 * @param rec
	 * @return true if successful
	 * @throws UserErrorException 
	 */
	public boolean addRecord(RecordType rec) throws UserErrorException {
		modifiedRecords.put(rec.getRecordId(), rec);
		return true;
	}

	public void clear() {
		modifiedRecords.clear();
	}

	@Override
	public RecordType getRecord(int id) throws InputException {
		return modifiedRecords.get(id);
	}

	@Override
	public int size() {
		return modifiedRecords.size();
	}

}
