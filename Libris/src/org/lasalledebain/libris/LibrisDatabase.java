package org.lasalledebain.libris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.DatabaseNotIndexedException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.OutputException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.records.DelimitedTextRecordsReader;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.records.RecordsReader;
import org.lasalledebain.libris.records.XmlRecordsReader;
import org.lasalledebain.libris.ui.Dialogue;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisParameters;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.ui.LibrisUiGeneric;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;


public class LibrisDatabase implements LibrisXMLConstants, LibrisConstants, XmlExportable {

	public LibrisMetadata getMetadata() {
		return metadata;
	}
	private static final String COULD_NOT_OPEN_SCHEMA_FILE = "could not open schema file "; //$NON-NLS-1$
	private static Exception lastException;
	public static LibrisException openException;
	public static LibrisException rebuildException;
	public static Exception getLastException() {
		return lastException;
	}
	private LibrisFileManager fileMgr;
	private IndexManager indexMgr;
	private GroupManager groupMgr;
	LibrisXmlFactory xmlFactory;
	Schema schem;
	private RecordTemplate mainRecordTemplate;
	private DateFormat databaseDate = DateFormat.getDateInstance();
	protected LibrisMetadata metadata;
	private LibrisUi ui = null;
	DatabaseUsageMode usageMode = DatabaseUsageMode.USAGE_BATCH;
	private boolean isModified = false;
	private ModifiedRecordList modifiedRecords;
	private int branch;
	private LibrisRecordsFileManager recordsFile;
	private LibrisJournalFileManager journalFile;
	private Records databaseRecords;
	private boolean recordsAccessible;
	private LibrisParameters parameters;
	public LibrisParameters getParameters() {
		return parameters;
	}
	private Logger databaseLogger;
	public static Logger librisLogger = Logger.getLogger(LibrisDatabase.class.getName());;
	private DatabaseAttributes xmlAttributes;
	private int lastChildBranch;
	public static final String DATABASE_FILE = "DATABASE_FILE"; //$NON-NLS-1$
	
	public static LibrisDatabase rebuild(LibrisUi ui, File dbFile) {
		LibrisParameters params = new LibrisParameters(ui, dbFile);
		return rebuild(params);
		
		// TODO identify record num on import errors
	}
	
	public static LibrisDatabase rebuild(LibrisParameters params) {
		openException = rebuildException = null;
		LibrisDatabase db = null;
		params.setDoIndexing(true);
		try {
			db = new LibrisDatabase(params);
		} catch (LibrisException e) {
			openException = e;
			e.printStackTrace();
			LibrisDatabase.librisLog(Level.SEVERE, Messages.getString("LibrisDatabase.error_opening_database"), e); //$NON-NLS-1$
			return null;
		}
		LibrisFileManager oldmgr = db.fileMgr;
		if (!oldmgr.lock()) {
			LibrisDatabase.librisLog(Level.SEVERE, Messages.getString("LibrisDatabase.rebuild_failed_files_locked")); //$NON-NLS-1$
			db = null;
		} else try {
			db.buildIndexes();
			params.setDoIndexing(false);
			db = new LibrisDatabase(params);
		} catch (LibrisException e) {
			rebuildException = e;
			db.log(Level.SEVERE, "Error rebuilding database", e); //$NON-NLS-1$
			db = null;
		} finally {
			oldmgr.unlock();
		}
		return db;
	}

	public static LibrisDatabase open(LibrisParameters params) throws LibrisException {
			LibrisDatabase db = new LibrisDatabase(params);
			return db;
	}

	protected LibrisDatabase(LibrisParameters params) throws UserErrorException, LibrisException, DatabaseException {
		
		this.parameters = params;
		boolean readOnly = params.isReadOnly();
		boolean doIndexing = params.isDoIndexing();
		File databaseFile = params.getDatabaseFile();
		File auxiliaryDirectory = params.getAuxiliaryDirectory();
		databaseLogger = Logger.getLogger(LibrisDatabase.class.getName());
		LibrisUiGeneric.setLoggingLevel(databaseLogger);
		this.ui = params.getUi();
		open(databaseFile, auxiliaryDirectory, readOnly, doIndexing);
	}

