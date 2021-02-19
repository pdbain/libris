package org.lasalledebain.libris.indexes;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.RecordDataException;
import org.lasalledebain.libris.util.Reporter;

public abstract class LibrisRecordMap {

	int numKeys = 0;
	public long recordsElementPosition;
	
	public abstract void flush() throws DatabaseException;
	public abstract void close() throws DatabaseException;
	public abstract void putRecordPosition(int recordIdNum, long data) throws DatabaseException;
	public abstract void generateReport(Reporter rpt);

	
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
	public long get(int recId) throws DatabaseException {
		return getRecordPosition(recId);
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
	public abstract boolean setSize(int numRecords);
}
