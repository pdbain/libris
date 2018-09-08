package org.lasalledebain.libris;

import static org.lasalledebain.libris.exception.Assertion.assertEquals;
import static org.lasalledebain.libris.exception.Assertion.assertNotNull;
import static org.lasalledebain.libris.exception.Assertion.assertTrue;

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
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.DatabaseNotIndexedException;
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
import org.lasalledebain.libris.records.XmlRecordsReader;
import org.lasalledebain.libris.search.KeywordFilter;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

public class LibrisDatabase implements LibrisXMLConstants, LibrisConstants, XMLElement {
	public LibrisMetadata getMetadata() {
		return metadata;
	}
	public  LibrisException rebuildException;
	private LibrisFileManager fileMgr;
	private IndexManager indexMgr;
	private GroupManager groupMgr;
	final static LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
	private Schema mySchema;
	private RecordTemplate mainRecordTemplate;
	protected LibrisMetadata metadata;
	private LibrisUi ui;
	DatabaseUsageMode usageMode = DatabaseUsageMode.USAGE_BATCH;
	private boolean isModified;
	private ModifiedRecordList modifiedRecords;
	private LibrisJournalFileManager journalFile;
	private Records databaseRecords;
	public static Logger librisLogger = Logger.getLogger(LibrisDatabase.class.getName());
	protected DatabaseAttributes xmlAttributes;
	private boolean readOnly;
	private Date databaseDate;
	public static final String DATABASE_FILE = "DATABASE_FILE"; //$NON-NLS-1$

	public LibrisDatabase(LibrisDatabaseParameter parameterObject) throws LibrisException  {
		fileMgr = new LibrisFileManager(parameterObject.databaseFile, parameterObject.auxDir);
		indexMgr = new IndexManager(this, metadata, fileMgr);
		ui = parameterObject.ui;
		isModified = false;
		readOnly = parameterObject.readOnly;
		mySchema = null;
		if (!parameterObject.readOnly) {
			modifiedRecords = new ModifiedRecordList();			
		}
		metadata = new XmlMetadata(this);
		groupMgr = new GroupManager(this);
	}

	public void openDatabase(Schema theSchema) throws DatabaseNotIndexedException, DatabaseException, LibrisException {
		if (Objects.nonNull(theSchema)) {
			loadDatabaseInfo(false);
			mySchema = theSchema;
			openDatabaseImpl();
		} else {
			openDatabase();
		}
	}
	public void openDatabase() throws DatabaseNotIndexedException, DatabaseException, LibrisException {
		loadDatabaseInfo(true);
		openDatabaseImpl();
	}

