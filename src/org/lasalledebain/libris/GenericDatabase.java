package org.lasalledebain.libris;

import java.util.Objects;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.indexes.IndexConfiguration;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.RecordPositions;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public abstract class GenericDatabase<RecordType extends Record> implements XMLElement {
	protected GroupManager<RecordType> groupMgr;
	protected IndexManager<RecordType> indexMgr;
	protected final LibrisUi ui;
	protected ModifiedRecordList<RecordType> modifiedRecords;
	protected LibrisJournalFileManager<RecordType> journalFile;
	protected Records<RecordType> databaseRecords;
	protected boolean readOnly;
	protected boolean isModified;
	protected final LibrisFileManager fileMgr;

	public GenericDatabase(LibrisUi theUi, LibrisFileManager theFileManager) {
		ui = theUi;
		fileMgr = theFileManager;
	}

	protected void buildIndexes(IndexConfiguration config, Records<RecordType> recs, ElementManager recordsMgr)
			throws LibrisException {
		recs.fromXml(recordsMgr);
		indexMgr.buildIndexes(config, recs);
	}

	public abstract Schema getSchema();

	public LibrisFileManager getFileMgr() {
		return fileMgr;
	}

	public Records<RecordType> getDatabaseRecords() {
		return databaseRecords;
	}

	protected Records<RecordType> makeDatabaseRecords() throws LibrisException {
		if (null == databaseRecords) {
			databaseRecords = new Records<RecordType>(this, fileMgr);
		}
		return databaseRecords;
	}
	
	public synchronized ModifiedRecordList<RecordType> getModifiedRecords() {
		return modifiedRecords;
	}

	public RecordPositions getRecordPositions() throws DatabaseException {
		return indexMgr.getRecordPositions();
	}
	
	public abstract LibrisJournalFileManager<RecordType> getJournalFileMgr() throws LibrisException;
	
	public int getLastRecordId() {
		return getMetadata().lastRecordId;
	}
	
	public abstract GenericDatabaseMetadata getMetadata();

	public LibrisRecordsFileManager<RecordType> getRecordsFileMgr() throws LibrisException {
		return indexMgr.getRecordsFileMgr();
	}

	public Iterable<RecordType> getRecordReader() throws LibrisException {
		return getRecordsFileMgr();
	}
	
	public LibrisUi getUi() {
		return ui;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isIndexed() {
		return indexMgr.isIndexed();
	}

	public boolean isRecordReadOnly(int recordId) {
		
		return isReadOnly();
	}
	public abstract  RecordType newRecord() throws InputException;

	public abstract int putRecord(RecordType rec) throws LibrisException;
	
	/**
	 * Get the record.  If the record has been modified but not written to the database file, use that.
	 * Otherwise read the record from the file.
	 * @param id ID of the record to retrieve
	 * @return record, or null if it cannot be found
	 * @throws InputException  in case of error
	 */
	public RecordType getRecord(int recId) throws InputException {
		RecordType rec = modifiedRecords.getRecord(recId);
		if (null == rec) {
			try {
				LibrisRecordsFileManager<RecordType> recFileMgr = indexMgr.getRecordsFileMgr();
				rec = recFileMgr.getRecord(recId);
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

	public String getRecordName(int recordNum) throws InputException {
		Record rec = getRecord(recordNum);
		return (null == rec) ? null: rec.getName();
	}
	
	public NamedRecordList<RecordType> getNamedRecords() {
		NamedRecordList<RecordType> l = new NamedRecordList<RecordType>(this);
		return l;
	}

	public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return indexMgr.getNamedRecordIndex();
	}

	public void alert(String msg, Exception e) {
		getUi().alert(msg, e);
	}
	public void alert(String msg) {
		getUi().alert(msg);
	}

	public abstract RecordFactory<RecordType> getRecordFactory();

	protected int genericPutRecord(LibrisMetadata metaData, RecordType rec)
			throws DatabaseException, LibrisException, InputException, UserErrorException {
				int id = rec.getRecordId();
				if (RecordId.isNull(id)) {
					id = newRecordId();
					rec.setRecordId(id);
					metaData.adjustModifiedRecords(1);
				} else {
					if (isRecordReadOnly(id)) {
						throw new DatabaseException("Record "+id+" is read-only");
					}
					metaData.setLastRecordId(id);
				}
				getJournalFileMgr().put(rec);
				String recordName = rec.getName();
				if (Objects.nonNull(recordName)) {
					indexMgr.addNamedRecord(id, recordName);
				}
				indexMgr.addRecordKeywords(rec);
				
				modifiedRecords.addRecord(rec);
				setModified(true);
				metaData.adjustSavedRecords(1);
				return id;
			}

	public int newRecordId() {
		return getMetadata().newRecordId();
	}

	public int getSavedRecordCount() {
		return getMetadata().getSavedRecords();
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}

}
