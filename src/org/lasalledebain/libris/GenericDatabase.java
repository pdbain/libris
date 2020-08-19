package org.lasalledebain.libris;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.indexes.DatabaseConfiguration;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.RecordPositions;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.search.KeywordFilter;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;

public abstract class GenericDatabase<RecordType extends Record> implements XMLElement {
	protected boolean dbOpen;
	protected GroupManager<RecordType> groupMgr;
	protected IndexManager<RecordType> indexMgr;
	protected final DatabaseUi<RecordType> ui;
	protected ModifiedRecordList<RecordType> modifiedRecords;
	/**
	 * XML representation of database records added
	 */
	protected LibrisJournalFileManager<RecordType> journalFileMgr;
	protected Records<RecordType> databaseRecords;
	protected boolean readOnly;
	protected boolean isModified;
	protected final FileManager fileMgr;
	final static LibrisXmlFactory xmlFactory = new LibrisXmlFactory();

	public GenericDatabase(DatabaseUi<RecordType> theUi, FileManager theFileManager) throws DatabaseException {
		ui = theUi;
		fileMgr = theFileManager;
		indexMgr = new IndexManager<RecordType>(this, fileMgr);
		modifiedRecords = readOnly? new EmptyRecordList<>(): new ModifiedRecordList<RecordType>();			
	}

	public void initialize() throws LibrisException {
		fileMgr.createAuxFiles(true);		
		DatabaseMetadata metatada = getMetadata();
		metatada.setLastRecordId(0);
		metatada.setSavedRecords(0);
		saveMetadata();
	}

	public void buildIndexes(DatabaseConfiguration config)
			throws LibrisException {
		Records<RecordType> myRecs = getDatabaseRecordsUnchecked();
		indexMgr.buildIndexes(config, myRecs);
		saveUnchecked();
	}

	public synchronized void openDatabase() throws LibrisException {
		if (dbOpen) {
			throw new DatabaseException("Database already open");
		}
		DatabaseMetadata metadata = getMetadata();
		FileAccessManager propsMgr = fileMgr.getAuxiliaryFileMgr(LibrisConstants.PROPERTIES_FILENAME);
		synchronized (propsMgr) {
			FileInputStream ipFile = null;
			try {
				ipFile = propsMgr.getIpStream();
				metadata.readProperties(ipFile);
			} catch (IOException | LibrisException e) {
				propsMgr.delete();
				throw new DatabaseException("Exception reading properties file"+propsMgr.getPath(), e); //$NON-NLS-1$
			} finally {
				try {
					propsMgr.releaseIpStream(ipFile);
				} catch (IOException e) { /* empty */ }					
			}
		}
		indexMgr.open(readOnly);
		if (!metadata.isMetadataOkay()) {
			throw new DatabaseException("Error in metadata");
		}
		getDatabaseRecordsUnchecked();
		dbOpen = true;
	}

	public boolean closeDatabase(boolean force) throws DatabaseException {

		if (!isOkayToClose(force)) {
			return false;
		} else {
			databaseRecords = null;
			if (null != indexMgr) {
				try {
					indexMgr.close();
				} catch (InputException | DatabaseException | IOException e) {
					LibrisDatabase.log(Level.SEVERE, "error destroying database", e);
				}
			}
			indexMgr = null;
			if (null != fileMgr) {
				fileMgr.close();
			}
			databaseRecords = null;
			dbOpen = false;
			return true;
		}
	}

	public boolean isOkayToClose(boolean force) throws DatabaseException {
		if (!force && !dbOpen) {
			throw new DatabaseException("Database not open");
		}
		return !isModified() || force;
	}

	public void save()  {
		assertDatabaseWritable("save database");
		saveUnchecked();
	}

	private void saveUnchecked() {
		try {
			if (isIndexed()) {
				saveMetadata();
			}
			Records<RecordType> recs = getDatabaseRecordsUnchecked();
			recs.putRecords(modifiedRecords);
			try {
				recs.flush();
				indexMgr.flush();
			} catch (IOException e) {
				throw new DatabaseException(e);
			}
			setModified(false);
		} catch (LibrisException e) {
			ui.alert("Exception while saving record", e); //$NON-NLS-1$
		}
	}

	void saveMetadata() {
		FileAccessManager propsMgr = fileMgr.getAuxiliaryFileMgr(LibrisConstants.PROPERTIES_FILENAME);
		synchronized (propsMgr) {
			try {
				FileOutputStream opFile = propsMgr.getOpStream();
				getMetadata().saveProperties(opFile);
			} catch (IOException e) {
				alert("Exception saving properties file"+propsMgr.getPath(), e); //$NON-NLS-1$
			} finally {
				try {
					propsMgr.releaseOpStream();
				} catch (IOException e) {}
			}
		}
	}

	public abstract Schema getSchema();

	public abstract DatabaseMetadata getMetadata();

	public DatabaseUi<RecordType> getUi() {
		return ui;
	}

	public static LibrisXmlFactory getXmlFactory() {
		return xmlFactory;
	}

	public abstract RecordFactory<RecordType> getRecordFactory();

