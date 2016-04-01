package org.lasalledebain.libris;

import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.LibrisException;

public class ModifiedRecordList extends RecordList {

	TreeMap<RecordId, Record> modifiedRecords;
	public ModifiedRecordList(LibrisDatabase database) {
		super(database);
		modifiedRecords = new TreeMap<RecordId, Record>();
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
	@Override
	public Record getRecord(RecordId id) throws LibrisException {
		return modifiedRecords.get(id);
	}

	public void clear() {
		modifiedRecords.clear();
	}
}
