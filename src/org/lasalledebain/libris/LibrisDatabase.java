package org.lasalledebain.libris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.OutputException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.AffiliateList;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.DelimitedTextRecordsReader;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.records.RecordsReader;
import org.lasalledebain.libris.records.XmlRecordsReader;
import org.lasalledebain.libris.ui.Layouts;
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


public class LibrisDatabase implements LibrisXMLConstants, LibrisConstants, XMLElement {
// TODO read records in order
	public LibrisMetadata getMetadata() {
		return metadata;
	}
	private static final String COULD_NOT_OPEN_SCHEMA_FILE = "could not open schema file "; //$NON-NLS-1$
	@Deprecated
	public LibrisException openException;
	public  LibrisException rebuildException;
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
	private LibrisJournalFileManager journalFile;
	private Records databaseRecords;
	public static Logger librisLogger = Logger.getLogger(LibrisDatabase.class.getName());;
	private DatabaseAttributes xmlAttributes;
	private boolean readOnly;
	boolean opened;
	public static final String DATABASE_FILE = "DATABASE_FILE"; //$NON-NLS-1$
	
	public LibrisDatabase(File databaseFile, File auxDir, LibrisUi ui, boolean readOnly) throws LibrisException  {	
		opened = false;
		fileMgr = new LibrisFileManager(databaseFile, auxDir);
		loadSchema();
		indexMgr = new IndexManager(this, metadata, fileMgr);
		this.ui = ui;
		this.readOnly = readOnly;
		if (!readOnly) {
			modifiedRecords = new ModifiedRecordList();			
		}
	}