	public FileManager getFileMgr() {
		return fileMgr;
	}

	public Records<RecordType> getDatabaseRecords() throws LibrisException {
		assertDatabaseOpen("get records");
		return getDatabaseRecordsUnchecked();
	}

	public Records<RecordType> getDatabaseRecordsUnchecked() throws LibrisException {
		if (null == databaseRecords) {
			databaseRecords = new Records<RecordType>(this, fileMgr);
		}
		return databaseRecords;
	}

	public NamedRecordList<RecordType> getNamedRecords() {
		assertDatabaseOpen("get named records");
		NamedRecordList<RecordType> l = new NamedRecordList<RecordType>(this);
		return l;
	}

	public FilteredRecordList<RecordType> makeKeywordFilteredRecordList(MATCH_TYPE matchType, boolean caseSensitive, 
			int fieldList[], Collection<String> searchTerms) throws UserErrorException, IOException {
		RecordList<RecordType> recList = new SignatureFilteredRecordList<RecordType>(this, searchTerms);
		KeywordFilter filter = new KeywordFilter(matchType, caseSensitive, fieldList, searchTerms);
		FilteredRecordList<RecordType> filteredList = new FilteredRecordList<RecordType>(recList, filter);
		return filteredList;
	}

	public FilteredRecordList<RecordType> makeKeywordFilteredRecordList(MATCH_TYPE matchType, boolean caseSensitive, 
			int fieldList[], String searchTerm) throws UserErrorException, IOException {
		Collection<String> searchTerms = Collections.singleton(searchTerm);
		RecordList<RecordType> recList = new SignatureFilteredRecordList<RecordType>(this, searchTerms);
		KeywordFilter filter = new KeywordFilter(matchType, caseSensitive, fieldList, searchTerms);
		FilteredRecordList<RecordType> filteredList = new FilteredRecordList<RecordType>(recList, filter);
		return filteredList;
	}

	public synchronized ModifiedRecordList<RecordType> getModifiedRecords() {
		return modifiedRecords;
	}

	public RecordPositions getRecordPositions() throws DatabaseException {
		return indexMgr.getRecordPositions();
	}

	public LibrisJournalFileManager<RecordType> getJournalFileMgr() throws LibrisException {
		if (null == journalFileMgr) {
			FileAccessManager journalFile = fileMgr.makeAuxiliaryFileAccessManager(LibrisConstants.JOURNAL_FILENAME);
			journalFileMgr = new LibrisJournalFileManager<RecordType>(this, journalFile, getRecordFactory());
		}
		return journalFileMgr;
	}

	public int getLastRecordId() {
		return getMetadata().lastRecordId;
	}

	public LibrisRecordsFileManager<RecordType> getRecordsFileMgr() throws LibrisException {
		return indexMgr.getRecordsFileMgr();
	}

	public Iterable<RecordType> getRecordReader() throws LibrisException {
		return getRecordsFileMgr();
	}

	public int newRecordId() {
		return getMetadata().newRecordId();
	}

	public abstract RecordType newRecord() throws InputException;

	public abstract RecordType newRecordUnchecked();

// TODO test if database writable
	public abstract int putRecord(RecordType rec) throws LibrisException;

	/**
	 * Get the record.  If the record has been modified but not written to the database file, use that.
	 * Otherwise read the record from the file.
	 * @param id ID of the record to retrieve
	 * @return record, or null if it cannot be found
	 * @throws InputException  in case of error
	 */
	public RecordType getRecord(int recId) throws InputException {
		assertDatabaseOpen("get record");
		return getRecordUnchecked(recId);
	}
	private RecordType getRecordUnchecked(int recId) throws InputException {

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

	public RecordType getRecord(String recordName) throws InputException {
		assertDatabaseOpen("get record");
		return getRecordUnchecked(recordName);
	}

	private RecordType getRecordUnchecked(String recordName) throws InputException {
		SortedKeyValueFileManager<KeyIntegerTuple> idx = indexMgr.getNamedRecordIndex();
		RecordType result = null;
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

	public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return indexMgr.getNamedRecordIndex();
	}

	protected int genericPutRecord(DatabaseMetadata metaData, RecordType rec)
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

	public int getSavedRecordCount() {
		return getMetadata().getSavedRecords();
	}

	public boolean isDatabaseReadOnly() {
		return readOnly;
	}

	public boolean isDatabaseOpen() {
		return dbOpen;
	}

	public boolean isIndexed() {
		return indexMgr.isIndexed();
	}

	public boolean isRecordReadOnly(int recordId) {

		return isDatabaseReadOnly();
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	public void alert(String msg, Exception e) {
		getUi().alert(msg, e);
	}

	public void alert(String msg) {
		getUi().alert(msg);
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	public void assertDatabaseWritable(String message) {
		Assertion.assertTrueError("Closed database: ", message, isDatabaseOpen());
		Assertion.assertTrueError("Read-only database: ", message, !isDatabaseReadOnly());
	}

	public void assertDatabaseOpen(String message) {
		Assertion.assertTrueError("Closed database: ", message, isDatabaseOpen());
	}
}
