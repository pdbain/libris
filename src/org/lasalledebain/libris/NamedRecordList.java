package org.lasalledebain.libris;

import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;

public class NamedRecordList extends RecordList implements RecordIdNameMapper {

	SortedKeyValueFileManager<KeyIntegerTuple> namedRecordIndex;
	private LibrisDatabase database;

	public NamedRecordList(LibrisDatabase db) {
		database = db;
		namedRecordIndex = database.getNamedRecordIndex();
	}

	@Override
	public String getName(int recId) throws InputException {
		return database.getRecordName(recId);
	}
	
	@Override
	public int getId(String recName) throws InputException {
		 KeyIntegerTuple r = namedRecordIndex.getByName(recName);
		return (null == r) ? RecordId.getNullId(): r.getValue();
	}
	
	@Override
	public Iterator<Record> iterator() {
		return new NamedRecordIterator();
	}

	class NamedRecordIterator implements Iterator<Record>{
		private Iterator<KeyIntegerTuple> tupleIterator;

		NamedRecordIterator() {
			tupleIterator = namedRecordIndex.iterator();
		}

		@Override
		public boolean hasNext() {
			return tupleIterator.hasNext();
		}

		@Override
		public Record next() {
			KeyIntegerTuple tup = tupleIterator.next();
			int numericId = tup.getValue();
			try {
				return database.getRecord(numericId);
			} catch (InputException e) {
				throw new DatabaseError("Error fetching record "+tup.getKey()+" ("+numericId+")", e);
			}
		}

		@Override
		public void remove() {
			return;
		}
		
	}

	@Override
	public Record getRecord(int id) throws InputException {
		return database.getRecord(id);
	}

	public void remove(String oldName) throws InputException {
		namedRecordIndex.removeElement(oldName);
	}

	public void put(String newName, int recordId) throws InputException {
		namedRecordIndex.addElement(new KeyIntegerTuple(newName, recordId));
	}
}