	private void openDatabaseImpl() throws DatabaseNotIndexedException, DatabaseException, LibrisException {
		if (isDatabaseReserved()) { 
			throw new DatabaseException("Database already opened");
		}
		mainRecordTemplate = RecordTemplate.templateFactory(mySchema, new DatabaseRecordList(this));
		if (!readOnly) {
			modifiedRecords = new ModifiedRecordList();			
		}
		if (!isIndexed()) {
			throw new DatabaseNotIndexedException();
		} else {
			indexMgr.open();
			setRecordsAccessible(true);
			FileAccessManager propsMgr = fileMgr.getAuxiliaryFileMgr(LibrisFileManager.PROPERTIES_FILENAME);
			synchronized (propsMgr) {
				try {
					FileInputStream ipFile = propsMgr.getIpStream();
					LibrisException exc = metadata.readProperties(ipFile);
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
	}

	public boolean reserveDatabase() {
		return fileMgr.reserveDatabase();
	}

	public void freeDatabase() {
		fileMgr.freeDatabase();
	}
	
	public boolean isDatabaseReserved() {
		return fileMgr.isDatabaseReserved();
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
	 * @param force close without saving
	 * @return true if database is not modified or force is true.
	 */
	public boolean closeDatabase(boolean force) {
		if (isModified() && !force) {
			return false;
		} else {
		databaseRecords = null;
		metadata = null;
		mySchema = null;
		if (null != indexMgr) {
			try {
				indexMgr.close();
			} catch (InputException | DatabaseException | IOException e) {
				log(Level.SEVERE, "error destroying database", e);
			}
		}
		indexMgr = null;
		if (null != fileMgr) {
			fileMgr.freeDatabase();
			fileMgr.close();
		}
		fileMgr = null;
		databaseRecords = null;
		return true;
		}
	}

	private void loadDatabaseInfo(boolean doLoadMetadata) throws LibrisException {
		try {
			FileAccessManager databaseFileMgr = fileMgr.getDatabaseFileMgr();
			FileInputStream fileIpStream = databaseFileMgr.getIpStream();
			String databaseFilePath = databaseFileMgr.getPath();
			ElementManager librisMgr = makeLibrisElementManager(fileIpStream, databaseFilePath);
			fromXml(librisMgr);
			if (doLoadMetadata) {
				loadMetadata(librisMgr);
			}
			if (isLocked()) {
				readOnly = true;
			}
			librisMgr.closeFile();
			databaseFileMgr.releaseIpStream(fileIpStream);
		} catch (XmlException e) {	
			throw new DatabaseException("Error reading schema", e); //$NON-NLS-1$
		} catch (IOException e) {	
			throw new DatabaseException("Error closing schema file", e); //$NON-NLS-1$
		}
	}

	@Override
	public void fromXml(ElementManager librisMgr) throws LibrisException {
		HashMap<String, String> dbElementAttrs = librisMgr.parseOpenTag();
		String dateString = dbElementAttrs.get(XML_DATABASE_DATE_ATTR);
		if ((null == dateString) || dateString.isEmpty()) {
			databaseDate = new Date();
		} else {
			try {
				databaseDate = LibrisMetadata.parseDateString(dateString);
			} catch (ParseException e) {
				throw new InputException("illegal date format: "+dateString, e);
			}
		}

		String nextElement = librisMgr.getNextId();
		DatabaseInstance instanceInfo = null;
		if (XML_INSTANCE_TAG.equals(nextElement)) {
			ElementManager instanceMgr = librisMgr.nextElement();
			instanceInfo  = new DatabaseInstance();
			instanceInfo.fromXml(instanceMgr);
			metadata.setInstanceInfo(instanceInfo);
			nextElement = librisMgr.getNextId();
		}
		xmlAttributes = new DatabaseAttributes(this, dbElementAttrs);
		if (xmlAttributes.isLocked()) {
			readOnly = true;
		}
	}

	private void loadMetadata(ElementManager librisMgr)
			throws DatabaseException, XmlException, InputException, LibrisException {
		String nextElementId = librisMgr.getNextId();
		ElementManager metadataMgr;
		if (XML_METADATA_TAG.equals(nextElementId)) {
			metadataMgr = librisMgr.nextElement();
		} else {
			final String schemaLocation = xmlAttributes.getSchemaLocation();
			final String schemaName = xmlAttributes.getSchemaName();
			metadataMgr = makeMetadataMgr(schemaName, schemaLocation);
		}
		Assertion.assertNotNullInputException("could not open schema file", metadataMgr);
		metadata.fromXml(metadataMgr);
	}
	
	public static boolean newDatabase(LibrisDatabaseParameter params, LibrisMetadata metadata) 
			throws XMLStreamException, IOException, LibrisException {
		File databaseFile = params.databaseFile;
		if (!databaseFile .createNewFile()) {
			params.ui.alert("Database file "+databaseFile.getAbsolutePath()+" already exisits");
			return false;
		}
		FileOutputStream databaseStream = new FileOutputStream(databaseFile);
		ElementWriter databaseWriter = ElementWriter.eventWriterFactory(databaseStream, 0);
		{
			LibrisAttributes attrs = new LibrisAttributes();
			attrs.setAttribute(XML_DATABASE_SCHEMA_NAME_ATTR, params.schemaName);
			attrs.setAttribute(XML_SCHEMA_VERSION_ATTR, Schema.currentVersion);
			attrs.setAttribute(XML_DATABASE_DATE_ATTR, LibrisMetadata.getCurrentDateAndTimeString());
			databaseWriter.writeStartElement(XML_LIBRIS_TAG, attrs, false);
		}
		metadata.toXml(databaseWriter);
		{
			LibrisAttributes recordsAttrs = new LibrisAttributes();
			recordsAttrs.setAttribute(XML_RECORDS_LASTID_ATTR, 0);
			databaseWriter.writeStartElement(XML_RECORDS_TAG, recordsAttrs, true);
		}
		databaseWriter.writeEndElement();
		databaseWriter.flush();
		databaseStream.close();
		
		return Libris.buildIndexes(databaseFile, params.ui);
	}

	boolean buildIndexes(boolean doLoadMetadata) throws LibrisException {
		fileMgr.createAuxFiles(true);
		if (reserveDatabase()) {
			loadDatabaseInfo(doLoadMetadata);
			mainRecordTemplate = RecordTemplate.templateFactory(mySchema, new DatabaseRecordList(this));
			final File databaseFile = fileMgr.getDatabaseFile();
			metadata.setSavedRecords(0);
			XmlRecordsReader.importXmlRecords(this, databaseFile);
			Records recs = getDatabaseRecords();
			indexMgr.open();
			indexMgr.buildIndexes(recs);
			save();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		toXml(outWriter, true, getRecordReader(), false);
	}

	public String getElementTag() {
		return getXmlTag();
	}

	public String getXmlTag() {
		return XML_LIBRIS_TAG;
	}
	private void toXml(ElementWriter outWriter, boolean includeMetadata,
			Iterable<Record> recordSource, boolean includeInstanceInfo) throws XmlException, LibrisException {
		outWriter.writeStartElement(XML_LIBRIS_TAG, getAttributes(), false);
		if (includeInstanceInfo) {
			DatabaseInstance databaseInstanceInfo = metadata.getInstanceInfo();
			if (Objects.isNull(databaseInstanceInfo)) {
				databaseInstanceInfo = new DatabaseInstance(metadata);
			}
			databaseInstanceInfo.toXml(outWriter);
		}
		if (includeMetadata) {
			metadata.toXml(outWriter, includeInstanceInfo);
		}
		if (null != recordSource) {
			LibrisAttributes recordsAttrs = new LibrisAttributes();
			recordsAttrs.setAttribute(XML_RECORDS_LASTID_ATTR, RecordId.toString(metadata.getLastRecordId()));
			outWriter.writeStartElement(XML_RECORDS_TAG, recordsAttrs, false);
			for (Record r: recordSource) {
				r.toXml(outWriter);
			}
			outWriter.writeEndElement(); /* records */
		}
		outWriter.writeEndElement(); /* database */	
		outWriter.flush();
	}

	/**
	 * Read records from a database forked instance and merge them to the current database.
	 * @param instanceReader reader for the XML of the exported records
	 * @throws FileNotFoundException if the specified increment file is missing
	 * @throws LibrisException if an error is encountered
	 */
	public boolean importIncrement(File incrementFile) throws FileNotFoundException, LibrisException {
		ElementManager incrementManager = makeLibrisElementManager(incrementFile);
		HashMap<String, String> incElementAttrs = incrementManager.parseOpenTag();
		if (
				!assertEquals(ui, "increment databasename attribute does not match database", xmlAttributes.getDatabaseName(), incElementAttrs.get(XML_DATABASE_NAME_ATTR))
				|| !		assertEquals(ui, "increment databasename attribute does not match database", xmlAttributes.getSchemaName(), incElementAttrs.get(XML_DATABASE_SCHEMA_NAME_ATTR))
				) {
			return false;
		}
		String nextElement = incrementManager.getNextId();
		if (!assertEquals(ui, "Wrong initial element in increment file", XML_INSTANCE_TAG, nextElement)) {
			return false;
		};
		ElementManager instanceMgr = incrementManager.nextElement();
		DatabaseInstance instanceInfo = new DatabaseInstance();
		instanceInfo.fromXml(instanceMgr);
		if (
				!assertNotNull(ui, "instance forkdate",instanceInfo.getForkDate())
				|| !assertNotNull(ui, "instance joindate",instanceInfo.getJoinDate())
				) {
			return false;
		}
		int baseId = instanceInfo.getRecordIdBase();
		int lastRecordId = getLastRecordId();
		if (!assertTrue(ui, "increment base record ID > root database last ID", baseId <= lastRecordId)) {
			return false;
		}
		int idAdjustment = lastRecordId - baseId;
		nextElement = incrementManager.getNextId();
		if (!assertEquals(ui, "Wrong element after instance element in increment file", XML_RECORDS_TAG, nextElement)) {
			return false;
		};
		ElementManager recsMgr = incrementManager.nextElement();
		XmlRecordsReader recordsReader = new XmlRecordsReader(this, recsMgr);
		for (Record newRec: recordsReader) {
			if (idAdjustment > 0) {
				newRec.offsetIds(baseId, idAdjustment);
			}
			put(newRec);
		}
		return true;
	}
	
	// TODO test read-onlyness

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
				alert("Exception saving properties file"+propsMgr.getPath(), e); //$NON-NLS-1$
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
		mySchema = null;
		if (null != indexMgr) {
			try {
				indexMgr.close();
			} catch (InputException | DatabaseException | IOException e) {
				log(Level.SEVERE, "error destroying database", e);
			}
		}
		indexMgr = null;
		if (null != fileMgr) {
			fileMgr.freeDatabase();
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
			if (!mySchema.equals(otherDb.mySchema)) {
				log(Level.FINE, "schema do not match"); //$NON-NLS-1$
				return false;
			}
			try {
				if (getLastRecordId() != otherDb.getLastRecordId()) {
					return false;
				}
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
						throw new InputException(LibrisConstants.COULD_NOT_OPEN_SCHEMA_FILE+schemaFile.getPath(), e);
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
		} catch (FileNotFoundException | DatabaseException | MalformedURLException e) {
			throw new InputException(LibrisConstants.COULD_NOT_OPEN_SCHEMA_FILE+schemaLocation, e);
		}
	}

	public void setSchema(Schema schem) {
		this.mySchema = schem;
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

	public void setAttributes(DatabaseAttributes attrs) {
		xmlAttributes = attrs;
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

	public static LibrisXmlFactory getXmlFactory() {
		return xmlFactory;
	}

	public Iterable<Record> getRecordReader() throws LibrisException {
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
		return mySchema;
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
	
	public void setTermCount(String term, boolean normalize, int termCount) throws DatabaseException {
		indexMgr.setTermCount(term, normalize, termCount);
	}

	public int getTermCount(String term, boolean normalize) throws DatabaseException {
		return indexMgr.getTermCount(term, normalize);
	}

	/**
	 * Save a record in the list of modified records
	 * @param rec Record to enter
	 * @return ID of the new record
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
				throw new DatabaseException("Record "+id+" is read-only");
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
		for (int g = 0; g < mySchema.getNumGroups(); ++g) {
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

	public RecordList getKeywordFilteredRecordList(KeywordFilter filter) {
		return null;
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
	
	public boolean isLocked() {
		return xmlAttributes.isLocked();
	}

	public void lockDatabase() {
		 xmlAttributes.setLocked(true);
		 readOnly = true;
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
		toXml(outWriter, includeSchema, getRecordReader(), addInstanceInfo); 
	}

	public boolean exportIncrement(OutputStream destination) throws OutputException  {
		if (isModified()) {
			ui.alert("Cannot export an increment from a database which has unsaved changes");
			return false;
		}
		DatabaseInstance instanceInfo = metadata.getInstanceInfo();
		if (!isFork()) {
			ui.alert("Cannot export an increment from a database which is not a fork");
			return false;
		}
		lockDatabase();
		instanceInfo.doJoin();
		Iterable<Record> rangeIter = new RangeRecordIterable(this, instanceInfo.getRecordIdBase(), getLastRecordId());
		try {
			ElementWriter outWriter = ElementWriter.eventWriterFactory(destination);
			toXml(outWriter, false, rangeIter, true); 
		} catch (XMLStreamException | LibrisException e) {
			throw new OutputException(Messages.getString("LibrisDatabase.exception_export_xml"), e); //$NON-NLS-1$
		}
		return true;
	}

	public void exportFork(FileOutputStream instanceStream) throws LibrisException {
		if (isFork()) {
			throw new UserErrorException("Cannot fork from a database fork");			
		}
		exportDatabaseXml(instanceStream, true, true, true);

	}

	public boolean isFork() {
		DatabaseInstance instanceInfo = metadata.getInstanceInfo();
		return (null != instanceInfo);
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

	public Date getDatabaseDate() {
		return databaseDate;
	}

	public void setDatabaseDate(Date databaseDate) {
		this.databaseDate = databaseDate;
	}
}


