package org.lasalledebain.libris.indexes;

import java.io.File;
import java.io.IOException;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;

public class RecordPositions {
	
	/**
	 * @param recordMap backing storage for the index
	 */
	public RecordPositions(LibrisRecordMap recordMap) {
		this.recordMap = recordMap;
	}

	public RecordPositions(FileAccessManager positionFile, boolean readOnly) throws DatabaseException {
		if (!positionFile.exists()) {
			try {
				positionFile.createNewFile();
			} catch (IOException e) {
				throw new DatabaseException("Cannot create position file "+positionFile.getPath(), e);
			}
		}
		recordMap = new FileRecordMap(positionFile, readOnly);
	}

	private LibrisRecordMap recordMap;
	/**
	 * @param recId record ID
	 * @return position of the start of the record's header, or 0 if not found
	 * @throws DatabaseException
	 */
	public long getRecordFilePosition(RecordId recId) throws DatabaseException  {
		final long filePos = recordMap.get(recId);
		return filePos;
	}

	public void setRecordFilePosition(RecordId recordId, long pos) throws DatabaseException {
		recordMap.putRecordPosition(recordId.getRecordNumber(), pos);
	}
	
	public void close() throws DatabaseException {
		recordMap.close();
	}

	public void flush() throws DatabaseException {
		recordMap.flush();
	}
}