	private void open(File databaseFile, File auxiliaryDirectory,
			boolean readOnly, boolean doIndexing) throws DatabaseException,
			UserErrorException, InputException, DatabaseNotIndexedException,
			LibrisException {
		fileMgr = new LibrisFileManager(databaseFile, auxiliaryDirectory);
		indexMgr = new IndexManager(this, metadata, fileMgr);
		groupMgr = new GroupManager(this);
		String schemaLocation = loadSchema();
		metadata.setSchemaInline(null == schemaLocation);
		if (!readOnly) {
				modifiedRecords = new ModifiedRecordList(this);			
		}
		if (!doIndexing) {
			if (!isIndexed()) {
				throw new DatabaseNotIndexedException();
			} else {
				setRecordsAccessible(true);
				recordsFile = getRecordsFileMgr();
				openRecords();
				databaseOpened();		
			}
		} else {
			setRecordsAccessible(false);		
		}
	}

	private void databaseOpened() throws DatabaseException {
		ui.databaseOpened(this);
	}
	/**
	 * @return path to the schema file. null if the schema is included in the database file
	 * @throws DatabaseException
	 * @throws UserErrorException
	 * @throws InputException 
	 */
	private String loadSchema() throws DatabaseException, UserErrorException, InputException {
		String schemaLocation = null;
		try {
			FileAccessManager databaseFileMgr = fileMgr.getDatabaseFileMgr();
			FileInputStream fileIpStream = databaseFileMgr.getIpStream();
			String databaseFilePath = databaseFileMgr.getPath();
			ElementManager librisMgr = makeLibrisElementManager(fileIpStream, databaseFilePath);
			HashMap<String, String> dbElementAttrs = librisMgr.parseOpenTag();
			String dateString = dbElementAttrs.get(XML_DATABASE_DATE_ATTR);
			if ((null == dateString) || dateString.isEmpty()) {
				databaseDate.setCalendar(Calendar.getInstance());
			} else {
				try {
					databaseDate.parse(dateString);
				} catch (ParseException e) {
					//TODO throw new SchemaException("illegal date format: "+dateString, e);
				}
			}
			branch = DatabaseAttributes.parseBranchString(dbElementAttrs.get(XML_DATABASE_BRANCH_ATTR));

			String nextElement = librisMgr.getNextId();
			ElementManager metadataMgr;
			if (XML_METADATA_TAG.equals(nextElement)) {
				metadataMgr = librisMgr.nextElement();
			} else {
				schemaLocation = dbElementAttrs.get(XML_SCHEMA_LOCATION_ATTR);
				metadataMgr = makeMetadataMgr(dbElementAttrs.get(XML_SCHEMA_NAME_ATTR), schemaLocation);
			}
			if (null == metadataMgr) {
				throw new UserErrorException("could not open schema file"); //$NON-NLS-1$
			}
			this.xmlAttributes = new DatabaseAttributes(this, dbElementAttrs);
			this.metadata = new LibrisMetadata(this);
			metadata.readMetadata(metadataMgr);
			librisMgr.closeFile();
			databaseFileMgr.releaseIpStream(fileIpStream);
		} catch (FactoryConfigurationError e) {	
			throw new DatabaseException("Error reading schema"); //$NON-NLS-1$
		} catch (XmlException e) {	
			throw new DatabaseException("Error reading schema", e); //$NON-NLS-1$
		} catch (IOException e) {	
			throw new DatabaseException("Error closing schema file", e); //$NON-NLS-1$
			}
		return schemaLocation;
	}
	