	public boolean open() throws InputException, LibrisException, DatabaseException {
		if (opened) {
			throw new UserErrorException("Database already opened");
		}
		if (!fileMgr.lockDatabase()) {
			ui.alert("Database opened by another process");
			opened = false;
			return false;
		}
		groupMgr = new GroupManager(this);
		mainRecordTemplate = RecordTemplate.templateFactory(schem, new DatabaseRecordList(this));
		if (!isIndexed()) {
			return false;
		} else {
			indexMgr.open();
			setRecordsAccessible(true);
			openRecords();
			databaseOpened();		
		}
		LibrisUiGeneric.setLoggingLevel(librisLogger);
		if (!readOnly) {
			modifiedRecords = new ModifiedRecordList();			
		}
		opened = true;
		return true;
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
				indexMgr.flush();
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
			if (null != fileMgr) {
				fileMgr.unlockDatabase();
			}
			destroy();
			ui.databaseClosed();
			opened = false;
			return true;
		}
	}

	/**
	 * @param force close without saving
	 * @return true if we are exiting, false if failed.
	 */
	public boolean close(boolean force) {
		if (null != ui) {
			ui.close(true, true);
			ui.databaseClosed();
		}
		if (null != fileMgr) {
			fileMgr.unlockDatabase();
		}
		destroy();
		return true;
	}

	public void quit() {
		close();
		System.exit(0);
		
	}

	private void databaseOpened() throws DatabaseException {
		ui.databaseOpened(this);
	}
	/**
	 * @return path to the schema file. null if the schema is included in the database file
	 * @throws LibrisException 
	 */
	private void loadSchema() throws LibrisException {
		try {
			FileAccessManager databaseFileMgr = fileMgr.getDatabaseFileMgr();
			FileInputStream fileIpStream = databaseFileMgr.getIpStream();
			String databaseFilePath = databaseFileMgr.getPath();
			ElementManager librisMgr = makeLibrisElementManager(fileIpStream, databaseFilePath);
			fromXml(librisMgr);
			librisMgr.closeFile();
			databaseFileMgr.releaseIpStream(fileIpStream);
		} catch (FactoryConfigurationError e) {	
			throw new DatabaseException("Error reading schema"); //$NON-NLS-1$
		} catch (XmlException e) {	
			throw new DatabaseException("Error reading schema", e); //$NON-NLS-1$
		} catch (IOException e) {	
			throw new DatabaseException("Error closing schema file", e); //$NON-NLS-1$
		}
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		String schemaLocation;
		HashMap<String, String> dbElementAttrs = mgr.parseOpenTag();
		String dateString = dbElementAttrs.get(XML_DATABASE_DATE_ATTR);
		if ((null == dateString) || dateString.isEmpty()) {
			databaseDate.setCalendar(Calendar.getInstance());
		} else {
			try {
				databaseDate.parse(dateString);
			} catch (ParseException e) {
			// TODO	throw new InputException("illegal date format: "+dateString, e);
			}
		}
		
		String nextElement = mgr.getNextId();
		ElementManager metadataMgr;
		if (XML_METADATA_TAG.equals(nextElement)) {
			metadataMgr = mgr.nextElement();
		} else {
			schemaLocation = dbElementAttrs.get(XML_DATABASE_SCHEMA_LOCATION_ATTR);
			metadataMgr = makeMetadataMgr(dbElementAttrs.get(XML_DATABASE_SCHEMA_NAME_ATTR), schemaLocation);
			metadata.setSchemaInline(null == schemaLocation);
		}
		if (null == metadataMgr) {
			throw new UserErrorException("could not open schema file"); //$NON-NLS-1$
		}
		this.xmlAttributes = new DatabaseAttributes(this, dbElementAttrs);
		this.metadata = new LibrisMetadata(this);
		metadata.fromXml(metadataMgr);
	}

	void buildIndexes() throws LibrisException {
		if (opened) {
			throw new UserErrorException("Cannot index an open database");
		}
		fileMgr.createAuxFiles(true);
		loadSchema();
		mainRecordTemplate = RecordTemplate.templateFactory(schem, new DatabaseRecordList(this));
		final File databaseFile = fileMgr.getDatabaseFile();
		metadata.setSavedRecords(0);
		XmlRecordsReader.importXmlRecords(this, databaseFile);
		Records recs = getDatabaseRecords();
		indexMgr.open();
		indexMgr.buildIndexes(recs);
		save();
		close();
	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		toXml(outWriter, true, true, false);
	}
	
	public String getElementTag() {
		return getXmlTag();
	}
		public String getXmlTag() {
		return XML_LIBRIS_TAG;
	}
	private void toXml(ElementWriter outWriter, boolean includeMetadata,
			boolean includeRecords, boolean addInstanceInfo) throws XmlException, LibrisException {
		outWriter.writeStartElement(XML_LIBRIS_TAG, getAttributes(), false);
		if (includeMetadata) {
			metadata.toXml(outWriter, addInstanceInfo);
		}
		if (includeRecords) {
			LibrisAttributes recordsAttrs = new LibrisAttributes();
			recordsAttrs.setAttribute(XML_RECORDS_LASTID_ATTR, RecordId.toString(metadata.getLastRecordId()));
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

		FileAccessManager propsMgr = fileMgr.getAuxiliaryFileMgr(LibrisFileManager.PROPERTIES_FILENAME);
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
			throw new DatabaseException("Error in metadata");
		}
		getDatabaseRecords();
	}
	
	/**
	 * 
	 */
	private void saveMetadata() {
		FileAccessManager propsMgr = fileMgr.getAuxiliaryFileMgr(LibrisFileManager.PROPERTIES_FILENAME);
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
	public void closeDatabaseSource() {
		fileMgr.getDatabaseFileMgr().close();
	}
	
	private void destroy() {
		databaseRecords = null;
		metadata = null;
		schem = null;
		if (null != indexMgr) {
			try {
				indexMgr.close();
			} catch (InputException | DatabaseException | IOException e) {
				log(Level.SEVERE, "error destroying database", e);
			}
		}
		indexMgr = null;
		if (null != fileMgr) {
			fileMgr.close();
		}
		fileMgr = null;
		databaseRecords = null;
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
					int rid = r.getRecordId();
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
			librisLogger.log(Level.SEVERE, msg, e); //$NON-NLS-1$
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
						if (!fileMgr.setSchemaAccessMgr(schemaFile)) {
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

	public void setSchema(Schema schem) {
		this.schem = schem;
	}
	public File getDatabaseFile() {
		return fileMgr.getDatabaseFile();
	}

	public void setDatabaseFile(String dbFileName) {
		fileMgr.setDatabaseFile(dbFileName);
	}
	
	private void setRecordsAccessible(boolean accessible) {
	}

	public boolean isIndexed() {
		return indexMgr.isIndexed();
	}

	public DatabaseAttributes getAttributes() {
		return xmlAttributes;
	}

	public Layouts getLayouts() {
		return metadata.getLayouts();
	}
	public LibrisRecordsFileManager getRecordsFileMgr() throws LibrisException {
		return indexMgr.getRecordsFileMgr();
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
	
	public void viewRecord(int recordId) {
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
		return mainRecordTemplate.makeRecord(true);
	}
	
	public int getLastRecordId() {
		return metadata.getLastRecordId();
	}
	/**
	 * Get the record.  If the record has been modified but not written to the database file, use that.
	 * Otherwise read the record from the file.
	 * @param id ID of the record to retrieve
	 * @return record, or null if it cannot be found
	 * @throws InputException  in case of error
	 */
	public Record getRecord(int recId) throws InputException {
		if (!opened) {
			throw new InputException("Database is closed");
		}
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
	
	/**
	 * Save a record in the list of modified records
	 * @param rec Record to enter
	 * @throws LibrisException 
	 */
	public int put(Record rec) throws LibrisException {
		int id = rec.getRecordId();
		if (RecordId.isNull(id)) {
			id = newRecordId();
			rec.setRecordId(id);
			metadata.adjustModifiedRecords(1);
		} else {
			if (isRecordReadOnly(id)) {
				librisLogger.log(Level.FINE, "LibrisDatabase.put "+rec.getRecordId() + "failed because read-only"); //$NON-NLS-1$
				ui.alert(DATABASE_OR_RECORD_ARE_READ_ONLY);
				return 0;
			}
			metadata.setLastRecordId(id);
		}
		librisLogger.log(Level.FINE, "LibrisDatabase.put "+rec.getRecordId()); //$NON-NLS-1$
		getJournalFileMgr().put(rec);
		String recordName = rec.getName();
		if ((null != recordName) && !recordName.isEmpty()) {
			SortedKeyValueFileManager<KeyIntegerTuple> nri = getNamedRecordIndex();
			KeyIntegerTuple query = nri.getByName(recordName);
			if (null != query) {
				if (query.getValue() != id) {
					throw new UserErrorException("Duplicate record name "+recordName);
				}
			} else {
				nri.addElement(new KeyIntegerTuple(recordName, id));
			}
		}
		for (int g = 0; g < schem.getNumGroups(); ++g) {
			int[] affiliations = rec.getAffiliates(g);
			if (affiliations.length != 0) {
				if (affiliations[0] != RecordId.getNullId()) {
					indexMgr.addChild(g, affiliations[0], id);
				}
				for (int i = 1; i < affiliations.length; ++i) {
					indexMgr.addAffiliate(g, affiliations[i], id);
				}
			}
		}
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
		int id = RecordId.getNullId();
		int lastId = 0;
		for (Record rec: recList) {
			id = rec.getRecordId();
			if (RecordId.isNull(id)) {
				id = metadata.newRecordId();
				rec.setRecordId(id);
			}
			recs.putRecord(rec);
			lastId = Math.max(id, lastId);
		}
		if (!RecordId.isNull(id)) {
			metadata.setLastRecordId(lastId);
		}

		try {
			recs.flush();
		} catch (IOException e) {
			throw (new OutputException("Error saving records", e)); //$NON-NLS-1$
		}
	}

	public int newRecordId() {
		return metadata.newRecordId();
	}
	
	public int getSavedRecordCount() {
		return metadata.getSavedRecords();
	}
	
	public RecordList getRecords() {
		return new DatabaseRecordList(this);	
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
	
	public void alert(String msg, Exception e) {
		getUi().alert(msg, e);
	}
	public void alert(String msg) {
		getUi().alert(msg);
	}
	public LibrisUi getUi() {
		return ui;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	public boolean isRecordReadOnly(int recordId) {
		boolean result;
		if (readOnly) {
			result = true;
		} else {
			result = (recordId <= metadata.getRecordIdBase());
		}
		return result;
	}

// FIXME clear modified marker when saving
	public static void log(Level severity, String msg, Throwable e) {
		librisLogger.log(severity, msg, e);
	}
	public static void log(Level severity, String msg) {
		librisLogger.log(severity, msg);
	}
	public static void log(Level severity, String msg, Object param) {
		librisLogger.log(severity, msg, param);
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
	
	public void exportDatabaseXml(OutputStream destination, boolean includeSchema, boolean includeRecords, boolean addInstanceInfo) throws LibrisException {
		ElementWriter outWriter;
		
		try {
			outWriter = ElementWriter.eventWriterFactory(destination);
		} catch (XMLStreamException e) {
			throw new OutputException(Messages.getString("LibrisDatabase.exception_export_xml"), e); //$NON-NLS-1$
		}
		toXml(outWriter, includeSchema, includeRecords, addInstanceInfo); 
	}

	public IndexManager getIndexes() {
		return indexMgr;
	}

	public GroupManager getGroupMgr() {
		return groupMgr;
	}

	 public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return indexMgr.getNamedRecordIndex();
	}

	public LibrisFileManager getFileMgr() {
		return fileMgr;
	}
	public RecordTemplate getMainRecordTemplate() {
		return mainRecordTemplate;
	}

	public NamedRecordList getNamedRecords() {
		NamedRecordList l = new NamedRecordList(this);
		return l;
	}
	
	public Iterable<Record> getChildRecords(int parent, int groupNum, boolean allDescendents) {
		AffiliateList affList = indexMgr.getAffiliateList(groupNum);
		if (allDescendents) {
			return affList.getDescendents(parent, this.getRecords());
		} else {
			int[] result = affList.getChildren(parent);
			return new ArrayRecordIterator(getRecords(), result);
		}
	}
	
	public Iterable<Record> getAffiliateRecords(int parent, int groupNum) {
		AffiliateList affList = indexMgr.getAffiliateList(groupNum);
			int[] result = affList.getAffiliates(parent);
			return new ArrayRecordIterator(getRecords(), result);
	}
	
	public String getRecordName(int recordNum) throws InputException {
		Record rec = getRecord(recordNum);
		return (null == rec) ? null: rec.getName();
	}

	public String getTitle() {
		return null;
	}

	public String getId() {
		return null;
	}

	public int getFieldNum() {
		return 0;
	}
}


