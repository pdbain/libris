package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.LibrisUi;

public abstract class GenericDatabase<RecordType extends Record> implements XMLElement {
	protected GroupManager<RecordType> groupMgr;
	protected LibrisFileManager fileMgr;
	protected IndexManager indexMgr;
	protected Repository documentRepository;
	protected final LibrisUi ui;
	protected ModifiedRecordList modifiedRecords;
	protected LibrisJournalFileManager<RecordType> journalFile;
	protected Records<RecordType> databaseRecords;
	protected LibrisMetadata metadata;
	protected boolean readOnly;


	public GenericDatabase(LibrisUi theUi, LibrisFileManager theFileManager) {
		ui = theUi;
		fileMgr = theFileManager;
	}

	public abstract Schema getSchema();

	public LibrisFileManager getFileMgr() {
		return fileMgr;
	}

	public abstract LibrisJournalFileManager<RecordType> getJournalFileMgr() throws LibrisException;
	
	public int getLastRecordId() {
		return metadata.getLastRecordId();
	}

	public LibrisRecordsFileManager<DatabaseRecord> getRecordsFileMgr() throws LibrisException {
		return indexMgr.getRecordsFileMgr();
	}

	public LibrisUi getUi() {
		return ui;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public abstract  RecordType newRecord() throws InputException;

	/**
	 * Get the record.  If the record has been modified but not written to the database file, use that.
	 * Otherwise read the record from the file.
	 * @param id ID of the record to retrieve
	 * @return record, or null if it cannot be found
	 * @throws InputException  in case of error
	 */
	public Record getRecord(int recId) throws InputException {
		Record rec = modifiedRecords.getRecord(recId);
		if (null == rec) {
			try {
				rec = indexMgr.getRecordsFileMgr().getRecord(recId);
			} catch (Exception e) {
				throw new InputException("Error getting record "+recId, e);
			}
		} 
		if (null == rec) {
			ui.alert("Cannot locate record "+recId); //$NON-NLS-1$
		}
		return rec;
	}

	public Record getRecord(String recordName) throws InputException {
		SortedKeyValueFileManager<KeyIntegerTuple> idx = indexMgr.getNamedRecordIndex();
		Record result = null;
		KeyIntegerTuple t = idx.getByName(recordName);
		if (null != t) {
			int id = t.getValue();
			result = getRecord(id);
		}
		return result;
	}

	public void alert(String msg, Exception e) {
		getUi().alert(msg, e);
	}
	public void alert(String msg) {
		getUi().alert(msg);
	}


}