	public void buildIndexes() throws LibrisException {
		fileMgr.createAuxFiles(true);
		close();
		open(parameters.getDatabaseFile(), parameters.getAuxiliaryDirectory(), false, true);
		loadSchema();
		final File databaseFile = fileMgr.getDatabaseFile();
		metadata.setSavedRecords(0);
		XmlRecordsReader.importXmlRecords(this, databaseFile);
		Records recs = getDatabaseRecords();
		indexMgr.buildIndexes(recs);
		saveMetadata();
		setRecordsAccessible(true);
		save();
		ui.alert("Database re-indexed and will now be closed"); //$NON-NLS-1$
		ui.close(true, true);
		close();
	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		toXml(outWriter, true, true);
	}
	
	public void createBranch(File childName) throws LibrisException {
		if (isModified()) {
			ui.alert(Messages.getString("LibrisDatabase.save_database_before_branch")); //$NON-NLS-1$
			return;
		} else if (getBranch() != DATABASE_ROOT_BRANCH_ID) {
			throw new UserErrorException("Branches can be created only from the master copy"); //$NON-NLS-1$
		}
		final File databaseFile = fileMgr.getDatabaseFile();
		int childBranch = lastChildBranch + 1;
		int originalBranch = getBranch();
		try {
			setBranch(childBranch);
			setLastChild(DATABASE_ROOT_BRANCH_ID);
			exportDatabaseXml(new FileOutputStream(childName), metadata.isSchemaInline(), true);
			setBranch(originalBranch);
			setLastChild(childBranch);			
			exportDatabaseXml(new FileOutputStream(fileMgr.getDatabaseFile()), metadata.isSchemaInline(), true);
		} catch (FileNotFoundException e) {
			setBranch(originalBranch);
			setLastChild(childBranch);			
			throw new OutputException("error exporting database to "+databaseFile.getPath(), e); //$NON-NLS-1$
		}
	}
	
	private void toXml(ElementWriter outWriter, boolean includeMetadata,
			boolean includeRecords) throws XmlException, LibrisException {
		outWriter.writeStartElement(XML_LIBRIS_TAG, getAttributes(), false);
		if (includeMetadata) {
			metadata.toXml(outWriter);
		}
		if (includeRecords) {
			LibrisAttributes recordsAttrs = new LibrisAttributes();
			recordsAttrs.setAttribute(XML_RECORDS_LASTID_ATTR, metadata.getLastRecordId().toString());
			outWriter.writeStartElement(XML_RECORDS_TAG, recordsAttrs, false);
			for (Record r: getRecordReader()) {
				r.toXml(outWriter);
			}
			outWriter.writeEndElement(); /* records */
		}
		outWriter.writeEndElement(); /* database */	
		outWriter.flush();
	}


	private void openRecords() throws LibrisException, DatabaseException {

		FileAccessManager propsMgr = fileMgr.getPropertiesFileMgr();
		synchronized (propsMgr) {
			try {
				FileInputStream ipFile = propsMgr.getIpStream();
				LibrisException exc = metadata.readProperties(this, ipFile);
				if (null != exc) {
					propsMgr.delete();
					throw new DatabaseException("Exception reading properties file"+propsMgr.getPath(), exc); //$NON-NLS-1$
				}
				propsMgr.releaseIpStream(ipFile);
			} catch (IOException e) {
				throw new DatabaseException("Exception reading properties file"+propsMgr.getPath(), e); //$NON-NLS-1$
			}
		}
		if (!metadata.isMetadataOkay()) {
			showRebuildDialogue(isIndexed()?
					Messages.getString("LibrisDatabase.unindexed_do_index"): //$NON-NLS-1$
						Messages.getString("LibrisDatabase.db_corrupt_reindex")); //$NON-NLS-1$
			// throw new DatabaseException("Error in metadata: last record ID"); //$NON-NLS-1$
		}
		indexMgr.setLastRecordId(metadata.getLastRecordId());
		getDatabaseRecords();
	}
	
