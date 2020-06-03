package org.lasalledebain.libris.indexes;

import java.io.IOException;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.util.Reporter;

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
	 * @throws DatabaseException
	 */
	public long getRecordFilePosition(int recId) throws DatabaseException {
		final long filePos = recordMap.get(recId);
		return filePos;
	}

	public void setRecordFilePosition(int recordId, long pos) throws DatabaseException {
		recordMap.putRecordPosition(recordId, pos);
	}
	
	public void close() throws DatabaseException {
		recordMap.close();
	}

	public void flush() throws DatabaseException {
		recordMap.flush();
	}
	public void generateReport(Reporter rpt) {
		recordMap.generateReport(rpt);
	}
}
