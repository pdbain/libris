package org.lasalledebain.libris.indexes;

import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.RecordDataException;

public abstract class LibrisRecordMap {

	int numKeys = 0;
	public long recordsElementPosition;
	void putRecordPosition(RecordId recId, long recordPosition) throws DatabaseException {
		putRecordPosition(recId.getRecordNumber(), recordPosition);
	}
	
	public abstract void flush() throws DatabaseException;
	public abstract void close() throws DatabaseException;
	public abstract void putRecordPosition(int recordIdNum, long data) throws DatabaseException;
	
	/**
	 * Get the file position of the start of a record element
	 * @param recordNum number of the record
	 * @return file position, -1 if the record is not found
	 * @throws DatabaseException 
	 */
	public abstract long getRecordPosition(int recordNum) throws DatabaseException;
	/**
	 * Get the file position of the start of a record element
	 * @param recId record ID
	 * @return file position, or 0 if the record is not found
	 * @throws RecordDataException
	 */
	public long get(RecordId recId) throws DatabaseException {
		return getRecordPosition(recId.getRecordNumber());
	}
	/**
	 * @return number of records in the index
	 * @throws DatabaseException
	 */
	public abstract int size() throws DatabaseException;
	
	/**
	 * Advice to set the prepare the datastructure to accept a given number of entries
	 * @param numRecords Desired number of entries
	 * @return false if the request cannot be fulfilled
	 */
	public boolean setSize(int numRecords) {
		return false;
	}

	static class IdAndPosition {
		int id;
		long position;
		/**
		 * @param id record numeric id
		 * @param position record file position
		 */
		public IdAndPosition(int id, long position) {
			setIdAndPosition(id, position);
		}
		/**
		 * @param id
		 * @param position
		 */
		public void setIdAndPosition(int id, long position) {
			this.id = id;
			this.position = position;
		}
		public int getId() {
			return id;
		}
		public long getPosition() {
			return position;
		}
		
	}
}