	public void save()  {
		try {
			if (isIndexed()) {
				saveMetadata();
			}
			Records recs = getDatabaseRecords();
			recs.putRecords(modifiedRecords);
			try {
				recs.flush();
			} catch (IOException e) {
				throw new DatabaseException(e);
			}
			setModified(false);
		} catch (LibrisException e) {
			ui.alert("Exception while saving record", e); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	private void saveMetadata() {
		FileAccessManager propsMgr = fileMgr.getPropertiesFileMgr();
		synchronized (propsMgr) {
			try {
				FileOutputStream opFile = propsMgr.getOpStream();
				metadata.saveProperties(opFile);
			} catch (IOException e) {
				getUi().alert("Exception saving properties file"+propsMgr.getPath(), e); //$NON-NLS-1$
			} finally {
				try {
					propsMgr.releaseOpStream();
				} catch (IOException e) {}
			}
		}
	}
	/**
	 * @param force close without saving
	 * @return true if we are exiting, false if failed.
	 */
	public boolean close(boolean force) {
		try {
			recordsFile.flush();
		} catch (DatabaseException e) {
			ui.alert("Error closing database", e); //$NON-NLS-1$
		}
		if (null != ui) {
			ui.close(true, true);
			ui.databaseClosed();
		}
		destroy();
		return true;
	}

	public void closeDatabaseSource() {
		fileMgr.getDatabaseFileMgr().close();
	}
	
	private void destroy() {
		databaseRecords = null;
		metadata = null;
		schem = null;
		indexMgr = null;
		if (null != fileMgr) {
			fileMgr.close();
		}
		try {
			if (null != recordsFile) {
				recordsFile.close();
			}
		} catch (LibrisException e) {
			alert("Error closing database", e); //$NON-NLS-1$
		} catch (IOException e) {
			alert("Error closing database", e); //$NON-NLS-1$
		}
		fileMgr = null;
		databaseRecords = null;
	}
	/**
	 * 
	 * @return true if we are exiting, false if failed.
	 */
	public boolean close() {
		if ((null != ui) && isModified) {
			int choice = ui.confirmWithCancel(Messages.getString("LibrisDatabase.save_database_before_close")); //$NON-NLS-1$
			switch (choice) {
			case JOptionPane.YES_OPTION:
				save();
				return close(true);
			case JOptionPane.NO_OPTION:
				return close(true);
			case JOptionPane.CANCEL_OPTION:
				return false;
			default:
				return false;
			}
		} else {
			destroy();
			return true;
		}
	}

	public boolean isModified() {
		return isModified;
	}
	@Override
	public boolean equals(Object comparand) {
		try {
			LibrisDatabase otherDb = (LibrisDatabase) comparand;
			if (isModified || otherDb.isModified()) {
				log(Level.FINE, "one of the databases has unsaved changes"); //$NON-NLS-1$
				return false; 
			}
			if (!metadata.equals(otherDb.metadata)) {
				log(Level.FINE, "metadata do not match"); //$NON-NLS-1$
				return false;
			}
			if (!schem.equals(otherDb.schem)) {
				log(Level.FINE, "schema do not match"); //$NON-NLS-1$
				return false;
			}
			try {
				for (Record r: getDatabaseRecords()) {
					RecordId rid = r.getRecordId();
					Record otherRecord = otherDb.getRecord(rid);
					if (!r.equals(otherRecord)) {
						return false;
					}
				}
			} catch (LibrisException e) {
				log(Level.SEVERE, "error reading records", e); //$NON-NLS-1$
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			log(Level.WARNING, "incompatible type in "+getClass().getName()+".equals()"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
		ui.indicateModified(isModified);
	}
	public void quit() {
		close();
		System.exit(0);
		
	}

	/**
	 * @param sourceFileMgr
	 * @return
	 * @throws InputException 
	 * @throws FileNotFoundException
	 * @throws SchemaException
	 */
	public ElementManager makeLibrisElementManager(FileInputStream fileStream, String filePath) throws InputException {
		try {
			InputStreamReader xmlFileReader = new InputStreamReader(fileStream);
			fileStream.getChannel().position(0);
			String initialElementName = LibrisXMLConstants.XML_LIBRIS_TAG;
			Reader rdr = xmlFileReader;
			ElementManager mgr = makeLibrisElementManager(rdr, initialElementName, filePath);
			return mgr;
		} catch (IOException e) {
			String msg = "error opening "+filePath; //$NON-NLS-1$
			databaseLogger.log(Level.SEVERE, msg, e); //$NON-NLS-1$
			throw new InputException("error opening "+msg, e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @param initialElementName
	 * @param reader
	 * @return
	 * @throws InputException 
	 */
	public ElementManager makeLibrisElementManager(Reader reader, String initialElementName, String filePath) throws
			InputException {
		return getXmlFactory().makeLibrisElementManager(reader, 
				filePath, initialElementName, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
	}

	public ElementManager makeLibrisElementManager(File source) throws InputException, FileNotFoundException {
		FileInputStream elementSource = new FileInputStream(source);
		return makeLibrisElementManager(elementSource, source.getPath());
	}

	protected ElementManager makeMetadataMgr(String schemaName, String schemaLocation) throws InputException {
		InputStreamReader metadataReader = null;
		ElementManager metadataMgr = null;
		FileAccessManager schemaFileMgr = fileMgr.getSchemaAccessMgr();
		try {
			if (null == schemaFileMgr) {
				if (null == schemaLocation) {
					File schemaFile = new File(schemaName+FILENAME_XML_FILES_SUFFIX);
					try {
						if (fileMgr.setSchemaAccessMgr(schemaFile)) {
							schemaFile = new File(getUi().SelectSchemaFile(schemaName));
						}
						fileMgr.setSchemaAccessMgr(schemaFile);
					} catch (DatabaseException e) {
						throw new InputException(COULD_NOT_OPEN_SCHEMA_FILE+schemaFile.getPath(), e);
					}
				} else {
					URL metadataUrl = new URL(schemaLocation);
					if (metadataUrl.getProtocol().equalsIgnoreCase("file")) { //$NON-NLS-1$
						String metaPath = metadataUrl.getPath();
						File schemaFile = new File(metaPath);
						if (!schemaFile.isAbsolute()) {
							schemaFile = new File(fileMgr.getDatabaseDirectory(), metaPath);
						}
						fileMgr.setSchemaAccessMgr(schemaFile);
					}
				}
			}
			metadataReader = new InputStreamReader(schemaFileMgr.getIpStream());
			ElementManager librisMgr = makeLibrisElementManager(metadataReader, XML_LIBRIS_TAG, schemaFileMgr.getPath());
			librisMgr.parseOpenTag();
			metadataMgr = librisMgr.nextElement();
			return metadataMgr;
		} catch (FileNotFoundException e) {
			throw new InputException(COULD_NOT_OPEN_SCHEMA_FILE+schemaLocation, e);
		} catch (MalformedURLException e) {
			throw new InputException(COULD_NOT_OPEN_SCHEMA_FILE+schemaLocation, e);
		} catch (DatabaseException e) {
			throw new InputException(COULD_NOT_OPEN_SCHEMA_FILE+schemaLocation, e);
		}
	}

	public boolean checkIfdatabaseIndexed(String msg) throws LibrisException {
		boolean result = true;
		if (!isIndexed()) {
			result =  showRebuildDialogue(msg);
		}
		return result;
	}

	public boolean showRebuildDialogue(String msg) throws LibrisException {
		boolean result = true;
		int response = ui.confirm(msg);
		switch (response) {
		case Dialogue.YES_OPTION:
			buildIndexes();
			break;
		case Dialogue.NO_OPTION:
		case Dialogue.CANCEL_OPTION:
			result = false;
		}
		return result;
	}

	public void setSchema(Schema schem) {
		this.schem = schem;
	}
	public File getDatabaseFile() {
		return fileMgr.getDatabaseFile();
	}

	public void setDatabaseFile(String dbFileName) {
		fileMgr.setDatabaseFile(parameters.setDatabaseFilePath(dbFileName));
	}
	
	private void setRecordsAccessible(boolean accessible) {
		this.recordsAccessible = accessible;
	}

	private boolean isIndexed() {
		return indexMgr.isIndexed();
	}

	public DatabaseAttributes getAttributes() {
		return xmlAttributes;
	}

	public int getBranch() {
		return branch;
	}

	public String getBranchString() {
		return Integer.toString(branch);
	}

	public int getLastChild() {
		return lastChildBranch;
	}
	public String getLastChildString() {
		return Integer.toString(getLastChild());
	}

	public Layouts getLayouts() {
		return metadata.getLayouts();
	}
	public LibrisRecordsFileManager getRecordsFileMgr() throws LibrisException {
		if (null == recordsFile) {
			recordsFile = new LibrisRecordsFileManager(this, ui.isReadOnly(),getSchema(), fileMgr);
		}
		return recordsFile;
	}
	public LibrisJournalFileManager getJournalFileMgr() throws LibrisException {
		if (null == journalFile) {
			journalFile = LibrisJournalFileManager.createLibrisJournalFileManager(this, fileMgr.getJournalFileMgr());
		}
		return journalFile;
	}

	public synchronized LibrisXmlFactory getXmlFactory() {
		if (null == xmlFactory) {
			xmlFactory = new LibrisXmlFactory();
		}
		return xmlFactory;
	}
	public RecordsReader getRecordReader() throws LibrisException {
		if (!recordsAccessible) {
			throw new DatabaseNotIndexedException();
		}
		return getRecordsFileMgr();
	}
	/**
	 * Create Records manager if necessary and return it.
	 * @return
	 * @throws LibrisException
	 */
	public Records getDatabaseRecords() throws LibrisException {
		if (null == databaseRecords) {
			databaseRecords = new Records(this, fileMgr);
		}
		return databaseRecords;
	}
	public Schema getSchema() {
		return schem;
	}
	
	public void viewRecord(RecordId recordId) {
		if (DatabaseUsageMode.USAGE_CMDLINE == usageMode) {
			System.err.println("cmdline not implemented"); //$NON-NLS-1$
			// TODO implement cmdline display record
		} else {
			try {
				ui.displayRecord(recordId);
			} catch (LibrisException e) {
				ui.alert(Messages.getString("LibrisDatabase.error_display_record")+recordId, e); //$NON-NLS-1$
			}
		}
	}
	public synchronized Record newRecord() throws InputException {
		if (null == mainRecordTemplate) {
			mainRecordTemplate =  RecordTemplate.templateFactory(schem);
		}
		return mainRecordTemplate.makeRecord(true);
	}
	/**
	 * Get the record.  If the record has been modified but not written to the database file, use that.
	 * Otherwise read the record from the file.
	 * @param id ID of the record to retrieve
	 * @return record, or null if it cannot be found
	 * @throws LibrisException in case of error
	 */
	public Record getRecord(RecordId id) throws LibrisException {
		Record rec = modifiedRecords.getRecord(id);
		if (null == rec) {
			rec = recordsFile.getRecord(id);
		} 
		if (null == rec) {
			ui.alert("Cannot locate record "+id); //$NON-NLS-1$
		}
		return rec;
	}
	/**
	 * Save a record in the list of modified records
	 * @param rec Record to enter
	 * @throws LibrisException 
	 */
	public RecordId put(Record rec) throws LibrisException {
		RecordId id = rec.getRecordId();
		if (null == id) {
			id = newRecordId();
			rec.setRecordId(id);
			metadata.adjustModifiedRecords(1);
		}
		databaseLogger.log(Level.FINE, "LibrisDatabase.put "+rec.getRecordId()); //$NON-NLS-1$
		getJournalFileMgr().put(rec);
		indexMgr.setLastRecordId(id);
		modifiedRecords.addRecord(rec);
		setModified(true);
		metadata.adjustSavedRecords(1);
		return id;
	}
	
	public void saveRecords(Iterable <Record> recList) throws LibrisException {

		if (isIndexed()) {
			saveMetadata();
		}
		setModified(false);
		Records recs = getDatabaseRecords();
		RecordId id = null;
		for (Record rec: recList) {
			id = rec.getRecordId();
			if (null == id) {
				id = newRecordId();
				rec.setRecordId(id);
			}
			recs.putRecord(rec);
		}
		if (null != id) {
			indexMgr.setLastRecordId(id);
		}

		try {
			recs.flush();
		} catch (IOException e) {
			throw (new OutputException("Error saving records", e)); //$NON-NLS-1$
		}
	}

	private RecordId newRecordId() throws LibrisException {
		RecordId lastId = metadata.getLastRecordId();
		RecordId newId;
		newId = lastId.increment();
		metadata.setLastRecordId(newId);
		return newId;
	}
	
	public int getSavedRecordCount() {
		return metadata.getSavedRecords();
	}
	
	public int getModifiedRecordCount() {
		return metadata.getModifiedRecords();
	}
	public synchronized ModifiedRecordList getModifiedRecords() {
		return modifiedRecords;
	}
	
	public DatabaseUsageMode getUsageMode() {
		return usageMode;
	}
	
	public void setAuxiliaryFilesDir(File auxDir) {
		fileMgr.setAuxDirectory(auxDir);
	}
	public void setBranch(int branch) {
		this.branch = branch;
		LibrisAttributes attrs = getAttributes();
		attrs.setAttribute(XML_DATABASE_BRANCH_ATTR, branch);
	}
	public void setLastChild(int childBranch) {
		this.lastChildBranch = childBranch;
		LibrisAttributes attrs = getAttributes();
		attrs.setAttribute(XML_DATABASE_LASTCHILD_ATTR, childBranch);
	}
	public void alert(String msg, Exception e) {
		getUi().alert(msg, e);
	}
	public void alert(String msg) {
		getUi().alert(msg);
	}
	public LibrisUi getUi() {
		return ui;
	}
	public static void setLastException(Exception e) {
		lastException = e;
	}

	public boolean isReadOnly() {
		return ui.isReadOnly();
	}

	public void log(Level severity, String msg, Throwable e) {
		databaseLogger.log(severity, msg, e);
	}
	static void librisLog(Level severity, String msg, Throwable e) {
		librisLogger.log(severity, msg, e);
	}
	static void librisLog(Level severity, String msg) {
		librisLogger.log(severity, msg);
	}
	public void log(Level severity, String msg, Object param) {
		databaseLogger.log(severity, msg, param);
	}
	public void log(Level severity, String msg) {
		databaseLogger.log(severity, msg);
	}
	public Logger getDatabaseLogger() {
		return databaseLogger;
	}
	public static Record[] importDelimitedTextFile(LibrisDatabase db, 
			File dataFile, String fids[], char separatorChar)
			throws FileNotFoundException, LibrisException {
		DelimitedTextRecordsReader importer = new DelimitedTextRecordsReader(db, separatorChar);
		importer.setDatafile(dataFile);
		importer.fieldIds = fids;
		Record[] recs = importer.importRecordsToDatabase();
		return recs;
	}
	
	public void exportDatabaseXml(OutputStream destination, boolean includeSchema, boolean includeRecords) throws LibrisException {
		ElementWriter outWriter;
		try {
			outWriter = ElementWriter.eventWriterFactory(destination);
		} catch (XMLStreamException e) {
			throw new OutputException(Messages.getString("LibrisDatabase.exception_export_xml"), e); //$NON-NLS-1$
		}
		toXml(outWriter, includeSchema, includeRecords); 
	}

	public IndexManager getIndexes() {
		return indexMgr;
	}

	public GroupManager getGroupMgr() {
		return groupMgr;
	}

	public LibrisFileManager getFileMgr() {
		return fileMgr;
	}

}
